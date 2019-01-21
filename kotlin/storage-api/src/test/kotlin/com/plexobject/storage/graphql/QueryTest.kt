package com.plexobject.storage.graphql


import com.plexobject.storage.StorageConfig
import com.plexobject.storage.domain.Artifact
import com.plexobject.storage.repository.ArtifactRepository
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.junit4.SpringRunner
import javax.transaction.Transactional


@Transactional
@RunWith(SpringRunner::class)
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [StorageConfig::class])
open class QueryTest {
    @Autowired
    lateinit var repository: ArtifactRepository
    @Autowired
    lateinit var query: Query

    var orgs = arrayOf("org1", "org2")
    var names = arrayOf("name1", "name2")
    var labels = arrayOf("label1", "label2")
    var platforms = arrayOf("IOS", "ANDROID")
    var users = arrayOf("user1", "user2")

    @Before
    fun setup() {
        for (i in 1..100) {
            val label = labels[i % labels.size]
            val platform = platforms[i % platforms.size]
            val org = orgs[i % orgs.size]
            val user = users[i % users.size]
            var artifact = Artifact(
                    organization = org,
                    system = "myapp",
                    subsystem = "myjob_$i",
                    name = names[i % names.size],
                    digest = "mydigest",
                    size = 1000,
                    platform = platform,
                    username = user,
                    contentType = "application/json",
                    userAgent = "firefox",
                    labels = "expenses, quarterly, year$i, $label")
            artifact.addProperties(mapOf("key1$i" to "value1", "key2$i" to "value2", "index" to "$i"))
            repository.save(artifact, true)
        }
    }

    @Test
    fun testQueryNull() {
        val result = query.query(null)
        Assert.assertEquals(100, result.totalRecords)
    }


    @Test
    fun testQueryEmpty() {
        val result = query.query(QueryCriteria())
        Assert.assertEquals(100, result.totalRecords)
    }

    @Test
    fun testQueryByOrg() {
        val result = query.query(QueryCriteria(organization = "org1"))
        Assert.assertEquals(50, result.totalRecords)
    }

    @Test
    fun testQueryOrgUsername() {
        val result = query.query(QueryCriteria(organization = "org1", username = "user1"))
        Assert.assertEquals(50, result.totalRecords)
    }

    @Test
    fun testQueryOrgSystem() {
        val result = query.query(QueryCriteria(organization = "org1", system = "myapp"))
        Assert.assertEquals(50, result.totalRecords)
    }

    @Test
    fun testQueryOrgSystemName() {
        val result = query.query(QueryCriteria(organization = "org1", system = "myapp", name = "name1"))
        Assert.assertEquals(50, result.totalRecords)
    }

}

