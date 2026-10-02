plugins {
    `kotlin-dsl`
}

group = "com.arsinde.oldboobstore.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("verifyModuleLayers") {
            id = "oldboobstore.verify.module.layers"
            implementationClass =
                "com.arsinde.oldboobstore.buildlogic.VerifyModuleLayersPlugin"
        }
        register("androidApplication") {
            id = "oldboobstore.android.application"
            implementationClass =
                "com.arsinde.oldboobstore.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "oldboobstore.android.library"
            implementationClass =
                "com.arsinde.oldboobstore.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("kotlinLibrary") {
            id = "oldboobstore.kotlin.library"
            implementationClass =
                "com.arsinde.oldboobstore.buildlogic.KotlinLibraryConventionPlugin"
        }
        register("detekt") {
            id = "oldboobstore.detekt"
            implementationClass =
                "com.arsinde.oldboobstore.buildlogic.DetektConventionPlugin"
        }
    }
}