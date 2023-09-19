description = "Starter for using we-node-client."

dependencies {
    api(project(":we-autoconfigure"))
    api("com.wavesenterprise:we-tx-signer-api")
    api("com.wavesenterprise:we-tx-signer-node")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm")
}
