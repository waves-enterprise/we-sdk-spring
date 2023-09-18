package com.wavesenterprise.sdk.spring.autoconfigure.node.validator

import com.wavesenterprise.sdk.node.client.blocking.credentials.NodeCredentialsProvider
import com.wavesenterprise.sdk.spring.autoconfigure.node.NodeBlockingServiceFactoryAutoConfiguration
import com.wavesenterprise.sdk.spring.autoconfigure.node.holder.NodeBlockingServiceFactoryMapHolder
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@AutoConfigureAfter(NodeBlockingServiceFactoryAutoConfiguration::class)
class NodeConnectionValidatorConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "node", name = ["validation-enabled"], havingValue = "true", matchIfMissing = false)
    fun nodePropertiesValidator(
        context: ConfigurableApplicationContext,
        nodeBlockingServiceFactoryMapHolder: NodeBlockingServiceFactoryMapHolder,
        nodeCredentialsProvider: NodeCredentialsProvider,
    ) = DefaultNodeConnectionValidator(
        nodeBlockingServiceFactoryMapHolder = nodeBlockingServiceFactoryMapHolder,
        nodeCredentialsProvider = nodeCredentialsProvider,
        context = context,
    )
}
