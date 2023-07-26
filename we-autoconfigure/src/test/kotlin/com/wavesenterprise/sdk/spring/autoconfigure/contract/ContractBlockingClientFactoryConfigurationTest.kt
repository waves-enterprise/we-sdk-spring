package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.wavesenterprise.sdk.contract.client.invocation.factory.ContractBlockingClientFactory
import com.wavesenterprise.sdk.contract.core.converter.factory.ConverterFactory
import com.wavesenterprise.sdk.contract.jackson.JacksonConverterFactory
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.spring.autoconfigure.contract.annotation.Contract
import com.wavesenterprise.sdk.spring.autoconfigure.contract.annotation.EnableContracts
import com.wavesenterprise.sdk.spring.autoconfigure.contract.properties.ContractsProperties
import com.wavesenterprise.sdk.spring.autoconfigure.node.NodeBlockingServiceFactoryAutoConfiguration
import com.wavesenterprise.sdk.spring.autoconfigure.node.service.NodeServicesAutoConfiguration
import com.wavesenterprise.sdk.tx.signer.TxSigner
import com.wavesenterprise.sdk.tx.signer.node.factory.TxServiceTxSignerFactory
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.support.BeanDefinitionOverrideException
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class ContractBlockingClientFactoryConfigurationTest {

    private val applicationContextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                NodeBlockingServiceFactoryAutoConfiguration::class.java,
                NodeServicesAutoConfiguration::class.java,
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

    @Test
    fun `should use EnabledContractBeanInfos from user config for contract factory creation`() {
        applicationContextRunner.withUserConfiguration(
            UserEnabledContractBeanInfoConfig::class.java
        ).run { context ->
            assertThat(context).hasBean("testOne")
            assertThat(context).hasBean("testTwo")
        }
    }

    @Test
    fun `should create EnabledContractBeanInfos from user annotation for contract factory creation`() {
        applicationContextRunner.withUserConfiguration(
            AnnotationConfig::class.java,
            CustomBeansInContextConfiguration::class.java
        ).run { context ->
            assertThat(context).hasBean("testOne")
            assertThat(context).hasBean("testTwo")
        }
    }

    @Test
    fun `should configure contract clients from several configurations`() {
        applicationContextRunner.withUserConfiguration(
            AppStarterConfig::class.java,
            AppConfig::class.java,
        ).run { context ->
            context.getBeansOfType(ContractBlockingClientFactory::class.java).keys.let {
                assertThat(it).contains("starter-contract")
                assertThat(it).contains("app-contract")
            }
        }
    }

    @Test
    fun `should throw exception when duplicate contract clients in several configuration`() {
        applicationContextRunner.withUserConfiguration(
            AppStarterConfig::class.java,
            AppWithDuplicateContractConfig::class.java,
        ).run { context ->
            assertThat(context.startupFailure).isInstanceOf(BeanDefinitionOverrideException::class.java)
        }
    }

    companion object {

        @Configuration
        @EnableContracts(
            contracts = [
                Contract(
                    api = TestContractOne::class,
                    impl = TestContractOneImpl::class,
                    name = "testOne",
                    localValidationEnabled = false,
                ),
                Contract(
                    api = TestContractTwo::class,
                    impl = TestContractTwoImpl::class,
                    name = "testTwo",
                    localValidationEnabled = false,
                ),
            ]
        )
        class JacksonModuleConfiguration {

            @Bean
            fun jacksonKotlinModule(): KotlinModule = kotlinModule()

            @Bean
            fun jacksonJavaTimeModule(): JavaTimeModule = JavaTimeModule()

            @Bean
            fun txSigner(): TxSigner = mockk()
        }
    }

    @Configuration
    class UserEnabledContractBeanInfoConfig {

        val nodeBlockingServiceFactory: NodeBlockingServiceFactory = mockk<NodeBlockingServiceFactory>().also {
            every { it.txService() } returns mockk()
            every { it.addressService() } returns mockk()
            every { it.blocksService() } returns mockk()
            every { it.contractService() } returns mockk()
            every { it.nodeInfoService() } returns mockk()
            every { it.privacyService() } returns mockk()
        }

        val converterFactory: ConverterFactory = mockk<ConverterFactory>().also {
            every { it.toDataValueConverter() } returns mockk()
            every { it.fromDataEntryConverter() } returns mockk()
        }

        @Bean
        fun enabledContractBeanInfo1() = EnabledContractsBeanInfo(
            api = TestContractOne::class.java,
            impl = TestContractOneImpl::class.java,
            name = "testOne",
            txSigner = mockk(),
            nodeBlockingServiceFactory = nodeBlockingServiceFactory,
            converterFactory = converterFactory,
        )

        @Bean
        fun enabledContractBeanInfo2() = EnabledContractsBeanInfo(
            api = TestContractOne::class.java,
            impl = TestContractOneImpl::class.java,
            name = "testTwo",
            txSigner = mockk(),
            nodeBlockingServiceFactory = nodeBlockingServiceFactory,
            converterFactory = converterFactory,
        )
    }

    @Configuration
    @EnableContracts(
        contracts = [
            Contract(
                api = TestContractOne::class,
                impl = TestContractOneImpl::class,
                name = "testOne",
                txSigner = "customTxSigner",
                nodeBlockingServiceFactory = "customNodeBlockingServiceFactory",
                converterFactory = "customConverterFactory",
                localValidationEnabled = false,
            ),
            Contract(
                api = TestContractTwo::class,
                impl = TestContractTwoImpl::class,
                name = "testTwo",
                txSigner = "customTxSigner",
                nodeBlockingServiceFactory = "customNodeBlockingServiceFactory",
                converterFactory = "customConverterFactory",
                localValidationEnabled = false,
            ),
        ]
    )
    class AnnotationConfig

    @Configuration
    class CustomBeansInContextConfiguration {

        @Bean("customTxSigner")
        fun txSigner(): TxSigner = mockk()

        @Bean("customConverterFactory")
        fun converterFactory(): ConverterFactory = mockk<ConverterFactory>().also {
            every { it.toDataValueConverter() } returns mockk()
            every { it.fromDataEntryConverter() } returns mockk()
        }

        @Bean("customNodeBlockingServiceFactory")
        fun nodeBlockingServiceFactory(): NodeBlockingServiceFactory = mockk<NodeBlockingServiceFactory>().also {
            every { it.txService() } returns mockk()
            every { it.addressService() } returns mockk()
            every { it.blocksService() } returns mockk()
            every { it.contractService() } returns mockk()
            every { it.nodeInfoService() } returns mockk()
            every { it.privacyService() } returns mockk()
            every { it.utilService() } returns mockk()
        }
    }

    @Configuration
    @EnableContracts(
        contracts = [
            Contract(
                api = TestContractOne::class,
                impl = TestContractOneImpl::class,
                name = "starter-contract",
                localValidationEnabled = false,
            )
        ]
    )
    class AppStarterConfig

    @Configuration
    @EnableContracts(
        contracts = [
            Contract(
                api = TestContractTwo::class,
                impl = TestContractTwoImpl::class,
                name = "app-contract",
                localValidationEnabled = false,
            ),
        ]
    )
    class AppConfig {

        @Bean
        fun txSigner(): TxSigner = mockk()
    }

    @Configuration
    @EnableContracts(
        contracts = [
            Contract(
                api = TestContractOne::class,
                impl = TestContractOneImpl::class,
                name = "starter-contract",
                localValidationEnabled = false,
            ),
        ]
    )
    class AppWithDuplicateContractConfig
}
