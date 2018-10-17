package com.wayfair.userlistanko.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.wayfair.userlistanko.data.UsersListDataModel
import dagger.android.AndroidInjection
import org.jetbrains.anko.setContentView
import javax.inject.Inject

class HomeActivity : AppCompatActivity() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val ankoHomeUI: AnkoHomeUI = AnkoHomeUI()
    private lateinit var homeViewModel: HomeViewModel
    private val adapter = HomeRecyclerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState)
        ankoHomeUI.setContentView(this)

        ankoHomeUI.recyclerViewList.adapter = this.adapter

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