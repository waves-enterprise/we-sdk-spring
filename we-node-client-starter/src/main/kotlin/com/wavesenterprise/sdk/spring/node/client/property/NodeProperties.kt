package com.wavesenterprise.sdk.spring.node.client.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("node")
@ConstructorBinding
data class NodeProperties(
    val validationEnabled: Boolean = true,
    val skipConfigMarker: String = "DO_NOT_USE",
    val config: MutableMap<String, NodeConfig> = mutableMapOf()
) {
    fun getConfigForUsage(): Map<String, NodeConfig> =
        config.filterNot { it.value.http.url.isBlank() }
            .filterNot { it.value.http.url.contains(skipConfigMarker) }

    @ConstructorBinding
    data class NodeConfig(
        val credentials: Credentials, // del
        val http: Http,
        val grpc: Grpc,
    ) {

        data class Credentials(
            val senderAddress: String,
            val password: String = "",
        )

        data class Http(
            val url: String,
            val feign: Feign,
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
            val keepAliveWithoutCalls: Boolean? = null
        )
    }

}
