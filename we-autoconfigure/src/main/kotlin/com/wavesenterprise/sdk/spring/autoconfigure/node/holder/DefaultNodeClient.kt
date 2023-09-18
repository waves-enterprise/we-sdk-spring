package com.wavesenterprise.sdk.spring.autoconfigure.node.holder

import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.client.feign.factory.FeignNodeServiceFactory
import com.wavesenterprise.sdk.node.client.grpc.blocking.factory.GrpcNodeServiceFactory

class DefaultNodeClient(
    private val http: FeignNodeServiceFactory,
    private val grpc: GrpcNodeServiceFactory?,
) : NodeClient {
    override fun getHttpClient(): NodeBlockingServiceFactory = http
    override fun getGrpcClient(): NodeBlockingServiceFactory? = grpc
}
