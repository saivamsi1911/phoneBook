@file:Suppress("DEPRECATION", "UnstableApiUsage")

import com.android.build.gradle.internal.api.ApkVariantOutputImpl

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    compileSdkVersion(libs.versions.compileSdk.get().toInt())

    defaultConfig {
        applicationId = "com.project.phonebook"
        minSdkVersion(libs.versions.minSdk.get().toInt())
        targetSdkVersion(libs.versions.targetSdk.get().toInt())
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")// Only supports v7a and v8a
        }
        vectorDrawables {
            useSupportLibrary = true
        }
        multiDexEnabled = true
    }
    flavorDimensions += listOf("default")
    buildTypes {

        release {

            isMinifyEnabled = false

            proguardFiles(

                getDefaultProguardFile("proguard-android-optimize.txt"),

                "proguard-rules.pro"

            )

        }

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        freeCompilerArgs = listOf(
            "-Xstring-concat=inline"
        )
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeKotlinVersion.get()
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
        compose = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
        animationsDisabled = true
    }
    hilt {
        enableTransformForLocalTests = true
    }
    configurations {
        //implementation.get().exclude(mapOf("group" to "org.jetbrains", "module" to "annotations"))

    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    namespace = "com.project.phonebook"

    applicationVariants.all {
        var nameUpdate = mergedFlavor.versionName.orEmpty()
        var codeUpdate = mergedFlavor.versionCode ?: 0
        if (buildType.isDebuggable) {
            nameUpdate = libs.versions.versionNameDebug.get()
            codeUpdate = libs.versions.versionCodeDebug.get().toInt()
        }
        outputs.map { it as? ApkVariantOutputImpl }
            .forEach { variant ->
                variant?.versionNameOverride = nameUpdate
                variant?.versionCodeOverride = codeUpdate
                variant?.outputFileName = "app-${variant?.baseName}.apk"
            }
    }

    bundle {
        language {
            enableSplit = false
        }
    }

}

dependencies {


    implementation(libs.kotlin.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.bundles.navigation)

    // Image Library

    implementation(libs.work.runtime.ktx)
    implementation(libs.kotlin.coroutine.core)

    //Lifecycle library
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.service)

    // Hilt Dependency
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    implementation(libs.android.hilt.work)
    kapt(libs.android.hilt.compiler)





    implementation(libs.retrofit.gson)


    implementation(libs.paging.runtime.ktx)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.compose.ui.tooling)

    androidTestImplementation(libs.bundles.androidTest)
    debugImplementation(libs.ui.frag.test)

    androidTestImplementation(libs.ui.hilt)
//    androidTestImplementation(libs.room.runtime)
//    androidTestImplementation(libs.room.ktx)
    kaptAndroidTest(libs.room.compiler)

    testImplementation(libs.bundles.test)
    implementation(libs.paging.compose.v100alpha17)

}