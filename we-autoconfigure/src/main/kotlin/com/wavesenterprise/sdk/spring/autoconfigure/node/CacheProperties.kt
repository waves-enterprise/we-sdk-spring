package com.wavesenterprise.sdk.spring.autoconfigure.node

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration

@ConstructorBinding
@ConfigurationProperties(prefix = "node.cache")
data class CacheProperties(
    @DefaultValue("true")
    var enabled: Boolean,
    @DefaultValue("5000")
    var txCacheSize: Int,
    @DefaultValue("500")
    var policyItemInfoCacheSize: Int,
    @DefaultValue("500s")
    var cacheDuration: Duration,
) {

    companion object {
        val DISABLED = CacheProperties(
            enabled = false,
            txCacheSize = 0,
            policyItemInfoCacheSize = 0,
            cacheDuration = Duration.ZERO,
        )
    }
}
