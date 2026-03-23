package com.sayeedjoy.linkarena.domain.repository

import com.sayeedjoy.linkarena.domain.model.Group
import com.sayeedjoy.linkarena.util.NetworkResult
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun getGroups(): Flow<List<Group>>
    suspend fun getGroupById(id: String): Group?
    suspend fun createGroup(name: String, color: String?): NetworkResult<Group>
    suspend fun updateGroup(id: String, name: String?, color: String?, order: Int?): NetworkResult<Group>
    suspend fun deleteGroup(id: String): NetworkResult<Unit>
    suspend fun syncGroups(): NetworkResult<Unit>
}
