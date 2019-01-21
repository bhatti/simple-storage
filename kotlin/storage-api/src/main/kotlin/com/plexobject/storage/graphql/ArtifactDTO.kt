package com.plexobject.storage.graphql

import com.plexobject.storage.domain.Artifact
import java.text.SimpleDateFormat

data class ArtifactDTO(
        val id: String,
        val organization: String,
        val system: String,
        val subsystem: String,
        val name: String,
        val digest: String,
        val size: Long,
        val platform: String?,
        val username: String?,
        val contentType: String?,
        val userAgent: String?,
        val labels: List<String>,
        val properties: List<KeyValue>,
        val createdAt: String,
        val updatedAt: String) {
    companion object {
        fun from(other: Artifact): ArtifactDTO {
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val otherProps = other.getPropertiesAsMap()
            var props = mutableListOf<KeyValue>()
            for ((k, v) in otherProps) {
                props.add(KeyValue(k, v))
            }
            //
            return ArtifactDTO(
                    id = other.id,
                    organization = other.organization,
                    system = other.system,
                    subsystem = other.subsystem,
                    name = other.name,
                    digest = other.digest,
                    size = other.size,
                    platform = other.platform,
                    username = other.username,
                    contentType = other.contentType,
                    userAgent = other.userAgent,
                    labels = ArrayList<String>(other.labelsAsSet()),
                    properties = props,
                    createdAt = fmt.format(other.createdAt),
                    updatedAt = fmt.format(other.updatedAt)
            )
        }
    }
}