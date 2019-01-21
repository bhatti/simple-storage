package com.plexobject.storage


import com.google.common.base.Preconditions
import com.plexobject.storage.graphql.Query
import com.plexobject.storage.graphql.Scalars
import com.plexobject.storage.util.S3Helper
import com.zaxxer.hikari.HikariDataSource
import graphql.schema.GraphQLSchema
import com.coxautodev.graphql.tools.SchemaParser
import com.plexobject.storage.api.GraphQLEndpoint
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.*
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
//@ComponentScan(basePackages = arrayOf("com.plexobject.storage"))
@ComponentScan
@EnableJpaRepositories("com.plexobject.storage.repository")
@PropertySource("classpath:/application.properties")
@ImportResource("classpath:/app-context.xml")
@EnableAutoConfiguration
@EnableMBeanExport
open class StorageConfig : InitializingBean {
    private val logger = LoggerFactory.getLogger(StorageConfig::class.java)

    @PersistenceContext
    lateinit var entityManager: EntityManager
    @Autowired
    lateinit var dataSource: DataSource

    @Value("\${spring.datasource.max-active:30}")
    private var dbMaxActive = 30

    @Value("\${s3.bucket}")
    private val bucket: String = ""
    @Value("\${s3.region:}")
    private val s3Region: String = ""
    @Value("\${s3.endpoint:}")
    private val s3Endpoint: String = ""
    @Value("\${s3.prefix}")
    var s3Prefix = ""
    @Value("\${s3.expirationMinutes}")
    private val expirationMinutes: Long = 60 * 24
    @Value("\${s3.accessKeyId}")
    var s3AccessKeyId = ""
    @Value("\${s3.secretAccessKey}")
    var s3SecretAccessKey = ""
    @Autowired
    lateinit var query: Query
    @Autowired
    lateinit var graphQLEndpoint: GraphQLEndpoint

    @Bean
    open fun graphQLSchema(): GraphQLSchema {
        Preconditions.checkNotNull(query, "query is not defined")
        return SchemaParser.newParser().file("schema.graphqls").resolvers(query).scalars(Scalars.dateTime)
                .build().makeExecutableSchema()
    }

    @Bean
    open fun graphQLServletRegistrationBean(): ServletRegistrationBean<*> {
        return ServletRegistrationBean(graphQLEndpoint, "/api/gq")
    }

    @Bean
    open fun s3Helper(): S3Helper {
        return S3Helper(s3AccessKeyId, s3SecretAccessKey, bucket, s3Region, s3Prefix, s3Endpoint, expirationMinutes)
    }

    override fun afterPropertiesSet() {
        var hds = dataSource as? HikariDataSource
        hds?.let {
            it.minimumIdle = 5
            it.maximumPoolSize = dbMaxActive
            logger.info("JDBC ${it.jdbcUrl}")
        }
    }
}