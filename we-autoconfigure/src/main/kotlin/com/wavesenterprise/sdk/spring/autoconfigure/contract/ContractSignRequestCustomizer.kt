package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.wavesenterprise.sdk.node.domain.sign.builder.ContractSignRequestBuilder

interface ContractSignRequestCustomizer {
    fun customize(
        contractKey: String,
        contractSignRequestBuilder: ContractSignRequestBuilder,
    )
}
