plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.velox"
    compileSdk = 35



    defaultConfig {
        applicationId = "com.example.velox"
        minSdk = 24
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.7.0-alpha03"
    }


}

dependencies {

    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.compiler)
    val kalendarVersion = "2.0.0-RC1"

    implementation("com.airbnb.android:lottie-compose:6.3.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.leanback)
    implementation(libs.glide)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.recyclerview)
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    implementation(libs.accompanist.systemuicontroller)
    implementation("androidx.navigation:navigation-compose:2.9.0") // or latest version
    implementation("androidx.compose.material:material-icons-extended")


    implementation("io.github.chouaibmo:rowkalendar:0.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.0.3")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.05.00"))

    // Core Compose libraries
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose")

    // Optional (for debug UI previewing)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")



    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.amazon.ion:ion-java:1.10.5")
    implementation("com.google.accompanist:accompanist-pager:0.28.0")


}