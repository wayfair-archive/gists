package com.wayfair.userlistanko.ui

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import com.wayfair.userlistanko.R
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.constraintLayout

class AnkoViewHolderListUI : AnkoComponent<ViewGroup> {

    override fun createView(ui: AnkoContext<ViewGroup>): View {
        return with(ui) {
            frameLayout {

                constraintLayout() {
                    id = R.id.list_card
                    backgroundColor = Color.WHITE

                    imageView() {
                        setBackgroundResource(R.drawable.circle_frame)
                        padding = dip(6)
                        id = R.id.profile_circle_image_viewholder
                    }.lparams(width = dip(42), height = dip(42)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            marginStart = dip(8)
                        } else {
                            leftMargin = dip(8)
                        }
                        topMargin = dip(8)
                        bottomMargin = dip(8)
                        leftToLeft = PARENT_ID
                        topToTop = PARENT_ID
                        bottomToBottom = PARENT_ID
                    }
                    textView() {
                        id = R.id.profile_text_viewholder
                        textSize = 6f
                    }.lparams(width = wrapContent, height = wrapContent) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            marginStart = dip(16)
                        } else {
                            leftMargin = dip(16)
                        }
                        topToTop = PARENT_ID
                        bottomToTop = R.id.node_id_text_viewholder
                        startToEnd = R.id.profile_circle_image_viewholder
                    }
                    textView() {
                        id = R.id.node_id_text_viewholder
                        textSize = 6f
                    }.lparams(width = wrapContent, height = wrapContent) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            marginStart = dip(16)
                        } else {
                            leftMargin = dip(16)
                        }
                        topToBottom = R.id.profile_text_viewholder
                        bottomToTop = R.id.site_admin_text_viewholder
                        startToEnd = R.id.profile_circle_image_viewholder
                    }
                    textView() {
                        id = R.id.site_admin_text_viewholder
                        textSize = 6f
                    }.lparams(width = wrapContent, height = wrapContent) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            marginStart = dip(16)
                        } else {
                            leftMargin = dip(16)
                        }
                        topToBottom = R.id.node_id_text_viewholder
                        bottomToTop = R.id.url_text_viewholder
                        startToEnd = R.id.profile_circle_image_viewholder
                    }
                    textView() {
                        id = R.id.url_text_viewholder
                        textSize = 6f
                    }.lparams(width = wrapContent, height = wrapContent) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            marginStart = dip(16)
                        } else {
                            leftMargin = dip(16)
                        }
                        topToBottom = R.id.site_admin_text_viewholder
                        bottomToTop = R.id.html_url_id_text_viewholder
                        startToEnd = R.id.profile_circle_image_viewholder
                    }
                    textView() {
                        id = R.id.html_url_id_text_viewholder
                        textSize = 6f
                    }.lparams(width = wrapContent, height = wrapContent) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            marginStart = dip(16)
                        } else {
                            leftMargin = dip(16)
                        }
                        topToBottom = R.id.url_text_viewholder
                        bottomToTop = R.id.type_id_text_viewholder
                        startToEnd = R.id.profile_circle_image_viewholder
                    }
                    textView() {
                        id = R.id.type_id_text_viewholder
                        textSize = 6f
                    }.lparams(width = wrapContent, height = wrapContent) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            marginStart = dip(16)
                        } else {
                            leftMargin = dip(16)
                        }
                        topToBottom = R.id.html_url_id_text_viewholder
                        bottomToTop = R.id.shadow_view_anko_viewholder
                        startToEnd = R.id.profile_circle_image_viewholder
                    }
                    view() {
                        id = R.id.shadow_view_anko_viewholder
                        backgroundColor = Color.LTGRAY

                    }.lparams(width = wrapContent, height = dip(0.5f)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            marginStart = dip(16)
                        } else {
                            leftMargin = dip(16)
                        }
                        bottomToBottom = PARENT_ID
                        startToEnd = R.id.profile_circle_image_viewholder
                    }


                }.lparams(width = matchParent, height = wrapContent)
            }
        }
    }
}