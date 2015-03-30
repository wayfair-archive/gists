#!/usr/bin/env python

import collections
import fcntl
import graypy
import jinja2
import json
import logging
import shlex
import shutil
import signal
import socket
import subprocess
import sys
import os
import threading
import time

import kazoo.exceptions

from ConfigParser import SafeConfigParser
from ConfigParser import RawConfigParser
from io import BytesIO
from kazoo.client import KazooClient
from kazoo.client import KeeperState
from kazoo.client import TransactionRequest
from kazoo.protocol.states import KazooState
from kazoo.recipe.watchers import DataWatch

APPLICATION_INSTALL_PATH = "/i/have/my/apps/here/zk_failover/"

def write_pid_file():
    """We keep the file open with locks on to avoid race conditions on starting the service."""
    global f
    pid = str(os.getpid())
    f = open('/some/path/for/pids/zk_failover.pid', 'a')
    fcntl.flock(f, fcntl.LOCK_EX | fcntl.LOCK_NB)
    fcntl.fcntl(f, fcntl.F_SETFL, os.O_WRONLY)
    f.seek(0)
    f.truncate()
    f.write(pid)
    f.flush()
    os.fsync(f.fileno())

def safe_write_file(content, filename):
    """
    When you write a file, there's a chance that the file contents will be empty for a period
    in between the truncate and the writes. Move, however, is atomic, so the safe way to do
    this is to write a tmp file, then move it to the location we want it to be in. This prevents
    PHP reading an empty file and failing requests in that critical period.

    The tmp file will be written to the same directory. Move is only guaranteed to be atomic
    (at least on our filesystems) on the same partition.

    Args:
    content: what we want in the file
    filename: full filepath of the file we want to write
    """
    try:
        with open(filename + '-tmp', 'wb') as configfile:
            configfile.write(content)
            configfile.flush()
            os.fsync(configfile.fileno())

        shutil.move(filename + '-tmp', filename)

        return True

    except IOError:
        log.critical('cannot write content to' + filename)
        return False


def signal_handler(signal, frame):
    log.warning('got shutdown signal')
    if config['role'] == 'resource':
        node_to_fail()
    log.info('__SYSTEM_SHUTDOWN__')
    sys.exit(0)


def set_signal_handlers():
    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)
    signal.signal(signal.SIGHUP, signal_handler)
    signal.signal(signal.SIGQUIT, signal_handler)
    log.info('setup my signal handlers')


def sanitize_dict(srcdict):
    for k, v in srcdict.items():
        srcdict[k] = v.strip('\'"')


def setup_logging():
    logging.basicConfig(filename=config['log-file'], level='WARNING', format='%(asctime)s %(levelname)s %(module)s %(process)d %(funcName)s: %(message)s')
    log = logging.getLogger(__name__)
    log.setLevel(config['log-level'])

    gelf_handler = graypy.GELFHandler(wfconfig['named_gelfreceiver'], 12201)
    gelf_handler.setLevel('CRITICAL')
    log.addHandler(gelf_handler)

    log.info('got my config')

    return log


def get_config():
    '''Everybody does their configs differently for this kind of thing.  Figure it out.'''
    config_parser = SafeConfigParser()
    config_parser.read(['/path/to/ini/files/zk_failover.ini', '/path/to/ini/files/globalish-ini-thing.ini'])

    config = dict(config_parser.items('zk_failover'))
    configtwo = dict(config_parser.items('resources'))

    sanitize_dict(config)
    sanitize_dict(configtwo)

    return config, configtwo


def conn_listener_zk(state):
    if state == KazooState.LOST:
        log.critical('connection lost in listener')
    elif state == KazooState.SUSPENDED:
        log.critical('connection suspended')
    elif state == KazooState.CONNECTED:
        log.info('connection is live')
        zk_events_queue.appendleft('Sync on regaining connection')


def connect_zk():
    log.info('trying to start new connection')
    try:
        zk.start()
        zk.add_listener(conn_listener_zk)
        log.info('got my zk connection')
        return True
    except kazoo.handlers.threading.TimeoutError:
        log.critical('got timeout exception, we ll try once again later')
        return False


def stop_zk():
    log.info('stop_zk: trying to stop existing connection')
    zk.stop()
    log.info('stop_zk: connection has been closed')


