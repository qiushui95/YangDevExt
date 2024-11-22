import com.android.build.gradle.LibraryExtension
import nbe.someone.code.configAndroidTest
import nbe.someone.code.configSamePlugins
import nbe.someone.code.configSpotless
import nbe.someone.code.configureKotlin
import nbe.someone.code.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

@Suppress("unused")
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configSamePlugins()

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            with(pluginManager) {
                apply(libs.findPlugin("library").get().get().pluginId)
                apply(libs.findPlugin("maven").get().get().pluginId)
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.consumerProguardFiles("consumer-rules.pro")
                defaultConfig.targetSdk = 34

                configAndroidTest(defaultConfig)
            }

            configSpotless()

            configureKotlin()
        }
    }
}