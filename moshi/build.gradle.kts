plugins {
    id("someone.code.kotlin")
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.moshi.core)

    kspTest(libs.moshi.ksp)
}
