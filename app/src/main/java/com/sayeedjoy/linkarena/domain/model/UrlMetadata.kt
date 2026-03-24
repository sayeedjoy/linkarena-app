package com.sayeedjoy.linkarena.domain.model

data class UrlMetadata(
    val normalizedUrl: String,
    val title: String?,
    val description: String?,
    val faviconUrl: String?
)
