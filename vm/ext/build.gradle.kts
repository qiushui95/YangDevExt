plugins {
    id("someone.code.library")
    id("someone.code.compose")
}

android {
    namespace = "zzz.me.yang.dev.ext.vm.ext"
}

dependencies {
    implementation(projects.vm.core)

    implementation(libs.lifecycle.viewmodel)

    api(libs.flowmvi.core)
    api(libs.flowmvi.android)
    api(libs.flowmvi.compose)

    implementation(libs.compose.runtime)
}
