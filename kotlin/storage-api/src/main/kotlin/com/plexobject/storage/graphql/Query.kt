package com.plexobject.storage.graphql

import com.coxautodev.graphql.tools.GraphQLRootResolver
import com.plexobject.storage.repository.ArtifactRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class Query : GraphQLRootResolver {
    @Autowired
    lateinit var artifactRepository: ArtifactRepository

    fun query(afilter: QueryCriteria?): QueryResult {
        var filter = afilter
        if (filter == null) {
            filter = QueryCriteria()
        }

        try {
            val result = artifactRepository.query(filter.toPaginatedQuery())
            return QueryResult.from(result)
        } catch (e: RuntimeException) {
            logger.error("Failed to find pending jobs", e)
            throw e
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Query::class.java)
    }
}

