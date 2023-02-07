plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("jvm") version "1.5.21"
}

group = "com.wavesenterprise.sdk.spring.plugin"
version = "1.0"

repositories {
    mavenCentral()
}


gradlePlugin {
    plugins {
        create("optionalDependencyPlugin") {
            id = "com.wavesenterprise.sdk.spring.plugin.optional"
            implementationClass = "com.wavesenterprise.sdk.spring.plugin.optional.OptionalDependenciesPlugin"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
}
