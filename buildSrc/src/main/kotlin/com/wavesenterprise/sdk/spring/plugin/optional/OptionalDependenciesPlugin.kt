package com.wavesenterprise.sdk.spring.plugin.optional

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer


/**
 * OptionalDependenciesPlugin from spring-boot
 * @see org.springframework.boot.build.optional.OptionalDependenciesPlugin
 */
class OptionalDependenciesPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val optional: Configuration = project.getConfigurations().create(OPTIONAL_CONFIGURATION_NAME)
        optional.setCanBeConsumed(false)
        optional.setCanBeResolved(false)
        project.getPlugins().withType(JavaPlugin::class.java) {
            val sourceSets: SourceSetContainer =
                project.getExtensions().getByType(JavaPluginExtension::class.java).getSourceSets()
            sourceSets.all {
                project.getConfigurations().getByName(this.compileClasspathConfigurationName).extendsFrom(optional)
                project.getConfigurations().getByName(this.runtimeClasspathConfigurationName).extendsFrom(optional)
            }
        }
    }

    companion object {
        private const val OPTIONAL_CONFIGURATION_NAME = "optional"
    }
}
