plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.rikka.refine)
}

android {
    namespace = "com.KTA.APF"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.KTA.APF"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true // Cần thiết để sử dụng BuildConfig.DEBUG
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true // Loại bỏ tài nguyên không dùng đến
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Đóng gói module :xposed vào trong APK của :app
    // Điều này đảm bảo khi người dùng cài đặt app, module hook cũng được cài đặt.
    implementation(project(":xposed"))

    // AndroidX & UI Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference.ktx)

    // Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    
    // Kotlinx Serialization for JSON
    implementation(libs.kotlinx.serialization.json)

    // 3rd Party UI/Util
    implementation(libs.kirich1409.viewbindingpropertydelegate)
    implementation(libs.zhanghai.appiconloader) // Để tải icon app hiệu quả

    // Rikka's Libraries for UI
    implementation(libs.rikka.material)
    implementation(libs.rikka.material.preference)
    implementation(libs.rikka.hidden.compat)
    compileOnly(libs.rikka.hidden.stub)
}