plugins {
    id("someone.code.library")
}

android {
    namespace = "zzz.me.yang.dev.ext.fragment"
}

dependencies {
    implementation(libs.lifecycle.runtime)
    implementation(libs.fragment.core)
}
