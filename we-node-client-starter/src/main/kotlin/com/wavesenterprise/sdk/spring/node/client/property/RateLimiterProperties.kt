package com.wavesenterprise.sdk.spring.node.client.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("rate-limiting")
@ConstructorBinding
data class RateLimiterProperties(
    val maxUtxSize: Int,
    val backOff: BackOffProperties,
) {
    data class BackOffProperties(
        val minWaitMs: Long,
        val maxWaitMs: Long,
        val maxWaitTotalMs: Long,
    )
}