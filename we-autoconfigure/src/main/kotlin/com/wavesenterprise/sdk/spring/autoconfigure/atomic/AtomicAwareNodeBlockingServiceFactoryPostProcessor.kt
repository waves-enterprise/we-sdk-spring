package com.wavesenterprise.sdk.spring.autoconfigure.atomic

import com.wavesenterprise.sdk.atomic.AtomicAwareTxSigner
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.core.Ordered

class AtomicAwareNodeBlockingServiceFactoryPostProcessor(
    private val applicationContext: ApplicationContext,
) : BeanPostProcessor, Ordered {

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any = when (bean) {
        is NodeBlockingServiceFactory -> AtomicAwareNodeBlockingServiceFactoryFactory.create(
            nodeBlockingServiceFactory = bean,
            txSigner = { applicationContext.getBean(AtomicAwareTxSigner::class.java) },
        )
        else -> bean
    }

    override fun getOrder(): Int =
        Ordered.HIGHEST_PRECEDENCE + 1
}
