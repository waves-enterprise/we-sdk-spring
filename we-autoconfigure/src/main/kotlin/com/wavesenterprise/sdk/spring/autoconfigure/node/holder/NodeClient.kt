package com.wavesenterprise.sdk.spring.autoconfigure.node.holder

import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory

interface NodeClient {
    fun getHttpClient(): NodeBlockingServiceFactory
    fun getGrpcClient(): NodeBlockingServiceFactory?
}
