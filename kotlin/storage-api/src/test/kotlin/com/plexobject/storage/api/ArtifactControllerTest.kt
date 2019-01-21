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
                org = "myorg",
                system = "myapp",
                subsystem = "myjob",
                overwrite = false,
                file = multipartFile,
                labelList = listOf("mylabel1, mylabel2", "mylabel3"),
                username = "myuser",
                userAgent = "myagent",
                params = mapOf("org" to "newapp", "reportType" to "expenseReport", "author" to "bhatti", "platform" to ""))
        Assert.assertEquals(data.size.toLong(), artifact.size)
        Assert.assertEquals("myorg", artifact.organization)
        Assert.assertEquals("myapp", artifact.system)
        Assert.assertEquals("myjob", artifact.subsystem)
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
        val loaded = URL(controller.s3Helper.presignedUpload(artifact.id).toString()).readText(Charset.forName("UTF-8"))
        Assert.assertEquals(txt, loaded)

        val resp = controller.download(artifact.id)
        Assert.assertEquals(data.size, resp.body?.size)
    }


    @Test
    fun testUploadWithOverwrite() {
        val txt = "My test file 123."
        val data = txt.toByteArray()
        val multipartFile = MockMultipartFile("file", "myfile",
                "text/plain", data)
        val artifact = controller.upload(
                org = "myorg",
                system = "myapp",
                subsystem = "myjob",
                overwrite = true,
                file = multipartFile,
                labelList = listOf("mylabel1, mylabel2", "mylabel3"),
                username = "myuser",
                userAgent = "myagent",
                params = mapOf("org" to "newapp", "reportType" to "expenseReport", "author" to "bhatti", "platform" to ""))
        Assert.assertEquals(data.size.toLong(), artifact.size)
        Assert.assertEquals("myorg", artifact.organization)
        Assert.assertEquals("myapp", artifact.system)
        Assert.assertEquals("myjob", artifact.subsystem)
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
        val loaded = URL(controller.s3Helper.presignedUpload(artifact.id).toString()).readText(Charset.forName("UTF-8"))
        Assert.assertEquals(txt, loaded)
        controller.upload(
                org = "myorg",
                system = "myapp",
                subsystem = "myjob",
                overwrite = true,
                file = multipartFile,
                labelList = listOf("mylabel1, mylabel2", "mylabel3"),
                username = "myuser",
                userAgent = "myagent",
                params = mapOf("org" to "newapp", "reportType" to "expenseReport", "author" to "bhatti", "platform" to ""))
    }


    @Test
    fun testGet() {
        val txt = "Test file 456."
        val data = txt.toByteArray()
        val multipartFile = MockMultipartFile("file", "testfile",
                "text/plain", data)
        val saved = controller.upload(
                org = "testorg",
                system = "testapp",
                subsystem = "testjob",
                overwrite = false,
                file = multipartFile,
                labelList = listOf("testlabel1, testlabel2", "testlabel3"),
                username = "myuser",
                userAgent = "myagent",
                params = mapOf("org" to "newapp", "reportType" to "expenseReport", "author" to "john", "platform" to ""))
        val artifact = controller.get(saved.id)
        Assert.assertEquals(data.size.toLong(), artifact.size)
        Assert.assertEquals("testorg", artifact.organization)
        Assert.assertEquals("testapp", artifact.system)
        Assert.assertEquals("testjob", artifact.subsystem)
        Assert.assertEquals("testfile", artifact.name)
        Assert.assertEquals("", artifact.platform)
        Assert.assertEquals("text/plain", artifact.contentType)
        val labels = artifact.labelsAsSet()
        Assert.assertEquals(3, labels.size)
        Assert.assertTrue(labels.contains("testlabel1"))
        Assert.assertTrue(labels.contains("testlabel2"))
        Assert.assertTrue(labels.contains("testlabel3"))
        val props = artifact.getPropertiesAsMap()
        Assert.assertEquals(props.toString(), 2, props.size)
        Assert.assertEquals("john", props.get("author"))
        Assert.assertEquals("expenseReport", props.get("reportType"))
    }

    @Test
    fun testSaveProperties() {
        val txt = "Test file 456."
        val data = txt.toByteArray()
        val multipartFile = MockMultipartFile("file", "testfile",
                "text/plain", data)
        val saved = controller.upload(
                org = "testorg",
                system = "testapp",
                subsystem = "testjob",
                overwrite = false,
                file = multipartFile,
                labelList = listOf("testlabel1, testlabel2", "testlabel3"),
                username = "myuser",
                userAgent = "myagent",
                params = mapOf("org" to "newapp", "reportType" to "expenseReport", "author" to "john", "platform" to ""))
        var artifact = controller.get(saved.id)
        Assert.assertEquals(data.size.toLong(), artifact.size)
        var labels = artifact.labelsAsSet()
        Assert.assertEquals(3, labels.size)
        var props = artifact.getPropertiesAsMap()
        Assert.assertEquals(props.toString(), 2, props.size)
        //
        artifact = controller.saveProperties(saved.id, listOf("brand1,brand2", "brand3"), mapOf("appId" to "2434"))
        labels = artifact.labelsAsSet()
        Assert.assertEquals(6, labels.size)
        props = artifact.getPropertiesAsMap()
        Assert.assertEquals(props.toString(), 3, props.size)
    }


    @Test
    fun testQuery() {
        for (org in arrayOf("org1", "org2")) {
            for (system in arrayOf("system1", "system2")) {
                for (subsystem in arrayOf("subsys1", "subsys2")) {
                    for (k in 1..6) {
                        val platform = if (k % 2 == 1) "IOS" else "ANDROID"
                        val labels = if (k % 2 == 1) listOf("qlabel_${system}_${k}, qlabel2_${k}", "qlabel3") else listOf("plabel_${system}_${k}, plabel2_${k}", "plabel3")
                        val data = "Test file k $k".toByteArray()
                        val multipartFile = MockMultipartFile("file", "file${k}",
                                "text/plain", data)
                        controller.upload(
                                org = org,
                                system = system,
                                subsystem = subsystem,
                                overwrite = false,
                                file = multipartFile,
                                labelList = labels,
                                username = "myuser",
                                userAgent = "myagent",
                                params = mapOf("org" to "newapp", "reportType" to "expenseReport_${system}_${subsystem}_${k}", "author" to "john_${k}", "platform" to platform))
                    }
                }
            }
        }
        //
        var result = controller.query(1, 20, listOf(), mapOf())
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(2*2*2*6, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("org" to "org1"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(24, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("org" to "org2"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(24, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("system" to "system1"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(24, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("system" to "system2"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(24, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("subsystem" to "subsys1"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(24, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("subsystem" to "subsys1"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(24, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("org" to "org1", "system" to "system1"))
        Assert.assertEquals(12, result.records.size)
        Assert.assertEquals(12, result.totalRecords)
        //
        result = controller.query(1, 20, listOf(), mapOf("org" to "org2", "system" to "system2"))
        Assert.assertEquals(12, result.records.size)
        Assert.assertEquals(12, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("org" to "org1", "labels" to "Qlabel3,plabel3"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(24, result.totalRecords)
        //
        result = controller.query(1, 20, listOf(), mapOf("org" to "org2", "platform" to "IOS"))
        Assert.assertEquals(12, result.records.size)
        Assert.assertEquals(12, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("org" to "org2", "platform" to "ANDROID"))
        Assert.assertEquals(12, result.records.size)
        Assert.assertEquals(12, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("platform" to "ANDROID"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(24, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("platform" to "LINUX"))
        Assert.assertEquals(0, result.records.size)
        Assert.assertEquals(0, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("userAgent" to "myagent"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(48, result.totalRecords)
        result = controller.query(1, 20, listOf(), mapOf("username" to "myuser"))
        Assert.assertEquals(20, result.records.size)
        Assert.assertEquals(48, result.totalRecords)
    }

    @Test
    fun testSubsystem() {
        for (org in arrayOf("xorg1", "xorg2")) {
            for (system in arrayOf("xsystem1", "xsystem2")) {
                for (subsystem in arrayOf("xsubsys1", "xsubsys2")) {
                    for (k in 1..4) {
                        val platform = if (k % 2 == 1) "IOS" else "ANDROID"
                        val labels = if (k % 2 == 1) listOf("qlabel_${system}_${k}, qlabel2_${k}", "qlabel3") else listOf("plabel_${system}_${k}, plabel2_${k}", "plabel3")
                        val data = "Test file k $k".toByteArray()
                        val multipartFile = MockMultipartFile("file", "file${k}",
                                "text/plain", data)
                        controller.upload(
                                org = org,
                                system = system,
                                subsystem = subsystem,
                                overwrite = false,
                                file = multipartFile,
                                labelList = labels,
                                username = "myuser",
                                userAgent = "myagent",
                                params = mapOf("org" to "newapp", "reportType" to "expenseReport_${system}_${subsystem}_${k}", "author" to "john_${k}", "platform" to platform))
                    }
                }
            }
        }
        //
        for (org in arrayOf("xorg1", "xorg2")) {
            for (system in arrayOf("xsystem1", "xsystem2")) {
                for (subsystem in arrayOf("xsubsys1", "xsubsys2")) {
                    var result = controller.subsystemArtifacts(org, system, subsystem)
                    Assert.assertEquals(4, result.size)
                    result = controller.subsystemArtifacts(org, system, subsystem + "x")
                    Assert.assertEquals(0, result.size)
                }
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

