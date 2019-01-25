package com.plexobject.storage.util

import com.amazonaws.HttpMethod
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.CreateBucketRequest
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.S3ObjectInputStream
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.s3.model.ObjectListing



open class S3HelperTest {
    var helper = S3Helper(
            "accessKey1",
            "verySecretKey1",
            "screenshots",
            "",
            "subfolder",
            "http://localhost:8000",
            1000)

    class MockErrorS3Helper() : S3Helper(
            "accessKey1",
            "verySecretKey1",
            "screenshots",
            "",
            "subfolder",
            "http://localhost:8000",
            1000) {
        override fun buildS3(): AmazonS3 {
            val s3 = mock<AmazonS3>(AmazonS3::class.java)
            doThrow(RuntimeException()).`when`(s3).createBucket(ArgumentMatchers.any(CreateBucketRequest::class.java))
            doThrow(RuntimeException()).`when`(s3).getObject(ArgumentMatchers.any(String::class.java), ArgumentMatchers.any(String::class.java))
            doThrow(RuntimeException()).`when`(s3).putObject(ArgumentMatchers.any(String::class.java), ArgumentMatchers.any(String::class.java), ArgumentMatchers.any(String::class.java))
            doThrow(RuntimeException()).`when`(s3).deleteObject(ArgumentMatchers.any(String::class.java), ArgumentMatchers.any(String::class.java))
            doThrow(RuntimeException()).`when`(s3).generatePresignedUrl(ArgumentMatchers.any(GeneratePresignedUrlRequest::class.java))
            doThrow(RuntimeException()).`when`(s3).listObjects(any(String::class.java), any(String::class.java))
            return s3
        }
    }
    class MockS3Helper() : S3Helper(
    "accessKey1",
    "verySecretKey1",
    "screenshots",
    "",
    "subfolder",
    "http://localhost:8000",
    1000) {
        override fun buildS3(): AmazonS3 {
            val s3 = mock<AmazonS3>(AmazonS3::class.java)
            val s3Obj = mock<S3Object>(S3Object::class.java)
            `when`<S3ObjectInputStream>(s3Obj.getObjectContent()).thenReturn(mock<S3ObjectInputStream>(S3ObjectInputStream::class.java))
            `when`<S3Object>(s3.getObject(ArgumentMatchers.any(String::class.java), ArgumentMatchers.any(String::class.java))).thenReturn(s3Obj)
            try {
                `when`<URL>(s3.generatePresignedUrl(ArgumentMatchers.any(GeneratePresignedUrlRequest::class.java)))
                        .thenReturn(URL("https://google.com"))
            } catch (e: Exception) {
            }

            val listObjs = mock(ObjectListing::class.java)
            val objSum = mock(S3ObjectSummary::class.java)
            `when`(listObjs.objectSummaries).thenReturn(listOf(objSum))
            `when`(s3.listObjects(any(String::class.java), any(String::class.java))).thenReturn(listObjs)
            return s3
        }
    }

    @Before
    fun setUp() {
    }

    @Test
    fun testDummy() {
    }

    @Test
    fun testHash() {
        Assert.assertNotNull(helper.hash("test".toByteArray(Charset.forName("UTF-8"))))
    }


    @Test
    fun testBuild() {
        var s3 = helper.buildS3()
        Assert.assertNotNull(s3)
        helper.endpoint = ""
        helper.region = ""
        helper.region = "us-west-2"
        s3 = helper.buildS3()
        Assert.assertNotNull(s3)
    }

    //@Test
    fun testDownload() {
        mockHelper()
        val arr = helper.download("screenshot_1524774411525.png")
        Assert.assertNotNull(arr)
    }

    //@Test
    fun testUploadDownload() {
        mockHelper()

        helper.upload("key", "value".toByteArray(Charset.forName("UTF-8")))
        val arr = helper.download("key")
        Assert.assertNotNull(arr)
        val result = String(arr)
        Assert.assertEquals("", result)
    }

