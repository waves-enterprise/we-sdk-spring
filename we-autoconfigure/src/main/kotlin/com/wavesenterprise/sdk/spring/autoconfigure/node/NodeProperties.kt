package com.wavesenterprise.sdk.spring.autoconfigure.node

data class NodeProperties(
    val validationEnabled: Boolean,
    val config: MutableMap<String, NodeConfig>,
) {

    data class NodeConfig(
        val http: Http,
        val grpc: Grpc,
    ) {

        data class Http(
            val url: String,
            val feign: Feign,
        ) {
            data class Feign(
                val decode404: Boolean,
                val connectTimeout: Long,
                val readTimeout: Long,
            )
        }

        data class Grpc(
            val address: String,
            val port: Int,
            val keepAliveTime: Long?,
            val keepAliveWithoutCalls: Boolean?,
        )
    }
}
