package com.plexobject.storage.api

import com.plexobject.storage.graphql.SanitizedError
import graphql.ExceptionWhileDataFetching
import graphql.GraphQLError
import graphql.schema.GraphQLSchema
import graphql.servlet.DefaultExecutionStrategyProvider
import graphql.servlet.GraphQLSchemaProvider
import graphql.servlet.SimpleGraphQLServlet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest


//@WebServlet(urlPatterns = arrayOf("/api/gq"))
@Component
open class GraphQLEndpoint : SimpleGraphQLServlet(
        null as GraphQLSchemaProvider?,
        DefaultExecutionStrategyProvider(),
        null,
        null), GraphQLSchemaProvider {
    @Autowired
    lateinit var graphQLSchema: GraphQLSchema

    override fun getSchemaProvider(): GraphQLSchemaProvider {
        return this
    }

    override fun filterGraphQLErrors(errors: List<GraphQLError>): List<GraphQLError> {
        return errors.filter { e -> e is ExceptionWhileDataFetching || super.isClientError(e) }.map { e -> if (e is ExceptionWhileDataFetching) SanitizedError(e) else e }
    }

    override  fun getSchema(request: HttpServletRequest): GraphQLSchema {
        return graphQLSchema
    }

    override fun getSchema(): GraphQLSchema {
        return graphQLSchema
    }

    override  fun getReadOnlySchema(request: HttpServletRequest): GraphQLSchema {
        return graphQLSchema
    }
}
