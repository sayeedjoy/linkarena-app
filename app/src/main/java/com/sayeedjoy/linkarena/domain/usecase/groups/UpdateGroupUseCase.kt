package com.sayeedjoy.linkarena.domain.usecase.groups

import com.sayeedjoy.linkarena.domain.model.Group
import com.sayeedjoy.linkarena.domain.repository.GroupRepository
import com.sayeedjoy.linkarena.util.NetworkResult
import javax.inject.Inject

class UpdateGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(id: String, name: String?, color: String?, order: Int?): NetworkResult<Group> {
        if (name != null && name.isBlank()) {
            return NetworkResult.Error("Group name cannot be empty")
        }
        return groupRepository.updateGroup(id, name, color, order)
    }
}
