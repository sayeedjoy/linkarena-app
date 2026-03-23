package com.sayeedjoy.linkarena.domain.usecase.groups

import com.sayeedjoy.linkarena.domain.repository.GroupRepository
import com.sayeedjoy.linkarena.util.NetworkResult
import javax.inject.Inject

class DeleteGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(id: String): NetworkResult<Unit> {
        return groupRepository.deleteGroup(id)
    }
}
