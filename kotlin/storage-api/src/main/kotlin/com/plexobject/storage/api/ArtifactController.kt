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
import org.springframework.web.bind.annotation.ResponseBody
import javax.transaction.Transactional


@RestController
@ContextConfiguration(classes = [StorageConfig::class])
@Transactional
open class ArtifactController {
    private val logger = LoggerFactory.getLogger(ArtifactController::class.java)
    @Autowired
    lateinit var artifactRepository: ArtifactRepository
    @Autowired
    lateinit var s3Helper: S3Helper

    @RequestMapping(value = arrayOf("/api/{org}/{system}/{subsystem}"), method = arrayOf(RequestMethod.POST), produces = arrayOf("application/json"))
    fun upload(
            @PathVariable(value = "org") org: String,
            @PathVariable(value = "system") system: String,
            @PathVariable(value = "subsystem") subsystem: String,
            @RequestParam("overwrite", defaultValue = "false") overwrite: Boolean,
            @RequestParam("file") file: MultipartFile,
            @RequestParam("labels", defaultValue = "") labelList: List<String>,
            @RequestHeader("username", defaultValue = "") username: String,
            @RequestHeader("User-Agent", defaultValue = "") userAgent: String,
            @RequestParam params: Map<String, String>): Artifact {
        val name = file.originalFilename
        val size: Long = file.bytes.size.toLong()
        check(org.isNotEmpty()) { "organization is not specified." }
        check(system.isNotEmpty()) { "system is not specified." }
        check(subsystem.isNotEmpty()) { "subsystem is not specified." }
        check(name.isNotEmpty()) { "name is not specified." }
        check(size > 0) { "size is zero." }
        //
        var digest = s3Helper.hash(file.bytes)
        val key = Artifact.toKey(org, system, subsystem, name)
        s3Helper.upload(key, file.bytes)
        val labels: String = StringUtils.join(labelList, ',')
        val platform = params.get("platform")
        val contentType = file.contentType
        var artifact = Artifact(
                organization =  org,
                system = system,
                subsystem = subsystem,
                name = name,
                digest = digest,
                size = size,
                platform =  platform,
                username =  username,
                contentType = contentType,
                userAgent = userAgent,
                labels = labels)
        artifact.addProperties(params)
        artifactRepository.save(artifact, overwrite)
        //redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.originalFilename + "!")
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
        val q = PaginatedQuery(pageNumber, pageSize, params, sortBy.toTypedArray())
        return artifactRepository.query(q)
    }

    @RequestMapping(value = arrayOf("/api/{id}"), method = arrayOf(RequestMethod.GET), produces = arrayOf("application/json"))
    @ResponseBody
    fun get(
            @PathVariable(value = "id") id: String): Artifact {
        return artifactRepository.get(id)
    }

    @RequestMapping(value = arrayOf("/api/{id}/download"), method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun download(
            @PathVariable(value = "id") id: String): ResponseEntity<ByteArray> {
        val artifact = artifactRepository.get(id)
        val data = s3Helper.download(id)
        val mediaType = MediaType.parseMediaType(artifact.contentType ?: "application/octet-stream")
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + artifact.name)
                .contentType(mediaType)
                .contentLength(data.size.toLong())
                .body(data)
    }

    @RequestMapping(value = arrayOf("/api/{organization}/{system}/{subsystem}"), method = arrayOf(RequestMethod.GET), produces = arrayOf("application/json"))
    @ResponseBody
    fun subsystemArtifacts(
            @PathVariable(value = "organization") org: String,
            @PathVariable(value = "system") system: String,
            @PathVariable(value = "subsystem") subsystem: String): List<Artifact> {
        check(org.isNotEmpty()) { "organization is not specified." }
        check(system.isNotEmpty()) { "system is not specified." }
        check(subsystem.isNotEmpty()) { "subsystem is not specified." }
        val q = PaginatedQuery(1, 200, mapOf("organization" to org, "system" to system, "subsystem" to subsystem))
        return artifactRepository.query(q).records
    }

    //redirectAttributes: RedirectAttributes)
    //@ResponseBody
    @RequestMapping(value = arrayOf("/api/{id}"), method = arrayOf(RequestMethod.PUT), produces = arrayOf("application/json"))
    fun saveProperties(
            @PathVariable(value = "id") id: String,
            @RequestParam("labels", defaultValue = "") labelList: List<String>,
            @RequestParam params: Map<String, String>): Artifact {
        val labels: String = StringUtils.join(labelList, ',')
        val artifact = artifactRepository.saveProperties(id, labels, params)
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
