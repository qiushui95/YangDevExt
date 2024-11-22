package nbe.someone.code

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Configure base Kotlin with Android options
 */
internal fun configureKotlinAndroid(commonExtension: CommonExtension<*, *, *, *, *, *>) {

    commonExtension.apply {
        compileSdk = 35

        defaultConfig {
            minSdk = 26
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }
}

internal fun Project.configureKotlin( strictMode: Boolean = true) {
    kotlinOptions {
        compilerOptions {
            this.jvmTarget.set(JvmTarget.JVM_21)
            if (strictMode) {
                freeCompilerArgs.add("-Xexplicit-api=strict")
            }
        }
    }
}

internal fun Project.kotlinOptions(block: KotlinAndroidProjectExtension.() -> Unit) {
    (this as ExtensionAware).extensions.configure("kotlin", block)
}
