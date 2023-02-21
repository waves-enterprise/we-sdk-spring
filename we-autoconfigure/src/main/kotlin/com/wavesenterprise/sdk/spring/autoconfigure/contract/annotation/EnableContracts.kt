package com.wavesenterprise.sdk.spring.autoconfigure.contract.annotation

import com.wavesenterprise.sdk.spring.autoconfigure.contract.EnableContractBeanDefinitionRegistrar
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.AliasFor

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Import(
    EnableContractBeanDefinitionRegistrar::class,
)
annotation class EnableContracts(
    @get:AliasFor("contracts")
    val value: Array<Contract> = [],
    @get:AliasFor("value")
    val contracts: Array<Contract> = [],
)
