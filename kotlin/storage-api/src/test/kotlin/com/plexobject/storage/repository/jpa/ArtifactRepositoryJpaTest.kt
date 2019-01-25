package com.plexobject.storage.repository.jpa

import com.plexobject.storage.StorageConfig
import com.plexobject.storage.domain.Artifact
import com.plexobject.storage.domain.DuplicateException
import com.plexobject.storage.domain.PaginatedQuery
import com.plexobject.storage.repository.ArtifactRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDate
import java.time.Month
import javax.transaction.Transactional


@Transactional
@RunWith(SpringRunner::class)
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [StorageConfig::class])
open class ArtifactRepositoryJpaTest() {
    @Autowired
    lateinit var repository: ArtifactRepository
    var names = arrayOf("name1", "name2")
    var labels = arrayOf("label1", "label2")
    var platforms = arrayOf("IOS", "ANDROID")
    var users = arrayOf("user1", "user2")
    val jan1 = LocalDate.of(2019, Month.JANUARY, 1)
    //val feb1 = LocalDateTime.of(2019, Month.FEBRUARY, 1, 0, 0, 0)

    @Before
    fun setup() {
        for (i in 1..100) {
            val label = labels[i % labels.size]
            val platform = platforms[i % platforms.size]
            val user = users[i % users.size]
            var artifact = Artifact(
                    organization = "myorg",
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
            artifact.createdAt = java.sql.Date.valueOf(jan1.plusDays(i.toLong()))
            artifact.addProperties(mapOf("key1$i" to "value1", "key2$i" to "value2", "index" to "$i"))
            repository.save(artifact, true)
        }
    }

    @Test
    fun createArtifact() {
        var artifact = Artifact(
                organization = "org",
                system = "system",
                subsystem = "job",
                name = "IOS_APP",
                digest = "ios-app-digest",
                size = 1000,
                platform = "IOS",
                username = "user",
                contentType = "application/json",
                userAgent = "firefox",
                labels = "new_expenses, new_quarterly, new_2019 ")
        //
        assertNotNull(artifact.id)
        assertEquals(Artifact.stringToSet("new_2019, new_expenses, new_quarterly").toString(), artifact.labelsAsSet().toString())
    }

    @Test
    fun testSave() {
        var artifact = Artifact(
                organization = "neworg",
                system = "newsystem",
                subsystem = "newjob",
                name = "IOS_APP",
                digest = "ios-app-digest",
                size = 1000,
                platform = "IOS",
                username = "newuser",
                contentType = "application/json",
                userAgent = "firefox",
                labels = "new_expenses, new_quarterly, new_2019 ")
        //
        artifact.addProperties(mapOf("zkey1" to "value1", "zkey2" to "value2"))
        //
        repository.save(artifact, true)
        repository.save(artifact, true) // multiple save should work

        val it = repository.get(artifact.id)
        assertArtifact(it, artifact)
    }


    @Test
    fun testDelete() {
        var artifact = Artifact(
                organization = "delorg",
                system = "delsystem",
                subsystem = "deljob",
                name = "IOS_APP",
                digest = "ios-app-digest",
                size = 1000,
                platform = "IOS",
                username = "deluser",
                contentType = "application/json",
                userAgent = "firefox",
                labels = "del_expenses, del_quarterly, del_2019 ")
        //
        val props = mapOf("system" to "delsystem")
        repository.save(artifact, false)
        var result = repository.query(PaginatedQuery(1, 20, props))
        assertEquals(1, result.records.size)
        assertEquals(1, result.totalRecords)
        val it = repository.delete(artifact.id)
        result = repository.query(PaginatedQuery(1, 20, props))
        assertEquals(0, result.records.size)
        assertEquals(0, result.totalRecords)
    }

    @Test(expected = DuplicateException::class)
    fun testSavePropertiesWithoutOverwrite() {
        var artifact = Artifact(
                organization = "xorg",
                system = "xsystem",
                subsystem = "xjob",
                name = "IOS_APP",
                digest = "ios-app-digest",
                size = 1000,
                platform = "IOS",
                username = "xuser",
                contentType = "application/json",
                userAgent = "firefox",
                labels = "new_expenses, new_quarterly, new_2019 ")
        assertEquals(Artifact.stringToSet("new_expenses, new_quarterly, new_2019 ").toString(), artifact.labelsAsSet().toString())
        artifact.addProperties(mapOf("zkey1" to "value1", "zkey2" to "value2"))
        //
        repository.save(artifact, false)
        repository.save(artifact, false)
    }


    @Test
    fun testSavePropertiesWithOverwrite() {
        var artifact = Artifact(
                organization = "yorg",
                system = "ysystem",
                subsystem = "yjob",
                name = "IOS_APP",
                digest = "ios-app-digest",
                size = 1000,
                platform = "IOS",
                username = "yuser",
                contentType = "application/json",
                userAgent = "firefox",
                labels = "new_expenses, new_quarterly, new_2019 ")
        //
        assertEquals(Artifact.stringToSet("new_expenses, new_quarterly, new_2019 ").toString(), artifact.labelsAsSet().toString())
        artifact.addProperties(mapOf("zkey1" to "value1", "zkey2" to "value2"))
        //
        repository.save(artifact, true)
        repository.save(artifact, true) // multiple save should work

        var it = repository.get(artifact.id)
        assertArtifact(it, artifact)
        var props = it.getPropertiesAsMap()
        assertEquals("value1", props.get("zkey1"))
        assertEquals("value2", props.get("zkey2"))
        assertEquals(2, it.getPropertiesAsMap().size)
        assertEquals(3, it.labelsAsSet().size)

        // update labels/properties
        artifact.mergeLabels(Artifact.stringToSet("new_check,new_month"))
        assertEquals(Artifact.stringToSet("new_expenses, new_quarterly, new_2019,new_check,new_month ").toString(), artifact.labelsAsSet().toString())
        artifact.addProperties(mapOf("xkey1" to "value1", "xkey2" to "value2"))
        repository.save(artifact, true) // multiple save should work

        it = repository.get(artifact.id)
        assertArtifact(it, artifact)
        props = it.getPropertiesAsMap()
        assertEquals("value1", props.get("xkey1"))
        assertEquals("value2", props.get("xkey2"))
        assertEquals(4, it.getPropertiesAsMap().size)
        assertEquals(5, it.labelsAsSet().size)
        //
        repository.saveProperties(artifact.id, "brand1,brand2", mapOf("ykey1" to "value1", "ykey2" to "value2"))
        artifact.labels = "new_quarterly,brand2,new_month,brand1,new_expenses,new_2019,new_check"
        it = repository.get(artifact.id)
        assertArtifact(it, artifact)
        props = it.getPropertiesAsMap()
        assertEquals("value1", props.get("ykey1"))
        assertEquals("value2", props.get("ykey2"))
        assertEquals(6, it.getPropertiesAsMap().size)
        assertEquals(7, it.labelsAsSet().size)
    }


    @Test
    fun testFindAll() {
        val all = repository.findAll()
        assertEquals(100, all.size)
    }

    @Test
    fun testQueryBySystem() {
        val props = mapOf("system" to "myapp")
        val result = repository.query(PaginatedQuery(1, 20, props))
        assertEquals(20, result.records.size)
        assertEquals(100, result.totalRecords)
        assertEquals("1", result.records.first().getPropertiesAsMap().get("index"))
        assertEquals("20", result.records.last().getPropertiesAsMap().get("index"))
    }

    @Test
    fun testQueryByAppPage2() {
        val props = mapOf("system" to "myapp")
        val result = repository.query(PaginatedQuery(2, 20, props))
        assertEquals(20, result.records.size)
        assertEquals(100, result.totalRecords)
        assertEquals("21", result.records.first().getPropertiesAsMap().get("index"))
        assertEquals("40", result.records.last().getPropertiesAsMap().get("index"))
    }

    @Test
    fun testQueryByAppAndPlatform() {
        val props = mapOf("system" to "myapp", "platform" to "IOS")
        val result = repository.query(PaginatedQuery(1, 200, props, arrayOf("system DESC")))
        assertEquals(50, result.records.size)
        assertEquals(50, result.totalRecords)
    }

    @Test
    fun testQueryByAppAndName() {
        val props = mapOf("system" to "myapp", "name" to "name1")
        val result = repository.query(PaginatedQuery(1, 200, props, arrayOf("system DESC")))
        assertEquals(50, result.records.size)
        assertEquals(50, result.totalRecords)
    }

    @Test
    fun testQueryByAppAndPlatformAndName() {
        val props = mapOf("system" to "myapp", "platform" to "IOS", "name" to "name1")
        val result = repository.query(PaginatedQuery(1, 200, props, arrayOf("system DESC")))
        assertEquals(50, result.records.size)
        assertEquals(50, result.totalRecords)
    }

    @Test
    fun testQueryByAppAndPlatformAndNameNotFound() {
        val props = mapOf("system" to "myapp", "platform" to "IOS", "name" to "name2")
        val result = repository.query(PaginatedQuery(1, 200, props, arrayOf("system DESC")))
        assertEquals(0, result.records.size)
        assertEquals(0, result.totalRecords)
    }

    @Test
    fun testQueryByAppAndPlatformAndNameAndLabel() {
        val props = mapOf("system" to "myapp", "platform" to "IOS", "name" to "name1", "label" to "label1")
        val result = repository.query(PaginatedQuery(1, 200, props, arrayOf("system DESC")))
        assertEquals(50, result.records.size)
        assertEquals(50, result.totalRecords)
    }

    @Test
    fun testQueryByAppAndLabels() {
        val props = mapOf("system" to "myapp", "label" to "Label1, Label2")
        val result = repository.query(PaginatedQuery(1, 200, props, arrayOf("system DESC")))
        assertEquals(100, result.records.size)
        assertEquals(100, result.totalRecords)
    }

    @Test
    fun testQueryByAppAndSince() {
        val since = "2019-01-03T00:00:00+05:00"
        val props = mapOf("system" to "myapp", "since" to since)
        val result = repository.query(PaginatedQuery(1, 200, props, arrayOf("system DESC")))
        assertEquals(99, result.records.size)
        assertEquals(99, result.totalRecords)
    }

    @Test
    fun testQueryByAppAndSinceFeb() {
        //val props = mapOf("system" to "myapp", "since" to feb1.format(DateTimeFormatter.ISO_DATE_TIME))
        val since = "2019-02-01T00:00:00+05:00"
        val props = mapOf("system" to "myapp", "since" to since)
        val result = repository.query(PaginatedQuery(1, 20, props, arrayOf("system DESC")))
        assertEquals(20, result.records.size)
        assertEquals(70, result.totalRecords)
    }

    fun assertArtifact(first: Artifact, second: Artifact) {
        if (first === second) return
        check(first.size == second.size) { "size $first.size didn't match $second.size." }
        check(first.organization.equals(second.organization)) { "organization $first.organization didn't match $second.organization." }
        check(first.system.equals(second.system)) { "system $first.system didn't match $second.system." }
        check(first.subsystem.equals(second.subsystem)) { "subsystem $first.subsystem didn't match $second.subsystem." }
        check(first.name.equals(second.name)) { "name $first.name didn't match $second.name." }
        check(first.digest.equals(second.digest)) { "digest $first.digest didn't match $second.digest." }
        check(first.platform.equals(second.platform)) { "platform $first.platform didn't match $second.platform." }
        check(first.contentType.equals(second.contentType)) { "contentType $first.contentType didn't match $second.contentType." }
        check(first.userAgent.equals(second.userAgent)) { "userAgent $first.userAgent didn't match $second.userAgent." }
        check(first.labelsAsSet().toString().equals(second.labelsAsSet().toString())) { "labels ${first.labels} didn't match ${second.labels}." }
        //check(first.url.equals(second.url)) { "url $first.url didn't match $second.url" }
        //check(first.getPropertiesMap().toString().equals(second.getPropertiesMap().toString())) { "properties $first.properties didn't match $second.properties." }
    }
}

