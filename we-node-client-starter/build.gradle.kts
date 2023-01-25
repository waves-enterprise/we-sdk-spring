
plugins {
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("org.springframework.boot:spring-boot")

    implementation("com.wavesenterprise:we-node-client-grpc-blocking-client:0.3.14-1f5915a6-SNAPSHOT")
    implementation("com.wavesenterprise:we-node-client-feign-client:0.3.14-1f5915a6-SNAPSHOT")
}
