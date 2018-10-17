package com.wayfair.userlistanko.ui

import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wayfair.userlistanko.R
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.dip
import org.jetbrains.anko.recyclerview.v7.recyclerView

class AnkoHomeUI : AnkoComponent<HomeActivity> {

    lateinit var recyclerViewList: RecyclerView

    override fun createView(ui: AnkoContext<HomeActivity>) = with(ui) {
        constraintLayout() {
            id = R.id.root
            recyclerViewList = recyclerView {
                id = R.id.recycler_view
                layoutManager = GridLayoutManager(context, 2)
                backgroundColor = Color.WHITE
                isFocusableInTouchMode = true

            }.lparams(width = dip(0), height = dip(0)) {
                topToTop = PARENT_ID
                leftToLeft = PARENT_ID
                rightToRight = PARENT_ID
                bottomToBottom = PARENT_ID

            }
        }
    }
}