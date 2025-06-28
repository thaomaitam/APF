plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.rikka.refine) // Plugin để sử dụng API ẩn
}

android {
    namespace = "com.KTA.APF.xposed"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    // API của Xposed chỉ cần lúc biên dịch, không đóng gói vào APK
    compileOnly(libs.xposed.api)

    // Thư viện để parse JSON cấu hình
    implementation(libs.kotlinx.serialization.json)

    // Thư viện của Rikka để làm việc với API ẩn
    implementation(libs.rikka.hidden.compat)
    compileOnly(libs.rikka.hidden.stub)
}