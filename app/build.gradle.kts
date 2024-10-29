plugins {
    id("someone.code.application")
}

android {
    defaultConfig {
        applicationId = "zzz.me.yang.dev.ext"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }

    namespace = "zzz.me.yang.dev.ext"

    buildTypes {

        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {

}
