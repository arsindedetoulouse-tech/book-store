plugins {
    id("oldboobstore.android.library")
    id("oldboobstore.detekt")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    api(libs.bundles.compose)
}
