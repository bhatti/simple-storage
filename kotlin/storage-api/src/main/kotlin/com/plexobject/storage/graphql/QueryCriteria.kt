package com.plexobject.storage.graphql

import com.plexobject.storage.domain.PaginatedQuery
import org.apache.commons.lang3.StringUtils


data class QueryCriteria(
        var pageNumber: Int = 1,
        var pageSize: Int = 20,
        var organization: String? = null,
        var system: String? = null,
        var subsystem: String? = null,
        var name: String? = null,
        var digest: String? = null,
        var platform: String? = null,
        var contentType: String? = null,
        var username: String? = null,
        var userAgent: String? = null,
        var labels: List<String> = listOf(),
        var since: String? = null,
        var sortedBy: List<String> = listOf()) {
    fun toPaginatedQuery(): PaginatedQuery {
        var props = mutableMapOf<String, String>()
        organization?.let {
            props.put("organization", it)
        }
        system?.let {
            props.put("system", it)
        }
        subsystem?.let {
            props.put("subsystem", it)
        }
        name?.let {
            props.put("name", it)
        }
        digest?.let {
            props.put("digest", it)
        }
        platform?.let {
            props.put("platform", it)
        }
        contentType?.let {
            props.put("contentType", it)
        }
        userAgent?.let {
            props.put("userAgent", it)
        }
        username?.let {
            props.put("username", it)
        }
        since?.let {
            props.put("since", it)
        }
        if (labels.size > 0) {
            props.put("labels", StringUtils.join(labels, ','))
        }
        return PaginatedQuery(pageNumber, pageSize, props, sortedBy.toTypedArray())
    }
}