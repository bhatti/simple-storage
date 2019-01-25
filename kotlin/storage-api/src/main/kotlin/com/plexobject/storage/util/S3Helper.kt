package com.plexobject.storage.util

import com.amazonaws.HttpMethod
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.CreateBucketRequest
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import java.net.URL
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*
import java.util.stream.Collectors



open class S3Helper(
        val accessKey: String,
        val secretKey: String,
        val bucket: String,
        var region: String,
        prefix: String,
        var endpoint: String,
        val expirationMinutes: Long) {

    lateinit var awsCreds: BasicAWSCredentials
    val prefix: String

    init {
        checkNotNull(accessKey, { "The accessKey id must be set." })
        checkNotNull(secretKey, { "The secretKey id must be set." })
        checkNotNull(bucket, { "The bucket id must be set." })
        checkNotNull(prefix, { "The prefix id must be set." })
        if (prefix.isNotEmpty() && !prefix.endsWith("/")) {
            this.prefix = "$prefix/"
        } else {
            this.prefix = prefix
        }
        this.awsCreds = BasicAWSCredentials(accessKey.trim(), secretKey.trim())
        logger.info("*** S3 endpoint " + endpoint + ", bucket " + bucket + ", region " + region + ", prefix " + prefix
                + ", key " + accessKey)
    }

    fun upload(akey: String, bytes: ByteArray) {
        val s3 = buildS3()
        val key = prefix + akey
        try {
            s3.createBucket(CreateBucketRequest(bucket))
        } catch (e: Exception) {
        }
        try {
            s3.putObject(bucket, key, String(bytes,  Charset.forName("UTF-8")))
        } catch (e: Exception) {
            throw RuntimeException("Failed to upload to bucket '$bucket', key '$key'", e)
        }

    }

    fun hash(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) }).toLowerCase()
    }

    fun list(bucket: String, prefix: String): List<String> {
        val s3 = buildS3()
        try {
            return s3.listObjects(bucket, prefix).objectSummaries.map { o -> o.key }
        } catch (e: Exception) {
            logger.error("Failed to retrieve list of keys for $bucket, prefix $prefix", e)
            return Collections.emptyList()
        }

    }

    fun presignedUpload(akey: String): URL {
        val key = prefix + akey
        val s3 = buildS3()
        try {
            val expTimeMillis = (expirationMinutes * 60 * 1000) + System.currentTimeMillis()
            val generatePresignedUrlRequest = GeneratePresignedUrlRequest(bucket, key)
                    .withMethod(HttpMethod.GET).withExpiration(Date(expTimeMillis))
            val url = s3.generatePresignedUrl(generatePresignedUrlRequest)
            logger.debug("**** presignedUpload " + key + " from bucket " + bucket + " using S3 '" + endpoint
                    + "', region '" + region + " ====== " + url)
            return url
        } catch (e: Exception) {
            throw RuntimeException("Failed to presigned bucket '$bucket', key '$key'", e)
        }
    }

    fun download(akey: String): ByteArray {
        val key = prefix + akey
        val s3 = buildS3()
        try {
            logger.debug("**** Downloading " + key + " from bucket " + bucket + " using S3 '" + endpoint
                    + "', region '" + region)
            // s3.createBucket(new CreateBucketRequest(bucket));
            val o = s3.getObject(bucket, key)
            return o.getObjectContent().readBytes()
        } catch (e: Exception) {
            throw RuntimeException("Failed to download from bucket '$bucket', key '$key'", e)
        }

    }

    fun delete(akey: String) {
        val key = prefix + akey

        val s3 = buildS3()
        try {
            s3.deleteObject(bucket, key)
        } catch (e: Exception) {
        }

    }

    open fun buildS3(): AmazonS3 {
        var s3Builder = AmazonS3ClientBuilder.standard()
                .withCredentials(AWSStaticCredentialsProvider(awsCreds)).enablePathStyleAccess()
        // s3Builder.withForceGlobalBucketAccessEnabled(true);
        if (!StringUtils.isEmpty(endpoint)) {
            s3Builder = s3Builder.withEndpointConfiguration(EndpointConfiguration(endpoint, region))
        } else if (!StringUtils.isEmpty(region)) {
            s3Builder = s3Builder.withRegion(region)
        }
        s3Builder.setPathStyleAccessEnabled(true)
        return s3Builder.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(S3Helper::class.java)
    }
}
