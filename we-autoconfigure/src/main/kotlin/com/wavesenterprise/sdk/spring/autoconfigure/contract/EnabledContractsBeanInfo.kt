package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.wavesenterprise.sdk.contract.core.converter.factory.ConverterFactory
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.tx.signer.TxSigner

class EnabledContractsBeanInfo(
    val api: Class<out Any>,
    val impl: Class<out Any>,
    val name: String,

    val txSigner: TxSigner? = null,
    val nodeBlockingServiceFactory: NodeBlockingServiceFactory? = null,
    val converterFactory: ConverterFactory? = null,

    val txSignerBeanName: String? = null,
    val nodeBlockingServiceFactoryBeanName: String? = null,
    val converterFactoryBeanName: String? = null,

    val localValidationEnabled: Boolean = true,
)
