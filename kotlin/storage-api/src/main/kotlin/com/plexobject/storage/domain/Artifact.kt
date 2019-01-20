package com.plexobject.storage.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.DateSerializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.apache.commons.lang3.StringUtils
import org.springframework.data.util.ProxyUtils
import java.io.Serializable
import java.util.*
import javax.persistence.*


@Entity
@Table(name = "artifacts") //, schema = "fs")
open class Artifact(
        @get:Column(nullable = false)
        open var application: String = "",

        @get:Column(nullable = false)
        open var job: String = "",

        @get:Column(nullable = false)
        open var name: String = "",

        @get:Column(nullable = false)
        open var digest: String = "",

        @get:Column(nullable = false)
        open var platform: String? = null,

        @get:Column(name = "content_type", nullable = false)
        open var contentType: String? = null,

        @get:Column(nullable = false)
        open var size: Long = 0,

        @get:Transient
        open var url: String? = null,

        @get:Column(name = "user_agent", nullable = false)
        open var userAgent: String? = null,

        @get:JsonSerialize(using = DateSerializer::class)
        @get:Column(name = "created_at", nullable = true)
        open var createdAt: Date = Date(),

        @get:JsonSerialize(using = DateSerializer::class)
        @get:Column(name = "updated_at", nullable = true)
        open var updatedAt: Date = Date(),

        @get:Column(name = "labels", nullable = false)
        @get:JsonSerialize(using = LabelsSerializer::class)
        //@Embedded
        //open var labels: ArtifactLabels = ArtifactLabels.fromString(""),
        open var labels: String = "",

        @get:OneToMany(mappedBy = "artifact", cascade = arrayOf(CascadeType.ALL), orphanRemoval = true, fetch = FetchType.EAGER)
        @get:JsonSerialize(using = PropertySerializer::class)
        open var properties: MutableSet<ArtifactProperty> = HashSet<ArtifactProperty>()
) : Serializable {
    //
    constructor(
            application: String,
            job: String,
            name: String,
            digest: String,
            platform: String?,
            contentType: String?,
            size: Long,
            url: String?,
            userAgent: String?,
            labels: String) :
            this(
                    application,
                    job,
                    name,
                    digest,
                    platform,
                    contentType,
                    size,
                    url,
                    userAgent,
                    Date(),
                    Date(),
                    labels,
                    HashSet<ArtifactProperty>()) {
        initId()
        val list = ArrayList<String>(stringToSet(labels))
        this.labels = StringUtils.join(list, ',')
    }

    @Column(name = "id")
    @Id
    fun getId(): String {
        return initId()
    }

    fun setId(id: String) {
    }

    @JsonIgnore
    fun initId(): String {
        val key = toKey(application, job, name)
        setId(key)
        return key
    }

    fun mergeLabels(other: Artifact) {
        mergeLabels(other.labelsAsSet())
    }

    fun mergeLabels(other: Set<String>) {
        val all = HashSet<String>()
        all.addAll(labelsAsSet())
        all.addAll(other)
        val list = ArrayList<String>(all)
        this.labels = StringUtils.join(list, ',')
    }

    @JsonIgnore
    fun labelsAsSet(): Set<String> {
        return stringToSet(labels)
    }

    private fun toKey(): String {
        return "${application}:${job}:${name}"
    }

    fun update(other: Artifact) {
        userAgent = other.userAgent
        size = other.size
        contentType = other.contentType
        platform = other.platform
        digest = other.digest
        mergeLabels(other)
        properties.clear()
        properties.addAll(other.properties)
    }

    fun validate() {
        check(size > 0) { "size must be greater than zero." }
        check(application.isNotEmpty()) { "application is not specified." }
        check(job.isNotEmpty()) { "job is not specified." }
        check(name.isNotEmpty()) { "name is not specified." }
        check(digest.isNotEmpty()) { "digest is not specified." }
    }

    @JsonIgnore
    fun addProperties(map: Map<String, String>) {
        val all = getPropertiesAsMap() + Artifact.filterProperties(map)
        var props = mutableSetOf<ArtifactProperty>()
        all.forEach { k, v ->
            val pid = "${getId()}:$k"
            props.add(ArtifactProperty(this, pid, k, v, Date()))
        }
        this.properties.clear()
        this.properties.addAll(props)
    }

    @Transient
    @JsonIgnore
    fun getPropertiesAsMap(): Map<String, String> {
        var map = TreeMap<String, String>()
        for (prop in properties) {
            map.put(prop.name, prop.value)
        }
        return map
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false

        if (this === other) return true

        if (javaClass != ProxyUtils.getUserClass(other)) return false

        other as Artifact

        return this.getId() == other.getId()
    }

    override fun hashCode(): Int {
        return this.getId().hashCode()
    }

    override fun toString(): String {
        return "Artifact(id='${getId()}', application='$application', job='$job', name='$name', digest='$digest', platform=$platform, contentType='$contentType', size=$size, url='$url', userAgent='$userAgent', createdAt=$createdAt, updatedAt=$updatedAt, labels=$labels, properties=${properties})"
    }

    companion object {
        val application = "application"
        val job = "job"
        val name = "name"
        val platform = "platform"
        val labels = "labels"
        val digest = "digest"
        val userAgent = "userAgent"
        val createdAt = "createdAt"

        fun stringToSet(str: String): Set<String> {
            return TreeSet<String>(
                    str.toLowerCase().split(",").map { s -> s.trim() }.filter { s -> s.isNotEmpty() })
        }

        fun setToMap(set: Set<ArtifactProperty>): Map<String, String> {
            var map = TreeMap<String, String>()
            set.forEach { p ->
                map.put(p.name, p.value)
            }
            return map
        }

        fun filterProperties(props: Map<String, String>): Map<String, String> {
            val fieldsRegex = Regex("(app|application|job|name|platform|contentType|overwrite|label|labels|createdAt|updatedAt|size|url|userAgent|digest)", RegexOption.IGNORE_CASE)
            var newProps = TreeMap<String, String>()
            props.forEach { k, v ->
                if (!fieldsRegex.matches(k)) { // containsMatchIn
                    newProps.put(k, v)
                }
            }
            return newProps
        }

        private fun normalize(str: String): String {
            return str.replace("[^A-Za-z0-9 ._]/", "_");
        }

        fun toKey(application: String, job: String, name: String): String {
            check(application.isNotEmpty()) { "application is not specified." }
            check(job.isNotEmpty()) { "job is not specified." }
            check(name.isNotEmpty()) { "name is not specified." }
            return "${normalize(application)}/${normalize(job)}/${normalize(name)}"
        }
    }
}


class LabelsSerializer @JvmOverloads constructor(t: Class<String>? = null) : StdSerializer<String>(t) {
    override fun serialize(value: String, gen: JsonGenerator, arg2: SerializerProvider) {
        val set = Artifact.stringToSet(value)
        gen.writeObject(set)
    }
}

class PropertySerializer @JvmOverloads constructor(t: Class<Set<ArtifactProperty>>? = null) : StdSerializer<Set<ArtifactProperty>>(t) {
    override fun serialize(value: Set<ArtifactProperty>, gen: JsonGenerator, arg2: SerializerProvider) {
        val map = Artifact.setToMap(value)
        gen.writeObject(map)
    }
}


