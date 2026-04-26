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
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val api: LinkArenaApi,
    private val groupDao: GroupDao,
    private val json: Json
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
            val responseBody = response.body()
            val resolvedGroup = decodeGroup(responseBody)
            if (response.isSuccessful && resolvedGroup != null) {
                val group = resolvedGroup
                groupDao.insert(group.toEntity())
                NetworkResult.Success(group)
            } else {
                NetworkResult.Error(extractErrorMessage(responseBody, "Create failed"))
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error")
        }
    }

    override suspend fun updateGroup(id: String, name: String?, color: String?, order: Int?): NetworkResult<Group> {
        return try {
            val response = api.updateGroup(id, UpdateGroupRequest(name, color, order))
            val responseBody = response.body()
            val resolvedGroup = decodeGroup(responseBody)
            if (response.isSuccessful && resolvedGroup != null) {
                val group = resolvedGroup
                groupDao.update(group.toEntity())
                NetworkResult.Success(group)
            } else {
                NetworkResult.Error(extractErrorMessage(responseBody, "Update failed"))
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
                val body = response.body()
                    ?: return NetworkResult.Error("Sync failed")

                val groupsSerializer = ListSerializer(GroupDto.serializer())
                val groupDtos: List<GroupDto> = when (body) {
                    is JsonArray -> json.decodeFromJsonElement(groupsSerializer, body)
                    else -> {
                        val groupsElement = body.jsonObject["groups"]
                            ?: return NetworkResult.Error("Sync failed")
                        json.decodeFromJsonElement(groupsSerializer, groupsElement.jsonArray)
                    }
                }

                val groups = groupDtos.map { it.toDomain().toEntity() }
                val remoteGroups = groups.sortedBy { it.id }
                val localGroups = groupDao.getAllGroupsSnapshot().sortedBy { it.id }
                if (remoteGroups != localGroups) {
                    groupDao.replaceAll(remoteGroups)
                }
                NetworkResult.Success(Unit)
            } else {
                if (response.code() == 401) {
                    NetworkResult.Error("Authentication expired. Please sign in again.")
                } else {
                    NetworkResult.Error("Sync failed")
                }
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

    private fun decodeGroup(body: JsonElement?): Group? {
        val root = body as? JsonObject ?: return null
        val candidates = buildList {
            root["group"]?.let { add(it) }
            root["category"]?.let { add(it) }
            root["data"]?.let { add(it) }
            if (root["id"] != null && root["name"] != null) {
                add(root)
            }
        }

        return candidates.firstNotNullOfOrNull { element ->
            runCatching {
                json.decodeFromJsonElement(GroupDto.serializer(), element).toDomain()
            }.getOrNull()
        }
    }

    private fun extractErrorMessage(body: JsonElement?, fallback: String): String {
        val root = body as? JsonObject ?: return fallback
        val direct = root["error"]?.jsonPrimitive?.contentOrNull
            ?: root["message"]?.jsonPrimitive?.contentOrNull
        if (!direct.isNullOrBlank()) return direct

        val nestedError = (root["error"] as? JsonObject)
            ?.get("message")
            ?.jsonPrimitive
            ?.contentOrNull
        return if (!nestedError.isNullOrBlank()) nestedError else fallback
    }
}
