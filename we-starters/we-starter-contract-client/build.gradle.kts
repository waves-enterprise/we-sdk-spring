description = "Starter for using we-contract-client."

dependencies {
    api(project(":we-autoconfigure"))
    api("com.wavesenterprise:we-node-client-grpc-blocking-client")
    api("com.wavesenterprise:we-contract-sdk-blocking-client")
    api("io.github.openfeign:feign-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm")
}
