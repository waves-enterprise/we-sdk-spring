package com.wavesenterprise.sdk.spring.autoconfigure.contract

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
        val contracts = enableContracts.getAnnotationArray("contracts")
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
        val api = attrs.getClass<Class<*>>("api")
        val impl = attrs.getClass<Class<*>>("impl")
        val name = attrs.getString("name")
        require(api.isAssignableFrom(impl)) {
            "$impl should extend $api"
        }

        return EnabledContractsBeanInfo(
            api = api,
            impl = impl,
            name = name,
        )
    }

    companion object {
        const val ENABLED_CONTRACT_BEAN_REGISTRY_NAME = "enabledContractsBeanInfo"
    }
}
