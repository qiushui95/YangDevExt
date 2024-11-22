package nbe.someone.code

import com.android.build.api.dsl.ApplicationDefaultConfig
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.kotlin

internal fun Project.configAndroidTest(defaultConfig: ApplicationDefaultConfig) {

    configJavaTest()

    defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    val runnerBuilder = "de.mannodermaus.junit5.AndroidJUnit5Builder"

    defaultConfig.testInstrumentationRunnerArguments["runnerBuilder"] = runnerBuilder

    val configurationParameterList = listOf(
        "junit.jupiter.execution.parallel.enabled=true",
        "junit.jupiter.execution.parallel.mode.default=concurrent"
    )

    val parameters = configurationParameterList.joinToString(",")

    defaultConfig.testInstrumentationRunnerArguments["configurationParameters"] = parameters

    val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

    project.dependencies {
        val androidTestI = "androidTestImplementation"
        val androidTestR = "androidTestRuntimeOnly"
        val debugI = "debugImplementation"

        add(androidTestI, libs.findLibrary("androidtest-uiautomator").get())
        add(debugI, libs.findLibrary("compose-test-manifest").get())
        add(androidTestI, libs.findLibrary("androidtest-core").get())
        add(androidTestI, libs.findLibrary("androidtest-junit-ext").get())
        add(androidTestI, libs.findLibrary("androidtest-espresso").get())

        add(androidTestI, kotlin("test"))
        add(androidTestI, libs.findLibrary("test-junit5-api").get())
        add(androidTestI, libs.findLibrary("test-junit5-params").get())
        add(androidTestR, libs.findLibrary("test-junit5-engine").get())
    }
}