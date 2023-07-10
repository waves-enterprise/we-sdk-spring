package com.wavesenterprise.sdk.spring.autoconfigure.node.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration

@ConstructorBinding
@ConfigurationProperties(prefix = "node.rate-limiter")
data class RateLimiterProperties(
    @DefaultValue("true")
    var enabled: Boolean,
    @DefaultValue("50")
    var maxUtx: Int,
    @DefaultValue("1s")
    var minWait: Duration,
    @DefaultValue("3s")
    var maxWait: Duration,
    @DefaultValue("10s")
    var maxWaitTotal: Duration
) {

    companion object {
        val DISABLED = RateLimiterProperties(
            enabled = false,
            maxUtx = 0,
            minWait = Duration.ZERO,
            maxWait = Duration.ZERO,
            maxWaitTotal = Duration.ZERO
        )
    }
}
