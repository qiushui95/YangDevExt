import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.LibraryExtension
import nbe.someone.code.configureAndroidCompose
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

@Suppress("unused")
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extension = extensions.runCatching { getByType<LibraryExtension>() }
                .getOrNull() ?: extensions.runCatching { getByType<ApplicationExtension>() }
                .getOrNull() ?: return

            configureAndroidCompose(extension)
        }
    }

}