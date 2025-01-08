plugins {
    id("someone.code.library")
}

android {
    namespace = "zzz.me.yang.dev.ext.vm.core"
}

dependencies {
    implementation(projects.entity.paging)

    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)

    implementation(libs.androidUtils)

    implementation(libs.flowmvi.core)

    implementation(libs.koin.core)

    implementation(libs.coroutines.core)

    implementation(libs.compose.runtime)
}
