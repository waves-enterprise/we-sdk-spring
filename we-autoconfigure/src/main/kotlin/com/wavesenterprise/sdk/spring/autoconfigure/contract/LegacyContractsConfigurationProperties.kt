package com.wavesenterprise.sdk.spring.autoconfigure.contract

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "contracts")
@ConstructorBinding
data class LegacyContractsConfigurationProperties(
    val sender: String? = null,
    val fee: Long = 0,
    val config: Map<String, Properties> = mutableMapOf(),
) {
    class Properties(
        val id: String? = null,
        val fee: Long? = 0,
        val image: String? = null,
        val imageHash: String? = null,
        val autoUpdate: Boolean = false,
    )
}
