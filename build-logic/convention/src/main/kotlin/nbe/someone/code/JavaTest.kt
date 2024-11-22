package nbe.someone.code

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.kotlin

internal fun Project.configJavaTest() {

    val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

    project.dependencies {
        val testI = "testImplementation"
        val testR = "testRuntimeOnly"

        add(testI, kotlin("test"))
        add(testI, libs.findLibrary("test-junit5-api").get())
        add(testI, libs.findLibrary("test-junit5-params").get())
        add(testR, libs.findLibrary("test-junit5-engine").get())
    }
}