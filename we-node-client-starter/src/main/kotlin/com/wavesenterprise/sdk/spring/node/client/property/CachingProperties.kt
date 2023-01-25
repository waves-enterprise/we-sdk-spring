package com.wavesenterprise.sdk.spring.node.client.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration


@ConfigurationProperties("node-caching")
@ConstructorBinding
class CachingProperties(
    val txCacheSize: Int,
    val policyItemInfoCacheSize: Int,
    val cacheDuration: Duration,
)
