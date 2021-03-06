package com.plexobject.storage.domain

import org.springframework.data.jpa.domain.Specification
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

data class ArtifactSpecification(val pq: PaginatedQuery) : Specification<Artifact> {
    fun and(builder: CriteriaBuilder, first: Predicate?, second: Predicate): Predicate {
        first?.let {
            return builder.and(it, second)
        }
        return second
    }

    fun or(builder: CriteriaBuilder, first: Predicate?, second: Predicate): Predicate {
        first?.let {
            return builder.or(it, second)
        }
        return second
    }

    override fun toPredicate(root: Root<Artifact>, query: CriteriaQuery<*>, builder: CriteriaBuilder): Predicate? {
        var topPredicate: Predicate? = null
        val org: String? = pq.params.get("organization") ?: pq.params.get("org")
        val system: String? = pq.params.get("system")
        val subsystem: String? = pq.params.get("subsystem")
        val name: String? = pq.params.get("name")
        val platform: String? = pq.params.get("platform")
        val userAgent: String? = pq.params.get("userAgent")
        val username: String? = pq.params.get("username")
        val digest: String? = pq.params.get("digest")
        var since: Date? = null
        pq.params.get("since")?.let {
            val accessor = DateTimeFormatter.ISO_DATE_TIME.parse(it)
            since = Date.from(Instant.from(accessor))
            //val dt: LocalDateTime = OffsetDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime()
            //since = Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
        }
        val labels: String? = pq.params.get("labels") ?: pq.params.get("label")

        org?.let {
            topPredicate = and(builder, topPredicate, builder.equal(root.get<String>(Artifact.organization), it))
        }
        system?.let {
            topPredicate = and(builder, topPredicate, builder.equal(root.get<String>(Artifact.system), it))
        }
        subsystem?.let {
            topPredicate = and(builder, topPredicate, builder.equal(root.get<String>(Artifact.subsystem), it))
        }
        userAgent?.let {
            topPredicate = and(builder, topPredicate, builder.equal(root.get<String>(Artifact.userAgent), it))
        }
        username?.let {
            topPredicate = and(builder, topPredicate, builder.equal(root.get<String>(Artifact.username), it))
        }
        platform?.let {
            topPredicate = and(builder, topPredicate, builder.equal(root.get<String>(Artifact.platform), it.toUpperCase()))
        }
        digest?.let {
            topPredicate = and(builder, topPredicate, builder.equal(root.get<String>(Artifact.digest), it.toLowerCase()))
        }
        name?.let {
            topPredicate = and(builder, topPredicate, builder.equal(root.get<String>(Artifact.name), it))
        }
        since?.let {
            topPredicate = and(builder, topPredicate, builder.greaterThanOrEqualTo(root.get<Date>(Artifact.createdAt), it))
        }
        labels?.let {
            val values = it.toLowerCase().split(",").map { it.trim() }.filter { it.isNotEmpty() }
            var labelPredicate: Predicate? = null
            for (i in 0..values.size - 1) {
                val value = values[i]
                labelPredicate = or(builder, labelPredicate, builder.like(root.get<String>(Artifact.labels), "%$value%"))
            }
            topPredicate = and(builder, topPredicate, labelPredicate!!)
        }
        return topPredicate
    }
}
