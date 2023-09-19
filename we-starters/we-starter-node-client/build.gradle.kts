description = "Starter for using we-node-client."

dependencies {
    api(project(":we-autoconfigure"))
    api("com.wavesenterprise:we-node-client-grpc-blocking-client")
    api("com.wavesenterprise:we-node-client-feign-client")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm")
}
