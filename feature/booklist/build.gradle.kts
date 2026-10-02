plugins {
    id("oldboobstore.android.library")
    id("oldboobstore.detekt")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose.viewmodel)

    api(libs.orbit.core)
    api(libs.orbit.viewmodel)
    api(libs.orbit.compose)

    testImplementation(libs.junit)
    testImplementation(libs.orbit.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.koin.test)
}
