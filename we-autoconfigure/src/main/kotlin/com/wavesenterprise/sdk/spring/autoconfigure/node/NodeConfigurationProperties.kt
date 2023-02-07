package com.wavesenterprise.sdk.spring.autoconfigure.node

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "node")
data class NodeConfigurationProperties(
    val validationEnabled: Boolean = true,
    val skipConfigMarker: String = "DO_NOT_USE",
    val config: MutableMap<String, NodeConfig> = mutableMapOf(),
) {
    fun getConfigForUsage(): Map<String, NodeConfig> =
        config.filterNot { it.value.http.url.isBlank() }
            .filterNot { it.value.http.url.contains(skipConfigMarker) }

    @ConstructorBinding
    data class NodeConfig(
        val http: Http = Http(),
        val grpc: Grpc = Grpc(),
    ) {

        data class Http(
            val url: String = "",
            val feign: Feign = Feign(),
        ) {

            data class Feign(
                val decode404: Boolean = true,
                val connectTimeout: Long = 5000,
                val readTimeout: Long = 3000,
            )
        }

        data class Grpc(
            val address: String = "localhost",
            val port: Int = 6865,
            val keepAliveTime: Long? = null,
            val keepAliveWithoutCalls: Boolean? = null,
        )
    }
}
