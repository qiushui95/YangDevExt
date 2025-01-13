import com.android.build.api.dsl.ApplicationExtension
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
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configSamePlugins()

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            with(pluginManager) {
                apply(libs.findPlugin("application").get().get().pluginId)
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 34
                defaultConfig.versionCode = 1
                defaultConfig.versionName = "1.0.0"
            }

            configureKotlin(strictMode = false)

            configSpotless()
        }
    }
}
