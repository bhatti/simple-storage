package com.plexobject.storage.api

import com.plexobject.storage.StorageConfig
import com.plexobject.storage.domain.*
import com.plexobject.storage.repository.ArtifactRepository
import com.plexobject.storage.util.S3Helper
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import org.springframework.web.bind.annotation.ResponseBody




@RestController
@ContextConfiguration(classes = [StorageConfig::class])
class ArtifactController {
    private val logger = LoggerFactory.getLogger(ArtifactController::class.java)
    @Autowired
    lateinit var artifactRepository: ArtifactRepository
    @Autowired
    lateinit var s3Helper: S3Helper

    @RequestMapping(value = arrayOf("/api/{app}/{job}"), method = arrayOf(RequestMethod.POST), produces = arrayOf("application/json"))
    fun upload(
            @PathVariable(value = "app") app: String,
            @PathVariable(value = "job") job: String,
            @RequestParam("overwrite", defaultValue = "false") overwrite: Boolean,
            @RequestParam("file") file: MultipartFile,
            @RequestParam("labels", defaultValue = "") labelList: List<String>,
            @RequestHeader("User-Agent", defaultValue = "") userAgent: String,
            @RequestParam params: Map<String, String>): Artifact {
        val name = file.originalFilename
        var digest = s3Helper.hash(file.bytes)
        val key = Artifact.toKey(app, job, name)
        s3Helper.upload(key, file.bytes)
        val labels: String = StringUtils.join(labelList, ',')
        val size: Long = file.bytes.size.toLong()
        val platform = params.get("platform")
        val contentType = file.contentType
        var artifact = Artifact(
                app,
                job,
                name,
                digest,
                platform,
                contentType,
                size,
                "",
                userAgent,
                labels)
        artifact.addProperties(params)
        artifactRepository.save(artifact, overwrite)
        //redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.originalFilename + "!")
        artifact.url = s3Helper.presignedUpload(key).toString()
        logger.info("Saved artifact $artifact")
        return artifact
    }

    @RequestMapping(value = arrayOf("/api"), method = arrayOf(RequestMethod.GET), produces = arrayOf("application/json"))
    @ResponseBody
    fun query(
            @RequestParam("page", defaultValue = "1") pageNumber: Int,
            @RequestParam("pageSize", defaultValue = "20") pageSize: Int,
            @RequestParam("sortBy", defaultValue = "") sortBy: List<String>,
            @RequestParam params: Map<String, String>): PaginatedResult<Artifact> {
        val q = PaginatedQuery(params, pageNumber, pageSize, sortBy.toTypedArray())
        val result = artifactRepository.query(q)
        for (artifact in result.records) {
            artifact.url = s3Helper.presignedUpload(artifact.getId()).toString()
        }
        return result
    }

    @RequestMapping(value = arrayOf("/api/{app}/{job}/{name}"), method = arrayOf(RequestMethod.GET), produces = arrayOf("application/json"))
    @ResponseBody
    fun get(
            @PathVariable(value = "app") app: String,
            @PathVariable(value = "job") job: String,
            @PathVariable(value = "name") name: String): Artifact {
        val artifact = artifactRepository.get(app, job, name)
        artifact.url = s3Helper.presignedUpload(artifact.getId()).toString()
        return artifact
    }

    @RequestMapping(value = arrayOf("/api/{app}/{job}/{name}/download"), method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun download(
            @PathVariable(value = "app") app: String,
            @PathVariable(value = "job") job: String,
            @PathVariable(value = "name") name: String): ResponseEntity<ByteArray> {
        val artifact = artifactRepository.get(app, job, name)
        val data = s3Helper.download(artifact.getId())
        val mediaType = MediaType.parseMediaType(artifact.contentType ?: "application/octet-stream")
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + artifact.name)
                .contentType(mediaType)
                .contentLength(data.size.toLong())
                .body(data)
    }

    @RequestMapping(value = arrayOf("/api/{app}/{job}"), method = arrayOf(RequestMethod.GET), produces = arrayOf("application/json"))
    @ResponseBody
    fun job(
            @PathVariable(value = "app") app: String,
            @PathVariable(value = "job") job: String): List<Artifact> {
        val q = PaginatedQuery(mapOf("app" to app, "job" to job), 1, 200)
        val result = artifactRepository.query(q).records
        for (artifact in result) {
            artifact.url = s3Helper.presignedUpload(artifact.getId()).toString()
        }
        return result
    }

    //redirectAttributes: RedirectAttributes)
    //@ResponseBody
    @RequestMapping(value = arrayOf("/api/{app}/{job}/{name}"), method = arrayOf(RequestMethod.PUT), produces = arrayOf("application/json"))
    fun save(
            @PathVariable(value = "app") app: String,
            @PathVariable(value = "job") job: String,
            @PathVariable(value = "name") name: String,
            @RequestParam params: Map<String, String>): Artifact {
        val artifact = artifactRepository.saveProperties(app, job, name, params)
        logger.info("Saved artifact properties $artifact")
        return artifact
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleStorageFileNotFound(_exc: NotFoundException): ResponseEntity<*> {
        return ResponseEntity.notFound().build<Any>()
    }

    @ExceptionHandler(DuplicateException::class)
    fun handleStorageDuplicate(_exc: DuplicateException): ResponseEntity<*> {
        return ResponseEntity.status(HttpStatus.CONFLICT).build<Any>()
    }
}
