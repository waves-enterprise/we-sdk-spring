package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit
import com.wavesenterprise.sdk.contract.client.invocation.factory.ContractBlockingClientFactory
import com.wavesenterprise.sdk.contract.jackson.JacksonConverterFactory
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.spring.autoconfigure.contract.annotation.Contract
import com.wavesenterprise.sdk.spring.autoconfigure.contract.annotation.EnableContracts
import com.wavesenterprise.sdk.spring.autoconfigure.node.NodeBlockingServiceFactoryAutoConfiguration
import com.wavesenterprise.sdk.tx.signer.node.factory.TxServiceTxSignerFactory
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class ContractBlockingClientFactoryConfigurationTest {

    private val applicationContextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                NodeBlockingServiceFactoryAutoConfiguration::class.java,
                ContractAutoConfiguration::class.java,
            )
        )

    @Test
    fun `should create contract client for two contracts and other necessary beans`() {
        applicationContextRunner
            .withUserConfiguration(JacksonModuleConfiguration::class.java)
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context).hasSingleBean(NodeBlockingServiceFactory::class.java)
                assertThat(context).hasBean("testOne")
                assertThat(context).hasBean("testTwo")

                assertThat(context).hasSingleBean(ObjectMapper::class.java)
                val registeredModules = context.getBean(ObjectMapper::class.java).registeredModuleIds
                assertThat(registeredModules.contains("com.fasterxml.jackson.module.kotlin.KotlinModule")).isTrue
                assertThat(registeredModules.contains("jackson-datatype-jsr310")).isTrue

                assertThat(context).hasSingleBean(JacksonConverterFactory::class.java)
                assertThat(context).hasSingleBean(TxServiceTxSignerFactory::class.java)
            }
    }

    @Test
    fun `should throw exception when call contract which not configured by ContractSignRequestBuilder`() {
        applicationContextRunner
            .withUserConfiguration(JacksonModuleConfiguration::class.java)
            .run { context ->
                val contractName = "testOne"
                val contractClient = context.getBean(contractName) as ContractBlockingClientFactory<*>
                assertThrows<IllegalArgumentException> {
                    contractClient.executeContract(txSigner = mockk()) { contract ->
                        (contract as TestContractOne).init("test")
                    }
                }.also { ex ->
                    assertThat(ex.message)
                        .isEqualTo("Couldn't find contract config for contract with name = '$contractName'")
                }
            }
    }

    @Test
    fun `should map legacy config and start with legacy mode`() {
        applicationContextRunner
            .withPropertyValues("contracts.legacy-mode=true")
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context).hasSingleBean(ContractsProperties::class.java)
            }
    }

    companion object {
        interface TestContractOne {

            @ContractInit
            fun init(str: String)
        }

        class TestContractOneImpl : TestContractOne {
            override fun init(str: String) {}
        }

        interface TestContractTwo
        class TestContractTwoImpl : TestContractTwo

        @Configuration
        @EnableContracts(
            contracts = [
                Contract(
                    api = TestContractOne::class,
                    impl = TestContractOneImpl::class,
                    name = "testOne",
                ),
                Contract(
                    api = TestContractTwo::class,
                    impl = TestContractTwoImpl::class,
                    name = "testTwo",
                ),
            ]
        )
        class JacksonModuleConfiguration {

            @Bean
            fun jacksonKotlinModule(): KotlinModule = kotlinModule()

            @Bean
            fun jacksonJavaTimeModule(): JavaTimeModule = JavaTimeModule()
        }
    }
}
