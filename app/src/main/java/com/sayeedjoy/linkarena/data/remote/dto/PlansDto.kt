package com.sayeedjoy.linkarena.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlansResponseDto(
    val plans: List<PlanItemDto> = emptyList()
)

@Serializable
data class PlanItemDto(
    val id: String = "",
    val slug: String = "",
    val displayName: String = "",
    val googlePlayProductId: String = "",
    val aiGroupingAllowed: Boolean = false,
    val groupColoringAllowed: Boolean = false,
    val browserBulkImportAllowed: Boolean = false,
    val browserRealtimeSyncAllowed: Boolean = false,
    val bookmarkQuotaPerDay: Int? = null,
    val sortOrder: Int = 0
)
