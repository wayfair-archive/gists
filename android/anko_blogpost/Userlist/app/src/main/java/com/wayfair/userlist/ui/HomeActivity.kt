package com.wayfair.userlist.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wayfair.userlist.R
import com.wayfair.userlist.data.UsersListDataModel
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.home_screen.*
import javax.inject.Inject

class HomeActivity : AppCompatActivity() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var adapter: HomeRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_screen)
        this.adapter = HomeRecyclerAdapter()
        recycler_view.apply {
            adapter = this@HomeActivity.adapter
            layoutManager = GridLayoutManager(context, 2)
            setHasFixedSize(true)
        }

        // Use a Factory to inject dependencies into the ViewModel
        homeViewModel = ViewModelProviders.of(this, factory).get(HomeViewModel::class.java)

        homeViewModel.getUsers().observe(this, Observer<UsersListDataModel> {
            this.adapter.updateList(it)
        })

        if(savedInstanceState == null){
            homeViewModel.fetchNewUsers()
        }
    }
}