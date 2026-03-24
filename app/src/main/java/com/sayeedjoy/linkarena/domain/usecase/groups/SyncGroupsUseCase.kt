package com.sayeedjoy.linkarena.domain.usecase.groups

import com.sayeedjoy.linkarena.domain.repository.GroupRepository
import javax.inject.Inject

class SyncGroupsUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke() = groupRepository.syncGroups()
}
