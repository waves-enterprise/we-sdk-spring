package com.wavesenterprise.sdk.spring.autoconfigure.node.properties

import feign.Logger

data class NodeProperties(
    val validationEnabled: Boolean,
    val config: MutableMap<String, NodeConfig>,
) {

    data class NodeConfig(
        val http: Http,
        val grpc: Grpc? = null,
    ) {

        data class Http(
            val url: String,
            val xApiKey: String? = null,
            val xPrivacyApiKey: String? = null,
            val feign: Feign,
        ) {
            data class Feign(
                val decode404: Boolean,
                val connectTimeout: Long,
                val readTimeout: Long,
                val loggerLevel: Logger.Level,
            )
        }

        data class Grpc(
            val address: String,
            val port: Int,
            val keepAliveTime: Long?, // todo parameters are not used
            val keepAliveWithoutCalls: Boolean?,
        )
    }
}
