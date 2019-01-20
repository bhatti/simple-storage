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
    val jan1 = LocalDate.of(2019, Month.JANUARY, 1)
    //val feb1 = LocalDateTime.of(2019, Month.FEBRUARY, 1, 0, 0, 0)

    @Before
    fun setup() {
        for (i in 1..100) {
            val label = labels[i % labels.size]
            val platform = platforms[i % platforms.size]
            var artifact1 = Artifact(
                    "myapp",
                    "myjob_$i",
                    names[i % names.size],
                    "pdf_report",
                    platform,
                    "application/json",
                    1000,
                    "http://google.com",
                    "firefox",
                    "expenses, quarterly, year$i, $label")
            artifact1.createdAt = java.sql.Date.valueOf(jan1.plusDays(i.toLong()))
            artifact1.addProperties(mapOf("key1$i" to "value1", "key2$i" to "value2", "index" to "$i"))
            repository.save(artifact1, true)
        }
    }

    @Test
    fun createArtifact() {
        val artifact1 = Artifact(
                "app",
                "job1",
                "MAC",
                "report",
                "111",
                "application/json",
                1112,
                "http://gooogle.com",
                "firefox",
                "expenses, quarterly, 2019 ")
        assertEquals("app/job1/MAC", artifact1.getId())
        assertEquals(Artifact.stringToSet("expenses, quarterly, 2019").toString(), artifact1.labelsAsSet().toString())
    }

    @Test
    fun testSave() {
        var artifact1 = Artifact(
                "newapp",
                "newjob",
                "IOS_APP",
                "ios-app-digest",
                "IOS",
                "application/json",
                1000,
                "https://google.com",
                "firefox",
                "new_expenses, new_quarterly, new_2019 ")
        //
        artifact1.addProperties(mapOf("zkey1" to "value1", "zkey2" to "value2"))
        //
        repository.save(artifact1, true)
        repository.save(artifact1, true) // multiple save should work

        val loadArtifact1 = repository.get(artifact1.application, artifact1.job, artifact1.name)
        assertNotNull(loadArtifact1)
        loadArtifact1?.let {
            assertArtifact(it, artifact1)
        }
    }

    @Test(expected=DuplicateException::class)
    fun testSavePropertiesWithoutOverwrite() {
        var artifact1 = Artifact(
                "newapp",
                "newjob",
                "IOS_APP",
                "ios-app-digest",
                "IOS",
                "application/json",
                1000,
                "https://google.com",
                "firefox",
                "new_expenses, new_quarterly, new_2019 ")
        //
        assertEquals(Artifact.stringToSet("new_expenses, new_quarterly, new_2019 ").toString(), artifact1.labelsAsSet().toString())
        artifact1.addProperties(mapOf("zkey1" to "value1", "zkey2" to "value2"))
        //
        repository.save(artifact1, false)
        repository.save(artifact1, false)
    }


    @Test
    fun testSavePropertiesWithOverwrite() {
        var artifact1 = Artifact(
                "newapp",
                "newjob",
                "IOS_APP",
                "ios-app-digest",
                "IOS",
                "application/json",
                1000,
                "https://google.com",
                "firefox",
                "new_expenses, new_quarterly, new_2019 ")
        //
        assertEquals(Artifact.stringToSet("new_expenses, new_quarterly, new_2019 ").toString(), artifact1.labelsAsSet().toString())
        artifact1.addProperties(mapOf("zkey1" to "value1", "zkey2" to "value2"))
        //
        repository.save(artifact1, true)
        repository.save(artifact1, true) // multiple save should work

        var it = repository.get(artifact1.application, artifact1.job, artifact1.name)
        assertArtifact(it, artifact1)
        var props = it.getPropertiesAsMap()
        assertEquals("value1", props.get("zkey1"))
        assertEquals("value2", props.get("zkey2"))
        assertEquals(2, it.getPropertiesAsMap().size)
        assertEquals(3, it.labelsAsSet().size)

        // update labels/properties
        artifact1.mergeLabels(Artifact.stringToSet("new_check,new_month"))
        assertEquals(Artifact.stringToSet("new_expenses, new_quarterly, new_2019,new_check,new_month ").toString(), artifact1.labelsAsSet().toString())
        artifact1.addProperties(mapOf("xkey1" to "value1", "xkey2" to "value2"))
        repository.save(artifact1, true) // multiple save should work

        it = repository.get(artifact1.application, artifact1.job, artifact1.name)
        assertArtifact(it, artifact1)
        props = it.getPropertiesAsMap()
        assertEquals("value1", props.get("xkey1"))
        assertEquals("value2", props.get("xkey2"))
        assertEquals(4, it.getPropertiesAsMap().size)
        assertEquals(5, it.labelsAsSet().size)
        //
        repository.saveProperties(artifact1.application, artifact1.job, artifact1.name, mapOf("ykey1" to "value1", "ykey2" to "value2"))
        it = repository.get(artifact1.application, artifact1.job, artifact1.name)
        assertArtifact(it, artifact1)
        props = it.getPropertiesAsMap()
        assertEquals("value1", props.get("ykey1"))
        assertEquals("value2", props.get("ykey2"))
        assertEquals(6, it.getPropertiesAsMap().size)
        assertEquals(5, it.labelsAsSet().size)
    }


    @Test
    fun testFindAll() {
        val all = repository.findAll()
        assertEquals(100, all.size)
    }

    @Test
    fun testQueryByAppp() {
        val props = mapOf("application" to "myapp")
        val result = repository.query(PaginatedQuery(props, 1, 20))
        assertEquals(20, result.records.size)
        assertEquals(100, result.totalRecords)
        assertEquals("1", result.records.first().getPropertiesAsMap().get("index"))
        assertEquals("20", result.records.last().getPropertiesAsMap().get("index"))
    }

    @Test
    fun testQueryByAppPage2() {
        val props = mapOf("application" to "myapp")
        val result = repository.query(PaginatedQuery(props, 2, 20))
        assertEquals(20, result.records.size)
        assertEquals(100, result.totalRecords)
        assertEquals("21", result.records.first().getPropertiesAsMap().get("index"))
        assertEquals("40", result.records.last().getPropertiesAsMap().get("index"))
    }

    @Test
    fun testQueryByAppAndPlatform() {
        val props = mapOf("application" to "myapp", "platform" to "IOS")
        val result = repository.query(PaginatedQuery(props, 1, 200, arrayOf("job DESC")))
        assertEquals(50, result.records.size)
        assertEquals(50, result.totalRecords)
    }

    @Test
    fun testQueryByAppAndName() {
        val props = mapOf("application" to "myapp", "name" to "name1")
        val result = repository.query(PaginatedQuery(props, 1, 200, arrayOf("job DESC")))
        assertEquals(50, result.records.size)
        assertEquals(50, result.totalRecords)
    }

    @Test
    fun testQueryByAppAndPlatformAndName() {
        val props = mapOf("application" to "myapp", "platform" to "IOS", "name" to "name1")
        val result = repository.query(PaginatedQuery(props, 1, 200, arrayOf("job DESC")))
        assertEquals(50, result.records.size)
        assertEquals(50, result.totalRecords)
    }

    @Test
    fun testQueryByAppAndPlatformAndNameNotFound() {
        val props = mapOf("application" to "myapp", "platform" to "IOS", "name" to "name2")
        val result = repository.query(PaginatedQuery(props, 1, 200, arrayOf("job DESC")))
        assertEquals(0, result.records.size)
        assertEquals(0, result.totalRecords)
    }

    @Test
    fun testQueryByAppAndPlatformAndNameAndLabel() {
        val props = mapOf("application" to "myapp", "platform" to "IOS", "name" to "name1", "label" to "label1")
        val result = repository.query(PaginatedQuery(props, 1, 200, arrayOf("job DESC")))
        assertEquals(50, result.records.size)
        assertEquals(50, result.totalRecords)
    }

    @Test
    fun testQueryByAppAndLabels() {
        val props = mapOf("application" to "myapp", "label" to "Label1, Label2")
        val result = repository.query(PaginatedQuery(props, 1, 200, arrayOf("job DESC")))
        assertEquals(100, result.records.size)
        assertEquals(100, result.totalRecords)
    }

    @Test
    fun testQueryByAppAndSince() {
        val since = "2019-01-03T00:00:00+05:00"
        val props = mapOf("application" to "myapp", "since" to since)
        val result = repository.query(PaginatedQuery(props, 1, 200, arrayOf("job DESC")))
        assertEquals(99, result.records.size)
        assertEquals(99, result.totalRecords)
    }

    @Test
    fun testQueryByAppAndSinceFeb() {
        //val props = mapOf("application" to "myapp", "since" to feb1.format(DateTimeFormatter.ISO_DATE_TIME))
        val since = "2019-02-01T00:00:00+05:00"
        val props = mapOf("application" to "myapp", "since" to since)
        val result = repository.query(PaginatedQuery(props, 1, 20, arrayOf("job DESC")))
        assertEquals(20, result.records.size)
        assertEquals(70, result.totalRecords)
    }

    fun assertArtifact(first: Artifact, second: Artifact) {
        if (first === second) return
        check(first.size == second.size) { "size $first.size didn't match $second.size." }
        check(first.application.equals(second.application)) { "application $first.application didn't match $second.application." }
        check(first.job.equals(second.job)) { "job $first.job didn't match $second.job." }
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

