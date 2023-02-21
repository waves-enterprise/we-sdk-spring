package com.wavesenterprise.sdk.spring.autoconfigure.contract

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.wavesenterprise.sdk.contract.core.converter.factory.ConverterFactory
import com.wavesenterprise.sdk.contract.jackson.JacksonConverterFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(ObjectMapper::class)
class JacksonConverterFactoryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun objectMapper(
        applicationContext: ApplicationContext,
    ): ObjectMapper =
        ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).apply {
                registerModules(applicationContext.getBeansOfType(Module::class.java).values)
            }

    @Bean
    @ConditionalOnMissingBean
    fun converterFactory(
        objectMapper: ObjectMapper,
    ): ConverterFactory = JacksonConverterFactory(
        objectMapper = objectMapper,
    )
}
