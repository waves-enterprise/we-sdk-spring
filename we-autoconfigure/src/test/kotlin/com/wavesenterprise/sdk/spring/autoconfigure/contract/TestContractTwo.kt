package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.wavesenterprise.sdk.contract.api.annotation.ContractInit
import com.wavesenterprise.sdk.contract.api.annotation.InvokeParam

interface TestContractTwo {
    @ContractInit
    fun init(@InvokeParam("init") str: String)
}

class TestContractTwoImpl : TestContractTwo {
    override fun init(str: String) {}
}
