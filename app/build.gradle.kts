plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    // hilt injection
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.statussaver"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
        buildFeatures{
            viewBinding = true}
    }

    defaultConfig {
        applicationId = "com.example.statussaver"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

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
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // okhttp
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    // Hilt Injection
    implementation("com.google.dagger:hilt-android:2.59.2")
    ksp("com.google.dagger:hilt-android-compiler:2.59.2")
    // Material Design
    implementation("com.google.android.material:material:1.9.0")
    // CircleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")
    // EasyPermissions
    implementation("pub.devrel:easypermissions:3.0.0")
    // Glide
    implementation("com.github.bumptech.glide:glide:4.12.0")
    //Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    // Lottie
    implementation("com.airbnb.android:lottie:3.7.0")
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
    // ROOM Database
    implementation("androidx.room:room-runtime:2.4.2")
    // Firebase
    implementation("com.google.firebase:firebase-analytics:19.0.0")
    // splash
    implementation("androidx.core:core-splashscreen:1.0.0")
    // Zoomable view
    implementation ("com.jsibbold:zoomage:1.3.1")
    // exo player
    implementation ("androidx.media3:media3-exoplayer:1.1.1")
    implementation ("androidx.media3:media3-exoplayer-dash:1.1.1")
    implementation ("androidx.media3:media3-ui:1.1.1")
    //
    implementation ("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation ("com.anggrayudi:storage:1.5.5")
}
