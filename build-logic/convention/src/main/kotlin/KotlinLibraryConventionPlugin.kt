import nbe.someone.code.configJavaTest
import nbe.someone.code.configSpotless
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

@Suppress("unused")
class KotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            with(pluginManager) {
                apply("java-library")
                apply(libs.findPlugin("kotlin-jvm").get().get().pluginId)
                apply(libs.findPlugin("spotless").get().get().pluginId)
                apply(libs.findPlugin("maven").get().get().pluginId)
            }

            extensions.configure<KotlinJvmProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                    freeCompilerArgs.add("-Xexplicit-api=strict")
                }
            }

            configSpotless()

            configJavaTest()

            tasks.named<Test>("test") {
                useJUnitPlatform()
            }
        }
    }
}