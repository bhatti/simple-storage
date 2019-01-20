package com.plexobject.storage.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.DateSerializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.apache.commons.lang3.StringUtils
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.util.ProxyUtils
import java.io.Serializable
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import javax.persistence.*
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root


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
            props.forEach { k,v ->
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

/*
@Embeddable
data class ArtifactLabels(
        @NotNull
        @get:Column(name = "labels", nullable = false)
        var values: Array<String> = arrayOf()
) : Serializable {
    fun merge(other: ArtifactLabels) {
        merge(*other.values)
    }

    fun merge(vararg other: String) {
        val all = HashSet<String>()
        all.addAll(other.toList())
        all.addAll(values.toList())
        val list = ArrayList<String>(all)
        this.values = list.toTypedArray()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArtifactLabels

        if (!values.contentEquals(other.values)) return false

        return true
    }

    override fun hashCode(): Int {
        return values.contentHashCode()
    }

    override fun toString(): String {
        val set = TreeSet<String>()
        set.addAll(values.toList())
        return set.toString()
    }


    companion object {
        fun fromString(str: String): ArtifactLabels {
            val toks = ArrayList<String>(HashSet<String>(
                    str.split(",").filter { s -> s.isNotEmpty() }.map { s -> s.trim() })).toTypedArray()
            return ArtifactLabels(toks)
        }
    }
}
*/

@Entity
@Table(name = "properties")
open class ArtifactProperty(
        @get:ManyToOne(fetch = FetchType.LAZY)
        @get:JoinColumn(name = "artifact_id", updatable = false)
        @JsonIgnore
        open var artifact: Artifact? = null,

        @get:Id
        open var id: String = "",

        @get:Column(nullable = false) //, insertable = true, updatable = false)
        open var name: String = "",

        @get:Column(nullable = false) //, insertable = true, updatable = false)
        open var value: String = "",

        @get:Column(name = "created_at", nullable = true)
        open var createdAt: Date = Date()
) : Serializable {

    override fun toString(): String {
        return "${name}=${value}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArtifactProperty

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
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


data class ArtifactSpecification(val pq: PaginatedQuery) : Specification<Artifact> {
    fun and(builder: CriteriaBuilder, first: Predicate?, second: Predicate): Predicate {
        first?.let {
            return builder.and(it, second)
        }
        return second
    }

    fun or(builder: CriteriaBuilder, first: Predicate?, second: Predicate): Predicate {
        first?.let {
            return builder.or(it, second)
        }
        return second
    }

    override fun toPredicate(root: Root<Artifact>, query: CriteriaQuery<*>, builder: CriteriaBuilder): Predicate? {
        var topPredicate: Predicate? = null
        val application: String? = pq.params.get("application") ?: pq.params.get("app")
        val job: String? = pq.params.get("job")
        val name: String? = pq.params.get("name")
        val platform: String? = pq.params.get("platform")
        val userAgent: String? = pq.params.get("userAgent")
        val digest: String? = pq.params.get("digest")
        var since: Date? = null
        pq.params.get("since")?.let {
            val accessor = DateTimeFormatter.ISO_DATE_TIME.parse(it)
            since = Date.from(Instant.from(accessor))
            //val dt: LocalDateTime = OffsetDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime()
            //since = Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
        }
        val labels: String? = pq.params.get("labels") ?: pq.params.get("label")

        application?.let {
            topPredicate = and(builder, topPredicate, builder.equal(root.get<String>(Artifact.application), it))
        }
        job?.let {
            topPredicate = and(builder, topPredicate, builder.equal(root.get<String>(Artifact.job), it))
        }
        userAgent?.let {
            topPredicate = and(builder, topPredicate, builder.equal(root.get<String>(Artifact.userAgent), it))
        }
        platform?.let {
            topPredicate = and(builder, topPredicate, builder.equal(root.get<String>(Artifact.platform), it.toUpperCase()))
        }
        digest?.let {
            topPredicate = and(builder, topPredicate, builder.equal(root.get<String>(Artifact.digest), it.toLowerCase()))
        }
        name?.let {
            topPredicate = and(builder, topPredicate, builder.equal(root.get<String>(Artifact.name), it))
        }
        since?.let {
            topPredicate = and(builder, topPredicate, builder.greaterThanOrEqualTo(root.get<Date>(Artifact.createdAt), it))
        }
        labels?.let {
            val values = it.toLowerCase().split(",").map { it.trim() }.filter { it.isNotEmpty() }
            var labelPredicate: Predicate? = null
            for (i in 0..values.size - 1) {
                val value = values[i]
                labelPredicate = or(builder, labelPredicate, builder.like(root.get<String>(Artifact.labels), "%$value%"))
            }
            topPredicate = and(builder, topPredicate, labelPredicate!!)
        }
        return topPredicate
    }
}