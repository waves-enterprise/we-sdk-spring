package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.wavesenterprise.sdk.contract.api.annotation.ContractAction
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit
import com.wavesenterprise.sdk.contract.api.annotation.InvokeParam

interface TestContractOne {
    @ContractInit
    fun init(@InvokeParam("init") str: String)
    @ContractAction
    fun call(@InvokeParam("action") str: String)
}

class TestContractOneImpl : TestContractOne {
    override fun init(str: String) {}
    override fun call(str: String) {}
}
