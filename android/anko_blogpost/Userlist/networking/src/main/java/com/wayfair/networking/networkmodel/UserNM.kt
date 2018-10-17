package com.wayfair.networking.networkmodel

data class UserNM(
        val login: String = "",
        val id: Int = 0,
        val node_id: String = "",
        val avatar_url: String = "",
        val gravatar_id: String = "",
        val url: String = "",
        val html_url: String = "",
        val type: String = "",
        val site_admin: Boolean = false
        )