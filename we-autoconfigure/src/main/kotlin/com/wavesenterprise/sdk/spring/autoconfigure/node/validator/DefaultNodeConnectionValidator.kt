package com.wavesenterprise.sdk.spring.autoconfigure.node.validator

import com.wavesenterprise.sdk.node.client.blocking.credentials.NodeCredentialsProvider
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.client.blocking.node.NodeInfoService
import com.wavesenterprise.sdk.node.domain.Fee
import com.wavesenterprise.sdk.node.domain.Hash.Companion.base58StrHash
import com.wavesenterprise.sdk.node.domain.NodeOwner
import com.wavesenterprise.sdk.node.domain.PolicyDescription
import com.wavesenterprise.sdk.node.domain.PolicyId.Companion.base58PolicyId
import com.wavesenterprise.sdk.node.domain.PolicyName
import com.wavesenterprise.sdk.node.domain.privacy.PolicyItemRequest
import com.wavesenterprise.sdk.node.domain.sign.CreatePolicySignRequest
import com.wavesenterprise.sdk.spring.autoconfigure.node.holder.NodeBlockingServiceFactoryMapHolder
import feign.FeignException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.EventListener
import kotlin.system.exitProcess

class DefaultNodeConnectionValidator(
    private val nodeBlockingServiceFactoryMapHolder: NodeBlockingServiceFactoryMapHolder,
    private val nodeCredentialsProvider: NodeCredentialsProvider,
    private val context: ConfigurableApplicationContext,
) : NodeConnectionValidator {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    override fun handleApplicationReadyEvent() {
        nodeBlockingServiceFactoryMapHolder.getAll().forEach { (nodeAlias, client) ->
            checkNodeHealth(nodeAlias = nodeAlias, client = client.getHttpClient())
            checkNodePrivacyHealth(nodeAlias = nodeAlias, client = client.getHttpClient())
        }
    }

    private fun checkNodeHealth(nodeAlias: String, client: NodeBlockingServiceFactory) {
        try {
            val nodeInfoService: NodeInfoService = client.nodeInfoService()
            val nodeOwner: NodeOwner = nodeInfoService.getNodeOwner()
            val txService = client.txService()
            txService.sign(
                CreatePolicySignRequest(
                    senderAddress = nodeOwner.address,
                    description = PolicyDescription("node health check"),
                    policyName = PolicyName("node health check"),
                    owners = listOf(nodeOwner.address),
                    recipients = listOf(nodeOwner.address),
                    password = nodeCredentialsProvider.getPassword(nodeOwner.address),
                    fee = Fee.fromInt(0),
                )
            )
        } catch (ex: Exception) {
            exitApplication(nodeAlias = nodeAlias, ex = ex)
        }
    }

    private fun checkNodePrivacyHealth(nodeAlias: String, client: NodeBlockingServiceFactory) {
        try {
            val privacyService = client.privacyService()
            privacyService.info(
                request = PolicyItemRequest(
                    policyId = MAGIC_POLICY_ID.base58PolicyId,
                    dataHash = MAGIC_HASH.base58StrHash,
                )
            )
        } catch (ex: FeignException) {
            if (ex.status() != NODE_NOT_FOUND_STATUS)
                exitApplication(nodeAlias = nodeAlias, ex = ex)
        } catch (ex: Exception) {
            exitApplication(nodeAlias = nodeAlias, ex = ex)
        }
    }

    private fun exitApplication(nodeAlias: String, ex: Exception) {
        log.error("Node properties validation failed! [nodeAlias = '$nodeAlias']", ex)

        SpringApplication.exit(context, ExitCodeGenerator { 1 }).also { exitProcess(it) }
    }

    companion object {
        private const val NODE_NOT_FOUND_STATUS = 400
        private const val MAGIC_POLICY_ID = "m8snUcMAihYN35RnqHETHxctFeCP1Roef4GTpnAugPYSrHUGG"
        private const val MAGIC_HASH = "Rm59kWzNUg7mge3GyFR41pEtXshiHU8P6jadHmmxV38yhpZrs"
    }
}
