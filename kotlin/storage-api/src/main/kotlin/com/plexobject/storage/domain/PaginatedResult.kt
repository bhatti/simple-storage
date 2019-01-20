package com.plexobject.storage.domain

data class PaginatedResult<T>(
        val pageNumber: Int,
        val pageSize: Int,
        val totalRecords: Long,
        val records: List<T>
    ) {
}