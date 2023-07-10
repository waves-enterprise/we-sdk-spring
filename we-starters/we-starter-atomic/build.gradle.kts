description = "Starter for using we-atomic."

dependencies {
    api(project(":we-autoconfigure"))
    api("com.wavesenterprise:we-atomic")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm")
}
