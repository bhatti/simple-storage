package com.plexobject.storage.api

import com.plexobject.storage.StorageConfig
import org.junit.Assert
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URL
import java.nio.charset.Charset
import javax.transaction.Transactional

@Transactional
@RunWith(SpringRunner::class)
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [StorageConfig::class])
@AutoConfigureMockMvc
open class ArtifactControllerTest() {
    @Autowired
    lateinit var controller: ArtifactController
    @Autowired
    private val mvc: MockMvc? = null

    @Test
    fun testUploadWithoutOverwrite() {
        val txt = "My test file 123."
        val data = txt.toByteArray()
        val multipartFile = MockMultipartFile("file", "myfile",
                "text/plain", data)
        val artifact = controller.upload(
                "myapp",
                "myjob",
                false,
                multipartFile,
                listOf("mylabel1, mylabel2", "mylabel3"),
                "myagent",
                mapOf("app" to "newapp", "reportType" to "expenseReport", "author" to "bhatti", "platform" to ""))
        Assert.assertEquals(data.size.toLong(), artifact.size)
        Assert.assertEquals("myapp", artifact.application)
        Assert.assertEquals("myjob", artifact.job)
        Assert.assertEquals("myfile", artifact.name)
        Assert.assertEquals("", artifact.platform)
        Assert.assertEquals("text/plain", artifact.contentType)
        val labels = artifact.labelsAsSet()
        Assert.assertEquals(3, labels.size)
        Assert.assertTrue(labels.contains("mylabel1"))
        Assert.assertTrue(labels.contains("mylabel2"))
        Assert.assertTrue(labels.contains("mylabel3"))
        val props = artifact.getPropertiesAsMap()
        Assert.assertEquals(2, props.size)
        Assert.assertEquals("bhatti", props.get("author"))
        Assert.assertEquals("expenseReport", props.get("reportType"))
        val loaded = URL(artifact.url).readText(Charset.forName("UTF-8"))
        Assert.assertEquals(txt, loaded)

        val resp = controller.download("myapp", "myjob", "myfile")
        Assert.assertEquals(data.size, resp.body.size)
    }


    @Test
    fun testUploadWithOverwrite() {
        val txt = "My test file 123."
        val data = txt.toByteArray()
        val multipartFile = MockMultipartFile("file", "myfile",
                "text/plain", data)
        val artifact = controller.upload(
                "myapp",
                "myjob",
                true,
                multipartFile,
                listOf("mylabel1, mylabel2", "mylabel3"),
                "myagent",
                mapOf("app" to "newapp", "reportType" to "expenseReport", "author" to "bhatti", "platform" to ""))
        Assert.assertEquals(data.size.toLong(), artifact.size)
        Assert.assertEquals("myapp", artifact.application)
        Assert.assertEquals("myjob", artifact.job)
        Assert.assertEquals("myfile", artifact.name)
        Assert.assertEquals("", artifact.platform)
        Assert.assertEquals("text/plain", artifact.contentType)
        val labels = artifact.labelsAsSet()
        Assert.assertEquals(3, labels.size)
        Assert.assertTrue(labels.contains("mylabel1"))
        Assert.assertTrue(labels.contains("mylabel2"))
        Assert.assertTrue(labels.contains("mylabel3"))
        val props = artifact.getPropertiesAsMap()
        Assert.assertEquals(2, props.size)
        Assert.assertEquals("bhatti", props.get("author"))
        Assert.assertEquals("expenseReport", props.get("reportType"))
        val loaded = URL(artifact.url).readText(Charset.forName("UTF-8"))
        Assert.assertEquals(txt, loaded)
        controller.upload(
                "myapp",
                "myjob",
                true,
                multipartFile,
                listOf("mylabel1, mylabel2", "mylabel3"),
                "myagent",
                mapOf("app" to "newapp", "reportType" to "expenseReport", "author" to "bhatti", "platform" to ""))
    }


    @Test
    fun testGet() {
        val txt = "Test file 456."
        val data = txt.toByteArray()
        val multipartFile = MockMultipartFile("file", "testfile",
                "text/plain", data)
        controller.upload(
                "testapp",
                "testjob",
                false,
                multipartFile,
                listOf("testlabel1, testlabel2", "testlabel3"),
                "myagent",
                mapOf("app" to "newapp", "reportType" to "expenseReport", "author" to "john", "platform" to ""))
        val artifact = controller.get("testapp", "testjob", "testfile")
        Assert.assertEquals(data.size.toLong(), artifact.size)
        Assert.assertEquals("testapp", artifact.application)
        Assert.assertEquals("testjob", artifact.job)
        Assert.assertEquals("testfile", artifact.name)
        Assert.assertEquals("", artifact.platform)
        Assert.assertEquals("text/plain", artifact.contentType)
        val labels = artifact.labelsAsSet()
        Assert.assertEquals(3, labels.size)
        Assert.assertTrue(labels.contains("testlabel1"))
        Assert.assertTrue(labels.contains("testlabel2"))
        Assert.assertTrue(labels.contains("testlabel3"))
        val props = artifact.getPropertiesAsMap()
        Assert.assertEquals(2, props.size)
        Assert.assertEquals("john", props.get("author"))
        Assert.assertEquals("expenseReport", props.get("reportType"))
    }


