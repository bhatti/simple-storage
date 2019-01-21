package com.plexobject.storage.graphql

import com.plexobject.storage.domain.Artifact
import com.plexobject.storage.domain.PaginatedResult


data class QueryResult(
        var page: Int,
        var pageSize: Int,
        var totalRecords: Long,
        var records: List<ArtifactDTO>) {
    companion object {
        fun from(result: PaginatedResult<Artifact>) : QueryResult {
            val records = result.records.map { ArtifactDTO.from(it) }
            return QueryResult(result.pageNumber, result.pageSize, result.totalRecords, records)
        }
    }
}
