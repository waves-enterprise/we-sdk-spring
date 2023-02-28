package com.wavesenterprise.sdk.spring.autoconfigure.contract.update

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

class ContractsUpdateListener(
    private val contractsUpdateHandler: ContractsUpdateHandler,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun onStartApp() {
        contractsUpdateHandler.handle()
    }
}
