package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.wavesenterprise.sdk.contract.client.invocation.factory.ContractBlockingClientFactory
import com.wavesenterprise.sdk.contract.client.invocation.factory.ContractClientParams
import com.wavesenterprise.sdk.contract.core.converter.factory.ConverterFactory
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.tx.signer.TxSigner
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.ApplicationContext
import org.springframework.core.ResolvableType

class ContractBlockingClientFactoryRegistryPostProcessor(
    private val txSigner: TxSigner?,
    private val nodeBlockingServiceFactory: NodeBlockingServiceFactory,
    private val converterFactory: ConverterFactory,
    private val applicationContext: ApplicationContext,
) : BeanDefinitionRegistryPostProcessor {

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        val beanInfos = applicationContext
            .getBeansOfType(EnabledContractsBeanInfo::class.java)
        beanInfos.values.forEach { beanInfo ->
            val contractSignRequestBuilderFactory = ContractConfigurationPropertiesForSignRequestBuilderFactory(
                contractKey = beanInfo.name,
                applicationContext = applicationContext,
            )
            val defBuilder = BeanDefinitionBuilder.rootBeanDefinition(
                ContractBlockingClientFactory::class.java.name
            )
            defBuilder.apply {
                addConstructorArgValue(beanInfo.impl)
                addConstructorArgValue(beanInfo.api)
                addConstructorArgValue(ContractClientParams(false)) // todo
                addConstructorArgValue(contractSignRequestBuilderFactory)

                beanInfo.txSigner?.let {
                    addConstructorArgValue(it)
                } ?: beanInfo.txSignerBeanName?.let {
                    if (it.isBlank()) null else addConstructorArgReference(it)
                } ?: addConstructorArgValue(txSigner)

                beanInfo.converterFactory?.let {
                    addConstructorArgValue(it)
                } ?: beanInfo.converterFactoryBeanName?.let {
                    if (it.isBlank()) null else addConstructorArgReference(it)
                } ?: converterFactory

                beanInfo.nodeBlockingServiceFactory?.let {
                    addConstructorArgValue(it)
                } ?: beanInfo.nodeBlockingServiceFactoryBeanName?.let {
                    if (it.isBlank()) null else addConstructorArgReference(it)
                } ?: nodeBlockingServiceFactory
            }

            val resolvableType: ResolvableType = ResolvableType.forClassWithGenerics(
                ContractBlockingClientFactory::class.java,
                beanInfo.api
            )
            val beanDefinition = defBuilder.beanDefinition as RootBeanDefinition
            beanDefinition.setTargetType(resolvableType)
            beanDefinition.autowireMode = AbstractBeanDefinition.AUTOWIRE_BY_TYPE
            beanDefinition.isAutowireCandidate = true
            registry.registerBeanDefinition(beanInfo.name, beanDefinition)
        }
    }
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        // no op
    }
}
