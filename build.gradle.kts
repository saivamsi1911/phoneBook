// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    //alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    //alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
//    id("org.jacoco:org.jacoco.core") version "0.8.7" apply false
//    id("org.sonarsource.scanner.gradle") version "2.7.1" apply false
}

tasks {
    register("clean", Delete::class) {
        delete(layout.buildDirectory)
    }
}