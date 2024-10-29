plugins {
    id("someone.code.library")
}

android {
    namespace = "zzz.me.yang.dev.ext.coroutines"
}

dependencies {
    implementation(libs.coroutines.core)
}
