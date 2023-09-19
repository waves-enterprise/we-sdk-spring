package com.wavesenterprise.sdk.spring.autoconfigure.atomic

import com.wavesenterprise.sdk.atomic.manager.AtomicAwareContextManager
import com.wavesenterprise.sdk.tx.signer.TxSigner
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.Ordered

class AtomicAwareTxSignerPostProcessor(
    private val atomicAwareContextManager: AtomicAwareContextManager,
) : BeanPostProcessor, Ordered {

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = when (bean) {
        is TxSigner -> AtomicAwareTxSignerFactory.create(
            txSigner = bean,
            atomicAwareContextManager = atomicAwareContextManager,
        )
        else -> bean
    }

    override fun getOrder(): Int =
        Ordered.HIGHEST_PRECEDENCE
}
