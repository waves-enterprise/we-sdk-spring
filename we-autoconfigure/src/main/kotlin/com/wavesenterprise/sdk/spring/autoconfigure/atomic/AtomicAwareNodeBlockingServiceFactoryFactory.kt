package com.wavesenterprise.sdk.spring.autoconfigure.atomic

import com.wavesenterprise.sdk.atomic.AtomicAwareNodeBlockingServiceFactory
import com.wavesenterprise.sdk.atomic.manager.AtomicAwareContextManager
import com.wavesenterprise.sdk.atomic.manager.ContractInfoCacheManager
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.tx.signer.TxSigner

object AtomicAwareNodeBlockingServiceFactoryFactory {

    fun create(
        nodeBlockingServiceFactory: NodeBlockingServiceFactory,
        atomicAwareContextManager: AtomicAwareContextManager,
        contractInfoCacheManager: ContractInfoCacheManager,
        txSigner: () -> TxSigner,
    ) = AtomicAwareNodeBlockingServiceFactory(
        nodeBlockingServiceFactory = nodeBlockingServiceFactory,
        atomicAwareContextManager = atomicAwareContextManager,
        contractInfoCacheManager = contractInfoCacheManager,
        txSigner = txSigner,
    )
}
