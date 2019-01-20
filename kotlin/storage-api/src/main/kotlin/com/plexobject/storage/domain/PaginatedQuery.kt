package com.plexobject.storage.domain

import org.springframework.data.domain.Sort
import org.springframework.lang.Nullable
import java.util.*

data class PaginatedQuery(
        val params: Map<String, String>,
        val pageNumber: Int = 1,
        val pageSize: Int = 20,
        val sortProperties: Array<String> = arrayOf()) {
    fun toSort() : Sort {
        var order = mutableListOf<Sort.Order>()
        for (prop in sortProperties) {
            val propToks = prop.split(" ").map { it.trim() }.filter { it.isNotEmpty() }
            var dir: Optional<Sort.Direction> = Optional.empty()
            if (propToks.size >= 2) {
                dir = Sort.Direction.fromOptionalString(propToks.get(1).toUpperCase())
            }
            if (propToks.size >= 1) {
                if (dir.isPresent) {
                    order.add(Sort.Order(dir.get(), propToks.get(0)))
                } else {
                    order.add(Sort.Order(Sort.Direction.ASC, propToks.get(0)))
                }
            }
        }
        return Sort.by(order)
    }
}