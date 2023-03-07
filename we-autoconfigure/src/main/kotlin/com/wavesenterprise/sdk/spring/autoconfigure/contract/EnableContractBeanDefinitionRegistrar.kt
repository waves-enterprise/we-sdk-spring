package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.wavesenterprise.sdk.spring.autoconfigure.contract.annotation.Contract
import com.wavesenterprise.sdk.spring.autoconfigure.contract.annotation.EnableContracts
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata

class EnableContractBeanDefinitionRegistrar : ImportBeanDefinitionRegistrar {

    override fun registerBeanDefinitions(
        importingClassMetadata: AnnotationMetadata,
        registry: BeanDefinitionRegistry,
    ) {
        val enableContracts = importingClassMetadata.getAnnotationAttributes(
            EnableContracts::class.java.name
        ) as AnnotationAttributes
        val contracts = enableContracts.getAnnotationArray(EnableContracts::contracts.name)
        contracts.map { contract ->
            val beanInfo = createBeanInfo(contract)
            BeanDefinitionBuilder.genericBeanDefinition(EnabledContractsBeanInfo::class.java) {
                beanInfo
            }.also {
                registry.registerBeanDefinition(
                    "${beanInfo.name}_$ENABLED_CONTRACT_BEAN_REGISTRY_NAME", it.beanDefinition
                )
            }
        }
    }

    private fun createBeanInfo(
        attrs: AnnotationAttributes,
    ): EnabledContractsBeanInfo {
        val api = attrs.getClass<Class<*>>(Contract::api.name)
        val impl = attrs.getClass<Class<*>>(Contract::impl.name)
        val name = attrs.getString(Contract::name.name)

        val txSigner = attrs.getString(Contract::txSigner.name)
        val nodeBlockingServiceFactory = attrs.getString(Contract::nodeBlockingServiceFactory.name)
        val converterFactory = attrs.getString(Contract::converterFactory.name)

        require(api.isAssignableFrom(impl)) {
            "$impl should extend $api"
        }

        return EnabledContractsBeanInfo(
            api = api,
            impl = impl,
            name = name,
            txSignerBeanName = txSigner,
            nodeBlockingServiceFactoryBeanName = nodeBlockingServiceFactory,
            converterFactoryBeanName = converterFactory,
        )
    }

    companion object {
        const val ENABLED_CONTRACT_BEAN_REGISTRY_NAME = "enabledContractsBeanInfo"
    }
}