    //@Test
    fun testIntegrationList() {
        // helper = new S3Helper("", "", "us-east-1", "");
        //
        // List<String> list = helper.list("dev",
        // "job/");
    }

    @Test
    fun testDeletePrefix() {
        mockHelper()
        helper.list("screenshots", "prefix")
    }

    //@Test(expected = RuntimeException::class)
    fun testDeletePrefixError() {
        mockHelperError()
        helper.list("screenshots", "prefix")
    }

    @Test
    fun testListWithError() {
        mockHelperError()
        helper.list("screenshots", "prefix")
    }


    @Test(expected = RuntimeException::class)
    fun testDownloadError() {
        mockHelperError()
        helper.download("screenshot_1524774411525.png")
    }

    @Test(expected = RuntimeException::class)
    fun testUploadDownloadError() {
        mockHelperError()

        helper.upload("key", "value".toByteArray(Charset.forName("UTF-8")))
    }

    @Test
    fun testDelete() {
        mockHelper()
        helper.delete("screenshot_1524774411525.png")
    }

    @Test
    fun testDeleteError() {
        mockHelperError()
        helper.delete("screenshot_1524774411525.png")
    }

    @Test
    fun testS3Presigned() {
        mockHelper()

        val u = helper.presignedUpload("key")
        Assert.assertNotNull(u)
    }

    @Test(expected = RuntimeException::class)
    fun testS3PresignedError() {
        mockHelperError()
        helper.presignedUpload("key")
    }

    // @Test
    @Throws(Exception::class)
    fun testIntegrationS3Presigned() {
        val awsCreds = BasicAWSCredentials("AKIAJFOO3FP7FDHEOQEA", "")
        val s3Client = AmazonS3ClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(awsCreds))
                .withRegion("us-east-1").build()
        val generatePresignedUrlRequest = GeneratePresignedUrlRequest("vmatrix-dev",
                "key1/key2.txt").withMethod(HttpMethod.PUT)
                .withExpiration(Date(System.currentTimeMillis() + 60 * 60 * 1000))
        val url = s3Client.generatePresignedUrl(generatePresignedUrlRequest)
        val connection = url.openConnection() as HttpURLConnection
        connection.setDoOutput(true)
        connection.setRequestMethod("PUT")
        val out = OutputStreamWriter(connection.getOutputStream())
        out.write("This text uploaded as an object via presigned URL.")
        out.close()
        val `object` = s3Client.getObject("vmatrix-dev", "key1/key2.txt")
        Assert.assertNotNull(`object`)
    }

    // @Test
    @Throws(IOException::class)
    fun testIntegrationUpload() {
        helper = S3Helper(
                "AKIAJFOO3FP7FDHEOQEA",
                "",
                "vmatrix-dev",
                "us-east-1",
                "subfolder/",
                "",
                1000)
        val u = helper.presignedUpload("myfile.txt")

        val connection = u.openConnection() as HttpURLConnection
        connection.setDoOutput(true)
        connection.setRequestMethod("PUT")
        val out = OutputStreamWriter(connection.getOutputStream())
        out.write("Sample output")
        out.close()

        connection.getResponseCode()
        val arr = helper.download("subfolder/myfile.txt")
        Assert.assertEquals("Sample output", String(arr, StandardCharsets.UTF_8))
    }

    // @Test
    fun testIntegrationDownload() {
        val arr = helper.download("screenshot_1524774411525.png")
        Assert.assertNotNull(arr)
    }

    // @Test
    fun testIntegrationUploadDownload() {
        helper.upload("test_key1", "value123".toByteArray(Charset.forName("UTF-8")))
        val arr = helper.download("test_key1")
        Assert.assertNotNull(arr)
        val result = String(arr)
        Assert.assertEquals("value123", result)
    }

    private fun mockHelperError() {
        helper = MockErrorS3Helper()
    }

    private fun mockHelper() {
        helper = MockS3Helper()
    }
}