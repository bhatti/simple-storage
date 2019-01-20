package com.plexobject.storage.repository

import com.plexobject.storage.domain.Artifact
import com.plexobject.storage.domain.PaginatedQuery
import com.plexobject.storage.domain.PaginatedResult
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.stereotype.Repository

//@Repository
@NoRepositoryBean
interface ArtifactRepository {
    fun get(application: String, job: String, name: String): Artifact
    fun save(entity: Artifact, overwrite: Boolean): Artifact
    fun saveProperties(application: String, job: String, name: String, params: Map<String, String>): Artifact
    fun query(query: PaginatedQuery): PaginatedResult<Artifact>
    fun findAll(): List<Artifact>
}