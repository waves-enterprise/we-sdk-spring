package com.wavesenterprise.sdk.spring.autoconfigure.contract.annotation

import kotlin.reflect.KClass

annotation class Contract(
    val api: KClass<*>,
    val impl: KClass<*>,
    val name: String,
)
