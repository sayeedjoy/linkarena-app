package com.sayeedjoy.linkarena.data.repository

import com.sayeedjoy.linkarena.data.local.db.GroupDao
import com.sayeedjoy.linkarena.data.local.db.entity.GroupEntity
import com.sayeedjoy.linkarena.data.remote.api.LinkArenaApi
import com.sayeedjoy.linkarena.data.remote.dto.CreateGroupRequest
import com.sayeedjoy.linkarena.data.remote.dto.GroupDto
import com.sayeedjoy.linkarena.data.remote.dto.UpdateGroupRequest
import com.sayeedjoy.linkarena.domain.model.Group
import com.sayeedjoy.linkarena.domain.repository.GroupRepository
import com.sayeedjoy.linkarena.util.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val api: LinkArenaApi,
    private val groupDao: GroupDao
) : GroupRepository {

    override fun getGroups(): Flow<List<Group>> {
        return groupDao.getAllGroups().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getGroupById(id: String): Group? {
        return groupDao.getGroupById(id)?.toDomain()
    }

    override suspend fun createGroup(name: String, color: String?): NetworkResult<Group> {
        return try {
            val response = api.createGroup(CreateGroupRequest(name, color))
            if (response.isSuccessful && response.body()?.group != null) {
                val group = response.body()!!.group!!.toDomain()
                groupDao.insert(group.toEntity())
                NetworkResult.Success(group)
            } else {
                NetworkResult.Error(response.body()?.error ?: "Create failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun updateGroup(id: String, name: String?, color: String?, order: Int?): NetworkResult<Group> {
        return try {
            val response = api.updateGroup(id, UpdateGroupRequest(name, color, order))
            if (response.isSuccessful && response.body()?.group != null) {
                val group = response.body()!!.group!!.toDomain()
                groupDao.update(group.toEntity())
                NetworkResult.Success(group)
            } else {
                NetworkResult.Error(response.body()?.error ?: "Update failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun deleteGroup(id: String): NetworkResult<Unit> {
        return try {
            val response = api.deleteGroup(id)
            if (response.isSuccessful) {
                groupDao.deleteById(id)
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.body()?.error ?: "Delete failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun syncGroups(): NetworkResult<Unit> {
        return try {
            val response = api.getGroups()
            if (response.isSuccessful) {
                val groups = response.body()!!.groups.map { it.toDomain().toEntity() }
                groupDao.deleteAll()
                groupDao.insertAll(groups)
                NetworkResult.Success(Unit)
            } else {
                NetworkResult.Error(response.body()?.error ?: "Sync failed")
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    private fun GroupEntity.toDomain() = Group(
        id = id,
        name = name,
        color = color,
        order = order,
        bookmarkCount = bookmarkCount
    )

    private fun Group.toEntity() = GroupEntity(
        id = id,
        name = name,
        color = color,
        order = order,
        bookmarkCount = bookmarkCount
    )

    private fun GroupDto.toDomain() = Group(
        id = id,
        name = name,
        color = color,
        order = order,
        bookmarkCount = if (bookmarkCount != 0) bookmarkCount else (count?.bookmarks ?: 0)
    )
}
