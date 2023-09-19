package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.wavesenterprise.sdk.atomic.AtomicAwareTxSigner
import com.wavesenterprise.sdk.contract.client.invocation.factory.ContractBlockingClientFactory
import com.wavesenterprise.sdk.contract.core.converter.factory.ConverterFactory
import com.wavesenterprise.sdk.contract.core.state.LocalValidationContextManager
import com.wavesenterprise.sdk.spring.autoconfigure.contract.properties.ContractConfigurationPropertiesForSignRequestBuilderFactory
import com.wavesenterprise.sdk.spring.autoconfigure.contract.validation.ApplicationContextAwareContractValidation
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.ApplicationContext
import org.springframework.core.ResolvableType

class ContractBlockingClientFactoryRegistryPostProcessor(
    private val converterFactory: ConverterFactory,
    private val applicationContext: ApplicationContext,
    private val localValidationContextManager: LocalValidationContextManager,
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
                addConstructorArgValue(
                    ApplicationContextAwareContractValidation(
                        contractKey = beanInfo.name,
                        applicationContext = applicationContext,
                    )
                )
                addConstructorArgValue(contractSignRequestBuilderFactory)

                beanInfo.txSigner?.let {
                    addConstructorArgValue(it)
                } ?: beanInfo.txSignerBeanName?.let {
                    if (it.isBlank()) null else addConstructorArgReference(it)
                } ?: try {
                    addConstructorArgValue(applicationContext.getBean(AtomicAwareTxSigner::class.java))
                } catch (ex: BeansException) {
                    addConstructorArgReference("txSigner")
                }

                beanInfo.converterFactory?.let {
                    addConstructorArgValue(it)
                } ?: beanInfo.converterFactoryBeanName?.let {
                    if (it.isBlank()) null else addConstructorArgReference(it)
                } ?: addConstructorArgValue(converterFactory)

                beanInfo.nodeBlockingServiceFactory?.let {
                    addConstructorArgValue(it)
                } ?: beanInfo.nodeBlockingServiceFactoryBeanName?.let {
                    if (it.isBlank()) null else addConstructorArgReference(it)
                } ?: addConstructorArgReference("nodeBlockingServiceFactory")
                addConstructorArgValue(localValidationContextManager)
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
