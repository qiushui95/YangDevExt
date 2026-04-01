import com.android.build.api.dsl.LibraryExtension
import nbe.someone.code.configSamePlugins
import nbe.someone.code.configSpotless
import nbe.someone.code.configureKotlin
import nbe.someone.code.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import java.net.URI

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
            }

            extensions.configure<PublishingExtension> {
                repositories {
                    maven {
                        name = "Hero"
                        url = URI.create(System.getenv("HERO_NEXUS_URL"))
                        credentials {
                            username = System.getenv("HERO_NEXUS_USERNAME")
                            password = System.getenv("HERO_NEXUS_PASSWORD")
                        }
                    }
                }
            }

            configSpotless()

            configureKotlin()
        }
    }
}
