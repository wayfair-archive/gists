package com.wayfair.userlistanko.ui

import android.util.TimingLogger
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wayfair.userlistanko.R
import com.wayfair.userlistanko.data.UsersListDataModel
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.find

class HomeRecyclerAdapter : RecyclerView.Adapter<HomeRecyclerAdapter.ListViewHolder>() {

    private val users = UsersListDataModel(mutableListOf())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val timings = TimingLogger(HomeRecyclerAdapter::class.java.simpleName, "onCreateViewHolder")
        val itemView = AnkoViewHolderListUI().createView(AnkoContext.create(parent.context, parent))
        timings.addSplit("onCreateViewHolder done")
        timings.dumpToLog()
        return ListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(users.usersList[position])
    }

    override fun getItemCount(): Int {
        return users.usersList.size
    }

    fun updateList(users: UsersListDataModel) {
        this.users.usersList.addAll(users.usersList)
        notifyDataSetChanged()
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.find(R.id.profile_circle_image_viewholder)
        private var profileText: TextView = itemView.find(R.id.profile_text_viewholder)
        private val nodeText: TextView = itemView.find(R.id.node_id_text_viewholder)
        private val siteAdminText: TextView = itemView.find(R.id.site_admin_text_viewholder)
        private val urlText: TextView = itemView.find(R.id.url_text_viewholder)
        private val htmlUrlText: TextView = itemView.find(R.id.html_url_id_text_viewholder)
        private val typeIdText: TextView = itemView.find(R.id.type_id_text_viewholder)


        fun bind(user: UsersListDataModel.UserDataModel) {
            profileImage.let {
                Glide.with(profileImage.context)
                        .load(user.avatar_url)
                        .apply(RequestOptions.circleCropTransform())
                        .into(it)
            }
            profileText.text = "${user.login} ${user.id}"
            nodeText.text = user.node_id
            siteAdminText.text = "Site admin: ${user.site_admin}"
            urlText.text = user.url
            htmlUrlText.text = user.html_url
            typeIdText.text = user.type
        }
    }

}