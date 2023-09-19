package com.wavesenterprise.sdk.spring.autoconfigure.node.holder

import com.google.common.collect.ImmutableMap

class DefaultNodeBlockingServiceFactoryMapHolder(
    val map: ImmutableMap<String, NodeClient>,
) : NodeBlockingServiceFactoryMapHolder {

    override fun get(nodeAlias: String): NodeClient? = map[nodeAlias]

    override fun getAll() = map
}
