plugins {
    `maven-publish`
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        project.rootProject.subprojects.forEach { project ->
            api(project)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("we-sdk-spring-bom") {
            from(components["javaPlatform"])
        }
    }
}