def verify_zk(conn_active):
    if conn_active:
        if zk.state == KazooState.CONNECTED:
            return True
        elif zk.state == KazooState.LOST:
            stop_zk()
            while not connect_zk():
                pass
        else:
            return False
    else:
        if zk.state != KazooState.LOST:
            log.warning('anomality found, trying to stop connection')
            stop_zk()
            return False
        else:
            return True


def node_to_slave(leader):
    result = os.popen(config['to-slave'] + " " + config['shard'] + ' ' + leader).read()
    result = result.strip()
    if result != 'OK':
        log.critical('failed script execution: ' + result)
        node_to_fail()


def node_to_master():
    result = os.popen(config['to-master'] + ' ' + config['shard']).read()
    result = result.strip()
    if result != 'OK':
        log.critical('failed script execution: ' + result)
        node_to_fail()


def node_to_fail():
    log.info('calling to-fail script')
    result = os.popen(config['to-fail'] + ' ' + config['shard']).read()
    result = result.strip()
    log.info('script output: ' + result)


def watch_callback_zk(WatchedEvent):
    zk_events_queue.append(WatchedEvent.path)
    log.info('got event from ZK! Path: {0} Type: {1} State: {2}'.format(WatchedEvent.path, WatchedEvent.type, WatchedEvent.state))


def leader_election():
    try:
        zk.ensure_path(config['zk_leader_root'])
        if zk.exists(config['zk_leader_root'] + '/' + config['shard']):
            leader, stat = zk.get(config['zk_leader_root'] + '/' + config['shard'], watch=watch_callback_zk)
            if leader == config['my_ip']:
                log.info('Im master, synchronizing')
                node_to_master()
            elif leader == '':
                log.info('leader is empty, promoting myself')
                zk.set(config['zk_leader_root'] + '/' + config['shard'], config['my_ip'])
                node_to_master()
            else:
                log.info('Im slave, synchronizing')
                node_to_slave(leader)
        else:
            log.info('promoting myself to master')
            zk.create(config['zk_leader_root'] + '/' + config['shard'], config['my_ip'], ephemeral=True, makepath=True)
            node_to_master()
        return True
    except (kazoo.exceptions.ZookeeperError, kazoo.exceptions.NoNodeError, kazoo.exceptions.NodeExistsError, kazoo.exceptions.ConnectionLoss):
        log.warning('ZooKeeper returned non-zero code, thats weird but do not panic, we will retry in a moment')
        return False


def init_healthcheck_thread():
    healthcheck_thread = threading.Thread(target=healthcheck)
    healthcheck_thread.daemon = True
    healthcheck_thread.start()
    return healthcheck_thread


def healthcheck():
    global healthcheck_status, healthcheck_status_last_change
    while True:
        failure_count = 0
        success_count = 0

        while failure_count < int(config['healthcheck-fails']):
            healthcheck_results = os.popen(config['healthcheck']).read()
            healthcheck_results = healthcheck_results.strip()
            if healthcheck_results != 'OK':
                failure_count += 1
                log.warning('healthcheck-fails: ' + str(failure_count) + ' output: ' + healthcheck_results)
            else:
                failure_count = 0
                success_count += 1
                if success_count >= int(config['healthcheck-fails']):
                    if not healthcheck_status:
                        healthcheck_status = True
                        healthcheck_status_last_change = int(time.time())
                        log.info('success threshold is met, re-enabling the service')
                    success_count = 0
                else:
                    if not healthcheck_status:
                        log.warning('healthcheck successful but threshold is not met ' + str(success_count))

            time.sleep(float(config['healthcheck-interval']))

        healthcheck_status = False
        healthcheck_status_last_change = int(time.time())
        log.critical('max healthcheck-fails exceeded: ' + str(failure_count))
        log.warning('going to sleep for 5 second before next retry')
        time.sleep(5)


