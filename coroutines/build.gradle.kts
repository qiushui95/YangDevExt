plugins {
    id("someone.code.kotlin")
}

dependencies {
    implementation(libs.coroutines.core)

    testImplementation(libs.coroutines.test)

    testImplementation(libs.jodatime)

    testImplementation(kotlin("test"))
    testImplementation(libs.test.junit5.api)
    testImplementation(libs.test.junit5.params)
    testRuntimeOnly(libs.test.junit5.engine)
}

tasks.test {
    useJUnitPlatform()
}
