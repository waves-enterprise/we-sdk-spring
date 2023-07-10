package com.wavesenterprise.sdk.spring.autoconfigure.node.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "node.credentials-provider")
data class NodeCredentialsProperties(
    var addresses: Map<String, String?> = mutableMapOf(),
)
