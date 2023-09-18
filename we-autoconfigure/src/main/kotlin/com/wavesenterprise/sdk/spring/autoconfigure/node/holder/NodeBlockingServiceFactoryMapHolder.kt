package com.wavesenterprise.sdk.spring.autoconfigure.node.holder

interface NodeBlockingServiceFactoryMapHolder {
    fun get(nodeAlias: String): NodeClient?
    fun getAll(): Map<String, NodeClient>
}
