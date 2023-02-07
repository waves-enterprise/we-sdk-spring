val junitVersion: String by project
val assertjVersion: String by project

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

    optional("com.wavesenterprise:we-node-client-grpc-blocking-client")
    optional("com.wavesenterprise:we-node-client-feign-client")
    optional("io.github.openfeign:feign-core")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.assertj:assertj-core")

    kapt("org.springframework.boot:spring-boot-configuration-processor")

//    implementation("com.wavesenterprise:we-contract-sdk-blocking-client:1.2.0")
//    implementation("com.wavesenterprise:we-tx-signer-node:0.3.14-1f5915a6-SNAPSHOT")
//    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
//    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.2")
//    implementation("commons-codec:commons-codec:1.15")
}