def resource_processor():
    global healthcheck_status, healthcheck_status_last_change
    log.debug('entering processor')

    my_ip = 'foo' # get your ips somehow

    config['zk_leader_root'] = '/zk_failover/' + config['app'] + '/' + config['cluster']
    config['my_ip'] = my_ip

    healthcheck_status = False
    healthcheck_status_last_change = int(time.time())
    service_active = False
    last_sync_time = int(time.time()) - int(config['sync-interval'])

    healthcheck_thread = init_healthcheck_thread()

    while True:
        if healthcheck_status:
            if verify_zk(True):
                service_active = True
                while len(zk_events_queue) > 0:
                    event = zk_events_queue.popleft()
                    log.info('processing event from ZK: ' + str(event))
                    log.debug('healthcheck status in processor: ' + str(healthcheck_status))
                    if leader_election():
                        last_sync_time = int(time.time())
                    else:
                        zk_events_queue.appendleft(event)
                        time.sleep(1)

                if (int(time.time()) - last_sync_time) > int(config['sync-interval']):
                    log.info('synchronizing status with ZK based on interval')
                    log.debug('healthcheck status in processor: ' + str(healthcheck_status))

                    if leader_election():
                        last_sync_time = int(time.time())

            if (int(time.time()) - last_sync_time) > (int(config['sync-interval']) * 5):
                log.critical('synchronization is out of SLA. Please check this ASAP')

        else:
            verify_zk(False)
            if service_active:
                node_to_fail()
                service_active = False

        if not healthcheck_thread.is_alive():
            log.critical('healthcheck thread is dead, creating a new one')
            healthcheck_thread = init_healthcheck_thread()

        time.sleep(1)


def handle_twemproxy(resource_details, resource_path):
    if 'redis' not in resource_path.lower() and \
       'memcache' not in resource_path.lower() and \
       'first_run' not in resource_path.lower():
        return True

    config_file, a_or_b = generate_twemproxy_config(resource_details)

    if not config_file or not a_or_b:
        return False

    kill_twemproxy(a_or_b)
    retval = start_twemproxy(config_file, a_or_b)
    log.info('Retval was ' + str(retval))
    if retval is 0 and write_php_twemproxy(a_or_b):
        return kill_twemproxy('a' if a_or_b is 'b' else 'b')
    else:
        log.error('Couldn\'t start twemproxy!')
        return False


def twemproxy_a_or_b():
    val = 'a'
    # twemproxy A listens on 127.0.0.10, so see if we can bind to that
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        s.bind(('127.0.0.10', 6379))
    except socket.error:
        val = 'b'

    s.close()
    return val


