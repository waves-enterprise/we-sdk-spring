package com.wavesenterprise.sdk.spring.autoconfigure

import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractImage
import com.wavesenterprise.sdk.node.domain.contract.ContractImageHash
import com.wavesenterprise.sdk.node.domain.contract.ContractInfo
import com.wavesenterprise.sdk.node.domain.contract.ContractVersion
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

fun contractInfo(
    contractId: String = "CgqRPcPnexY533gCh2SSvBXh5bca1qMs7KFGntawHGww",
    image: String = "image",
    imageHash: String = "imageHash",
    version: Int = 1,
    active: Boolean = true,
) = ContractInfo(
    id = ContractId.fromBase58(contractId),
    image = ContractImage.fromString(image),
    imageHash = ContractImageHash.fromString(imageHash),
    version = ContractVersion.fromInt(version),
    active = active,
)