    @Test
    fun testQuery() {
        for (app in arrayOf("qapp1", "qapp2")) {
            for (job in arrayOf("qjob1", "qjob2", "qjob3")) {
                for (k in 1..10) {
                    val platform = if (k % 2 == 1) "IOS" else "ANDROID"
                    val labels = if (k % 2 == 1) listOf("qlabel_${job}_${k}, qlabel2_${k}", "qlabel3") else listOf("plabel_${job}_${k}, plabel2_${k}", "plabel3")
                    val data = "Test file k $k".toByteArray()
                    val multipartFile = MockMultipartFile("file", "file${k}",
                            "text/plain", data)
                    controller.upload(
                            app,
                            job,
                            false,
                            multipartFile,
                            labels,
                            "myagent",
                            mapOf("app" to "newapp", "reportType" to "expenseReport_${app}_${job}_${k}", "author" to "john_${k}", "platform" to platform))
                }
            }
        }
        //
        var result = controller.query(1, 20, listOf(), mapOf())
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(2*3*10, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("app" to "qapp0"))
        Assert.assertEquals(0, result.records.size)
        Assert.assertEquals(0, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("app" to "qapp1"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(30, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("app" to "qapp2"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(30, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("job" to "qjob1"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(20, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("job" to "qjob2"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(20, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("job" to "qjob3"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(20, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("app" to "qapp1", "job" to "qjob1"))
        Assert.assertEquals(10, result.records.size)
        Assert.assertEquals(10, result.totalRecords)
        //
        result = controller.query(1, 20, listOf(), mapOf("app" to "qapp1", "labels" to "Qlabel3"))
        Assert.assertEquals(15, result.records.size)
        Assert.assertEquals(15, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("app" to "qapp1", "labels" to "Qlabel3,plabel3"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(30, result.totalRecords)
        //
        result = controller.query(1, 20, listOf(), mapOf("app" to "qapp1", "platform" to "IOS"))
        Assert.assertEquals(15, result.records.size)
        Assert.assertEquals(15, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("app" to "qapp1", "platform" to "ANDROID"))
        Assert.assertEquals(15, result.records.size)
        Assert.assertEquals(15, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("platform" to "ANDROID"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(30, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("platform" to "LINUX"))
        Assert.assertEquals(0, result.records.size)
        Assert.assertEquals(0, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("userAgent" to "myagent"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(60, result.totalRecords)
    }

    @Test
    fun testJob() {
        for (app in arrayOf("japp1", "japp2")) {
            for (job in arrayOf("jjob1", "jjob2", "jjob3")) {
                for (k in 1..10) {
                    val platform = if (k % 2 == 1) "IOS" else "ANDROID"
                    val labels = if (k % 2 == 1) listOf("qlabel_${job}_${k}, qlabel2_${k}", "qlabel3") else listOf("plabel_${job}_${k}, plabel2_${k}", "plabel3")
                    val data = "Test file k $k".toByteArray()
                    val multipartFile = MockMultipartFile("file", "file${k}",
                            "text/plain", data)
                    controller.upload(
                            app,
                            job,
                            false,
                            multipartFile,
                            labels,
                            "myagent",
                            mapOf("app" to "newapp", "reportType" to "expenseReport_${app}_${job}_${k}", "author" to "john_${k}", "platform" to platform))
                }
            }
        }
        //
        for (app in arrayOf("japp1", "japp2")) {
            for (job in arrayOf("jjob1", "jjob2", "jjob3")) {
                var result = controller.job(app, job)
                Assert.assertEquals(10, result.size)
                result = controller.job(app, job + "x")
                Assert.assertEquals(0, result.size)
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun should404WhenMissingFile() {
        //given(this.storageService.loadAsResource("test.txt"))
        //        .willThrow(StorageFileNotFoundException::class.java)

        this.mvc?.perform(get("/files/test.txt"))?.andExpect(status().isNotFound())
    }

}

