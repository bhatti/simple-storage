package com.plexobject.storage.domain


data class PaginatedResult<T>(
        var pageNumber: Int,
        var pageSize: Int,
        var totalRecords: Long,
        var records: List<T>
) {
}
