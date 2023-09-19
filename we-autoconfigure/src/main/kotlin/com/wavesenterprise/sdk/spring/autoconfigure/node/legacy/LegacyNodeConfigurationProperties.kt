package com.wavesenterprise.sdk.spring.autoconfigure.node.legacy

import feign.Logger
import org.springframework.boot.context.properties.ConstructorBinding

data class LegacyNodeConfigurationProperties(
    val validationEnabled: Boolean = true,
    val skipConfigMarker: String = "DO_NOT_USE",
    val errorDecoderSupplierBeanName: String? = null,
    val config: MutableMap<String, LegacyNodeConfig> = mutableMapOf(),
) {
    fun getConfigForUsage(): Map<String, LegacyNodeConfig> =
        config.filterNot { it.value.http.url.isBlank() }
            .filterNot { it.value.http.url.contains(skipConfigMarker) }

    @ConstructorBinding
    data class LegacyNodeConfig(
        val xApiKey: String? = null,
        val xPrivacyApiKey: String? = null,
        val nodeOwnerAddress: String? = null,
        val keyStorePassword: String? = null,
        val http: Http = Http(),
        val grpc: Grpc? = null,
    ) {
        data class Http(
            val url: String = "",
            val decode404: Boolean = true,
            val connectTimeout: Int = 5000,
            val readTimeout: Int = 3000,
            val loggerLevel: Logger.Level = Logger.Level.FULL,
        )

        data class Grpc(
            val address: String = "localhost",
            val port: Int = 6865,
            val keepAliveTime: Long? = null,
            val keepAliveWithoutCalls: Boolean? = null,
        )
    }
}
