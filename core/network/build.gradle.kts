plugins {
    id("oldboobstore.android.library")
    id("oldboobstore.detekt")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    // The Compose compiler plugin is applied by the library convention, so the
    // runtime must be on the compile classpath even though this module has no UI.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)

    api(project(":core:model"))

    api(platform(libs.koin.bom))
    api(libs.koin.core)
    implementation(libs.koin.android)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)

    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.koin.test)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
