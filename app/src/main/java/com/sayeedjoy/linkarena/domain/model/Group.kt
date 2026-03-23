package com.sayeedjoy.linkarena.domain.model

data class Group(
    val id: String,
    val name: String,
    val color: String?,
    val order: Int,
    val bookmarkCount: Int = 0
)
