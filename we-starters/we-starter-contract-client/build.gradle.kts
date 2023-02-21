description = "Starter for using we-contract-client."

dependencies {
    api(project(":we-autoconfigure"))
    api("com.wavesenterprise:we-node-client-grpc-blocking-client")
    api("com.wavesenterprise:we-node-client-feign-client")
    api("com.wavesenterprise:we-contract-sdk-blocking-client")
    api("com.wavesenterprise:we-tx-signer-node")
    api("io.github.openfeign:feign-core")
}
