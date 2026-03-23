package com.sayeedjoy.linkarena.domain.usecase.groups

import com.sayeedjoy.linkarena.domain.model.Group
import com.sayeedjoy.linkarena.domain.repository.GroupRepository
import com.sayeedjoy.linkarena.util.NetworkResult
import javax.inject.Inject

class CreateGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(name: String, color: String?): NetworkResult<Group> {
        if (name.isBlank()) {
            return NetworkResult.Error("Group name is required")
        }
        return groupRepository.createGroup(name, color)
    }
}
