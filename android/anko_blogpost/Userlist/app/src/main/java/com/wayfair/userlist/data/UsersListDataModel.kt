package com.wayfair.userlist.data

data class UsersListDataModel(
    val usersList: MutableList<UserDataModel>
) {
    data class UserDataModel(
        val login: String = "",
        val id: Int = 0,
        val node_id: String = "",
        val avatar_url: String = "",
        val url: String = "",
        val html_url: String = "",
        val type: String = "",
        val site_admin: Boolean = false
    )
}