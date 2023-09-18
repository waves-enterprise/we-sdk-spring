package com.wavesenterprise.sdk.spring.autoconfigure.contract.properties

data class ContractsProperties(
    val config: Map<String, Properties> = mutableMapOf(),
) {

    class Properties(
        val contractId: String? = null,
        val version: Int? = null,
        val fee: Long = 0,
        val image: String? = null,
        val imageHash: String? = null,
        val autoUpdate: AutoUpdate = AutoUpdate(),
        val validationEnabled: Boolean = true,
    ) {
        class AutoUpdate(
            val enabled: Boolean = false,
            val contractCreatorAddress: String? = null,
        )
    }
}
