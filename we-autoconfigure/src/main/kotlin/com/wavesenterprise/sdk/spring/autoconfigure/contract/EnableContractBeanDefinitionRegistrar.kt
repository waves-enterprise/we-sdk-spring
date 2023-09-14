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

        val txSignerBeanRef = attrs.getString(Contract::txSignerBeanRef.name)
        val nodeBlockingServiceFactoryBeanRef = attrs.getString(Contract::nodeBlockingServiceFactoryBeanRef.name)
        val converterFactoryBeanRef = attrs.getString(Contract::converterFactoryBeanRef.name)

        val localValidationEnabled = attrs.getBoolean(Contract::localValidationEnabled.name)

        require(api.isAssignableFrom(impl)) {
            "$impl should extend $api"
        }

        return EnabledContractsBeanInfo(
            api = api,
            impl = impl,
            name = name,
            txSignerBeanName = txSignerBeanRef,
            nodeBlockingServiceFactoryBeanName = nodeBlockingServiceFactoryBeanRef,
            converterFactoryBeanName = converterFactoryBeanRef,
            localValidationEnabled = localValidationEnabled,
        )
    }

    companion object {
        const val ENABLED_CONTRACT_BEAN_REGISTRY_NAME = "enabledContractsBeanInfo"
    }
}
