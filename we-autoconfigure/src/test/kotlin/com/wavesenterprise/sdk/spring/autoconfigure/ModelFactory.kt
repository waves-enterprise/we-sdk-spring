package com.wavesenterprise.sdk.spring.autoconfigure

import com.wavesenterprise.sdk.spring.autoconfigure.contract.ContractsProperties

fun contractProps(
    contractId: String = "CgqRPcPnexY533gCh2SSvBXh5bca1qMs7KFGntawHGww",
    fee: Long = 0,
    image: String? = "image",
    imageHash: String? = "imageHash",
    autoUpdateIsEnabled: Boolean = false,
    contractCreatorAddress: String = "",
) = ContractsProperties.Properties(
    contractId = contractId,
    fee = fee,
    image = image,
    imageHash = imageHash,
    autoUpdate = autoUpdate(
        enabled = autoUpdateIsEnabled,
        contractCreatorAddress = contractCreatorAddress,
    )
)

fun autoUpdate(
    enabled: Boolean = false,
    contractCreatorAddress: String? = "",
) = ContractsProperties.Properties.AutoUpdate(
    enabled = enabled,
    contractCreatorAddress = contractCreatorAddress,
)
