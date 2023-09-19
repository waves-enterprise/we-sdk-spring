package com.wavesenterprise.sdk.spring.autoconfigure.node.properties

import feign.Logger
import org.springframework.boot.context.properties.ConstructorBinding

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
        val grpc: Grpc? = null,
    ) {

        data class Http(
            val url: String = "",
            val xApiKey: String? = null,
            val xPrivacyApiKey: String? = null,
            val feign: Feign = Feign(),
        ) {

            data class Feign(
                val decode404: Boolean = true,
                val connectTimeout: Long = 5000,
                val readTimeout: Long = 3000,
                val loggerLevel: Logger.Level = Logger.Level.FULL,
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
