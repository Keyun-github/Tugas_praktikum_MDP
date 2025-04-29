// build.gradle.kts (Module :app)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt") // <-- TAMBAHKAN BARIS INI untuk mengaktifkan kapt
}

android {
    namespace = "com.example.week6" // Pastikan ini sesuai dengan namespace Anda
    compileSdk = 35 // Atau SDK target Anda

    defaultConfig {
        applicationId = "com.example.week6"
        minSdk = 24
        targetSdk = 35 // Sesuaikan jika perlu
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Opsi untuk Room (jika diperlukan skema ekspor)
        // javaCompileOptions {
        //     annotationProcessorOptions {
        //         arguments += mapOf(
        //             "room.schemaLocation" to "$projectDir/schemas".toString()
        //         )
        //     }
        // }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    // Aktifkan View Binding
    buildFeatures {
        viewBinding = true
    }
    // packagingOptions { // Tambahkan jika Anda mengalami masalah duplikasi file
    //     resources {
    //         excludes += "/META-INF/{AL2.0,LGPL2.1}"
    //     }
    // }
}

dependencies {

    // --- Dependensi Inti ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity) // Seharusnya activity-ktx jika alias benar
    implementation(libs.androidx.constraintlayout)

    // --- Dependensi Fragment KTX ---
    implementation(libs.androidx.fragment.ktx)

    // --- Dependensi Lifecycle (ViewModel & LiveData) ---
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.common.java8) // Opsional

    // --- Dependensi Room Database ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler) // Sekarang 'kapt' seharusnya dikenali

    // --- Dependensi Coroutines ---
    implementation(libs.kotlinx.coroutines.android)

    // --- Dependensi Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}