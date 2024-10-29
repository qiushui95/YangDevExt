plugins {
    id("someone.code.library")
}

android {
    namespace = "zzz.me.yang.dev.ext.moshi"
}

dependencies {
    implementation(libs.moshi.core)
}
