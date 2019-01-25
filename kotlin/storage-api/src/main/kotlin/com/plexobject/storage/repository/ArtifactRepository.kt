package com.plexobject.storage.repository

import com.plexobject.storage.domain.Artifact
import com.plexobject.storage.domain.PaginatedQuery
import com.plexobject.storage.domain.PaginatedResult
import org.springframework.data.repository.NoRepositoryBean

//@Repository
@NoRepositoryBean
interface ArtifactRepository {
    fun get(id: String): Artifact
    fun delete(id: String): Artifact
    fun save(entity: Artifact, overwrite: Boolean): Artifact
    fun saveProperties(id: String, labels: String, params: Map<String, String>): Artifact
    fun query(query: PaginatedQuery): PaginatedResult<Artifact>
    fun findAll(): List<Artifact>
}