package com.wavesenterprise.sdk.spring.autoconfigure.atomic

import com.wavesenterprise.sdk.atomic.AtomicAwareTxSigner
import com.wavesenterprise.sdk.atomic.manager.AtomicAwareContextManager
import com.wavesenterprise.sdk.tx.signer.TxSigner

object AtomicAwareTxSignerFactory {

    fun create(txSigner: TxSigner, atomicAwareContextManager: AtomicAwareContextManager) =
        AtomicAwareTxSigner(
            atomicAwareContextManager = atomicAwareContextManager,
            txSigner = txSigner,
        )
}
