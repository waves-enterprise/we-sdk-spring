package com.wavesenterprise.sdk.spring.autoconfigure.contract

class EnabledContractsBeanInfo(
    val api: Class<out Class<*>>,
    val impl: Class<out Class<*>>,
    val name: String,
)