def start_twemproxy(config_file, a_or_b):
    command = ['timeout 2 /path/to/programs/nutcracker']
    command.append('-c ' + config_file)
    command.append('-p /var/run/nutcracker/twemproxy_' + a_or_b + '.pid')
    command.append('-s ' + ('11291' if a_or_b is 'a' else '11292'))
    command.append('-d')
    command.append('-o /path/to/log/files/twemproxy/twemproxy_' + a_or_b + '.log')
    command.append('-v ' + twemproxy_log_level)

    log.info('Running ' + ' '.join(command))

    try:
        p = subprocess.Popen(shlex.split(' '.join(command)), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        p.wait()
        # Twemproxy doesn't start accepting connections right away, so give it a chance to get started before we use it
        time.sleep(3)
        return p.returncode
    except OSError:
        log.error('Couldn\'t open subprocess!')


def kill_twemproxy(a_or_b):
    # Make sure that all of the outstanding requests finish
    time.sleep(1)

    try:
        with open('/var/run/nutcracker/twemproxy_' + a_or_b + '.pid') as pidfile:
            pid = int(pidfile.readline())
    except IOError:
        log.error('No pidfile for old twemproxy found. Maybe wasn\'t running?')
        return False

    log.info('Killing twemproxy with pid ' + str(pid))
    try:
        os.kill(pid, signal.SIGTERM)
    except OSError as e:
        log.error('Twemproxy wasn\'t running! Ignore if this is the first run. ' + e.strerror)
        return False

    return True


def write_php_twemproxy(a_or_b):
    '''This actually writes the file that PHP reads to know which ip twemproxy is running on'''
    ip = '127.0.0.10' if a_or_b is 'a' else '127.0.0.11'
    return safe_write_file('[ip]\nTWEMPROXY = ' + ip + '\n\n', '/somewhere-for-etcs/etc/twemproxy-config-file-name.ini')

def generate_twemproxy_config(resource_details):
    log.info('Generating twemproxy config...')
    a_or_b = twemproxy_a_or_b()
    filename = '/path/to/etc-like/dir/etc/twemproxy_conf_' + a_or_b

    apps = {k:v for (k,v) in resource_details.iteritems() if 'memcache' in k.lower() or 'redis' in k.lower()}

    template_vars = {
                      'listen_ip' : '127.0.0.10' if a_or_b is 'a' else '127.0.0.11',
                      'hash' : 'fnv1a_32',
                      'distribution' : 'ketama',
                      'timeout' : '400',
                      'backlog' : '1024',
                      'server_retry_timeout' : '2000',
                      'server_failure_limit' : '3',
                      'preconnect' : 'false',
                      'apps' : apps,
                    }

    template = template_env.get_template('twemproxy_conf.jinja')
    config = template.render(template_vars)

    if safe_write_file(config, filename):
        return filename, a_or_b
    else:
        return False, False


def generate_config_dynamic(resource_details):
    log.info('Generating config-dynamic...')
    dynamic_config_name = '/path/to/etc-like/dir/etc/config-dynamic.ini'
    dynamic_config = RawConfigParser()
    dynamic_config.optionxform = str
    dynamic_config.add_section('dynamic_resources')

    for app in resource_details:
        dataset = json.dumps(resource_details[app], sort_keys=True)
        if dataset:
            dynamic_config.set('dynamic_resources', 'WF_' + app.upper() + '_SHARDS', "'" + dataset + "'")
        else:
            log.warning('got invalid dataset for: ' + app + ' value: ' + str(resource_details[app]))

    buffer = BytesIO()
    dynamic_config.write(buffer)

    return safe_write_file(buffer.getvalue(), dynamic_config_name)


def sync_client(is_push=False, resource_path=''):

    apps_to_sync = config['app'].strip().split(',')
    app_buff = {}

    for app in apps_to_sync:
        app_path = '/zk_failover/' + app + '/' + config['cluster'] + '/'
        try:
            if zk.exists(app_path):
                app_buff[app] = {}
                children = zk.get_children(app_path, watch=watch_callback_zk)
                for child in children:
                    val, stat = zk.get(app_path + '/' + child)
                    if val:
                        app_buff[app][app + '_' + child] = val
            else:
                log.warning('app: ' + app + 'is not in Zookeeper')
        except (kazoo.exceptions.ZookeeperError, kazoo.exceptions.NoNodeError, kazoo.exceptions.NodeExistsError, kazoo.exceptions.ConnectionLoss):
            log.warning('ZooKeeper returned non-zero code, thats weird but do not panic, we will retry in a moment')
            return False

    for func in sync_functions:
        func(app_buff)

    if is_push:
        for func in push_sync_functions:
            func(app_buff, resource_path)

    return True


def client_processor():
    log.debug('entering processor')
    last_sync_time = int(time.time()) - int(config['sync-interval'])

    zk_events_queue.append('first_run')

    while True:
        if verify_zk(True):
            while len(zk_events_queue) > 0:
                event = zk_events_queue.popleft()
                log.info('processing event from ZK: ' + str(event))
                if sync_client(True, event):
                    last_sync_time = int(time.time())
                else:
                    zk_events_queue.appendleft(event)
                    time.sleep(1)

            if (int(time.time()) - last_sync_time) > int(config['sync-interval']):
                log.info('synchronizing status with ZK based on interval')
                if sync_client():
                    last_sync_time = int(time.time())
        else:
            if (int(time.time()) - last_sync_time) > (int(config['sync-interval']) * 5):
                log.critical('synchronization is out of SLA. Please check this ASAP')

        time.sleep(1)


def main():
    global config
    global template_env
    global wfconfig
    global sync_functions
    global push_sync_functions
    global log
    global zk
    global zk_events_queue
    global twemproxy_log_level

    config, configtwo = get_config()
    log = setup_logging()
    log.info('__SYSTEM_START__')

    try:
        write_pid_file()
    except IOError:
        log.critical('Couldn\'t get file lock for pid! Exiting...')
        exit(1)

    set_signal_handlers()
    zk = KazooClient(hosts=config['zookeeper'], timeout=4)
    zk_events_queue = collections.deque()

    template_loader = jinja2.FileSystemLoader(searchpath=APPLICATION_INSTALL_PATH + "templates/")
    template_env = jinja2.Environment(loader=template_loader)

    if config['role'] == 'resource':
        resource_processor()
    elif config['role'] == 'client':
        twemproxy_log_level = config.get('twemproxy-log-level', '4')

        try:
            sync_functions = [globals()[x] for x in config['sync-functions'].split(',')]
        except KeyError:
            log.error('No sync functions defined, exiting.')
            exit(1)

        push_sync_functions = []
        try:
            push_sync_functions = [globals()[x] for x in config['push-sync-functions'].split(',')]
        except KeyError:
            log.debug('No push-only functions registered.')

        client_processor()
    else:
        log.critical('role is not supported')


if __name__ == '__main__':
    main()
