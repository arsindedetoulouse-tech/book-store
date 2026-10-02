plugins {
    id("oldboobstore.android.library")
    id("oldboobstore.detekt")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    api(libs.bundles.compose)
    // Destinations are NavKey values; the host is provided by each feature and by :app.
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.navigation3.ui)
}
