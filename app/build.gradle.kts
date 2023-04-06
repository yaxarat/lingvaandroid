plugins {
    id("com.android.application")
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
    kotlin("android")
    kotlin("plugin.serialization")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "dev.atajan.lingva_android"
        minSdk = 26
        targetSdk = 33
        versionCode = 18
        versionName = "1.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            postprocessing {
                isRemoveUnusedCode = false
                isRemoveUnusedResources = true
                isObfuscate = false
                isOptimizeCode = true
            }
        }
        debug { }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packagingOptions { // https://stackoverflow.com/a/47509465/8685398
        resources.excludes.add("META-INF/DEPENDENCIES")
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
    }
    namespace = "dev.atajan.lingva_android"
}

kotlin.sourceSets.all {
    languageSettings.apply {
        optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        optIn("androidx.compose.ui.ExperimentalComposeUiApi")
        optIn("androidx.compose.foundation.ExperimentalFoundationApi")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")

    // Accompanist
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.17.0")

    // Datastore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Result
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.14")

    // Ktor
    implementation("io.ktor:ktor-client-android:2.1.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.1.2")
    implementation("io.ktor:ktor-client-logging-jvm:2.1.2")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:2.1.2")

    // Compose
    implementation("androidx.compose.ui:ui:1.4.1")
    implementation("androidx.compose.material:material:1.4.1")
    implementation("androidx.compose.animation:animation:1.4.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.1")
    implementation("androidx.compose.material3:material3:1.1.0-beta02")

    // note that due to the very large size of this dependency you should make sure to use
    // R8 / ProGuard to remove unused icons from your application.
    implementation("androidx.compose.material:material-icons-extended:1.4.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-android-compiler:2.44")

    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.1")
}
