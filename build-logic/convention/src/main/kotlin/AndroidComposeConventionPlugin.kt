import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import nbe.someone.code.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

@Suppress("unused")
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extension = extensions.findByType<LibraryExtension>()
                ?: extensions.findByType<ApplicationExtension>()
                ?: return

            configureAndroidCompose(extension)
        }
    }

}
