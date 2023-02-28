package com.wavesenterprise.sdk.spring.autoconfigure.contract.update

import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.client.blocking.credentials.NodeCredentialsProvider
import com.wavesenterprise.sdk.node.client.blocking.tx.TxService
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.domain.Fee
import com.wavesenterprise.sdk.node.domain.TxVersion
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractImage
import com.wavesenterprise.sdk.node.domain.contract.ContractImageHash
import com.wavesenterprise.sdk.node.domain.contract.ContractName
import com.wavesenterprise.sdk.node.domain.sign.UpdateContractSignRequest
import com.wavesenterprise.sdk.node.domain.tx.Tx
import com.wavesenterprise.sdk.spring.autoconfigure.contract.ContractsProperties
import com.wavesenterprise.sdk.tx.signer.node.credentials.Credentials
import com.wavesenterprise.sdk.tx.signer.node.factory.TxServiceTxSignerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ContractsUpdateHandler(
    private val contractsProperties: ContractsProperties,
    private val contractIdProvider: ContractIdProvider,
    private val nodeCredentialsProvider: NodeCredentialsProvider,
    private val txSignerFactory: TxServiceTxSignerFactory,
    private val txService: TxService,
    private val contractService: ContractService,
) {

    private val log: Logger = LoggerFactory.getLogger(ContractsUpdateHandler::class.java)

    fun handle() {
        for ((contractName, contractProps) in contractsProperties.config) {
            if (!contractProps.autoUpdate.enabled) {
                log.debug("Contract $contractName auto update is disabled")
                continue
            }
            if (!canUpdate(contractProps)) {
                log.warn(
                    "Unable to update contract $contractName cause of" +
                        " missing image name or image hash: $contractProps"
                )
                continue
            }
            updateContracts(ContractName.fromString(contractName), contractProps)
        }
    }

    private fun canUpdate(contractProps: ContractsProperties.Properties) =
        !contractProps.image.isNullOrBlank() && !contractProps.imageHash.isNullOrBlank()

    private fun updateContracts(
        contractName: ContractName,
        contractProps: ContractsProperties.Properties,
    ) {
        val contactIds = contractIdProvider.getIds(contractName)
        if (contactIds.isEmpty()) {
            log.warn(
                "Unable to update contract $contractName cause of" +
                    " no contract IDs found"
            )
        }
        contactIds.forEach {
            update(it, contractName, contractProps)
        }
    }

    private fun update(
        contractId: ContractId,
        contractName: ContractName,
        contractProps: ContractsProperties.Properties,
    ) {
        val helper = ContractUpdateHelper(
            contractId = contractId,
            contractImage = ContractImage.fromString(contractProps.image!!),
            contractImageHash = ContractImageHash.fromString(contractProps.imageHash!!),
            txVersion = TxVersion.fromInt(2), // while only 2 version
            fee = Fee.fromInt(contractProps.fee.toInt()),
            senderAddress = Address.fromBase58(contractProps.autoUpdate.contractCreatorAddress!!),
        )
        if (!helper.isContractInfoFound()) {
            log.error(
                "Unable to update contract ${contractName.value} cause of" +
                    " contract info was not found for $contractId"
            )
            return
        }
        if (helper.isNeedToUpdate()) {
            log.info("Updating contract with ID = $contractId")
            try {
                val updateTx = helper.sendUpdateTx()
                log.info(
                    "Contract ${contractName.value} was updated in Tx with ID = ${updateTx.id}," +
                        " image = ${contractProps.image} hash = ${contractProps.imageHash}"
                )
            } catch (ex: Exception) {
                log.error("Update contract failed, ID = $contractId", ex)
            }
        } else {
            log.info("No need to update contract ${contractName.value} with ID = $contractId")
        }
    }

    inner class ContractUpdateHelper(
        private val contractId: ContractId,
        private val contractImage: ContractImage,
        private val contractImageHash: ContractImageHash,
        private val txVersion: TxVersion,
        private val fee: Fee,
        private val senderAddress: Address,
    ) {

        private val contractInfo by lazy { contractService.getContractInfo(contractId) }

        fun isContractInfoFound() = contractInfo.isPresent

        fun isNeedToUpdate() = contractInfo.get().imageHash.value != contractImageHash.value

        fun sendUpdateTx(): Tx =
            txSignerFactory.withCredentials(
                Credentials(
                    senderAddress = senderAddress,
                    password = nodeCredentialsProvider.getPassword(senderAddress),
                )
            ).sign(
                UpdateContractSignRequest(
                    senderAddress = senderAddress,
                    fee = fee,
                    image = contractImage,
                    imageHash = contractImageHash,
                    version = txVersion,
                    contractId = contractId,
                )
            ).run {
                txService.broadcast(this)
            }
    }
}
