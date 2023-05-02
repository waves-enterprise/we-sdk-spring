val kotlinModuleVersion: String by project

plugins {
    kotlin("kapt")
    kotlin("plugin.spring")
    id("com.wavesenterprise.sdk.spring.plugin.optional")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm")

    optional("com.wavesenterprise:we-node-client-grpc-blocking-client")
    optional("com.wavesenterprise:we-node-client-feign-client")
    optional("com.wavesenterprise:we-contract-sdk-blocking-client")
    optional("com.wavesenterprise:we-tx-signer-node")
    optional("io.github.openfeign:feign-core")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.assertj:assertj-core")
    testImplementation("io.mockk:mockk")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:$kotlinModuleVersion")

    kapt("org.springframework.boot:spring-boot-configuration-processor")
}
