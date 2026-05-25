package com.ai.food.recognition.data.repository

import com.ai.food.recognition.data.local.dao.ScriptDao
import com.ai.food.recognition.data.local.entity.Script
import com.ai.food.recognition.domain.repository.ScriptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class ScriptRepositoryImpl(
    private val scriptDao: com.ai.food.recognition.data.local.dao.ScriptDao
) : com.ai.food.recognition.domain.repository.ScriptRepository {

    override suspend fun insertScript(name: String, age: Int) {
        scriptDao.insertScript(_root_ide_package_.com.ai.food.recognition.data.local.entity.Script(name = name))
    }

    override fun getScripts(): Flow<List<com.ai.food.recognition.data.local.entity.Script>> {
        return scriptDao.getAll().map { list ->
            list.map {
                _root_ide_package_.com.ai.food.recognition.data.local.entity.Script(it.id, it.name)
            }
        }
    }
}