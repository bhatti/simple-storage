package com.plexobject.storage.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable
import java.util.*
import javax.persistence.*

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

