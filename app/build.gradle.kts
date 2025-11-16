plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.sijayyx.todone"
    compileSdk = 36

    androidResources {
        generateLocaleConfig = true
    }

    defaultConfig {
        applicationId = "com.sijayyx.todone"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "v1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation("androidx.compose.material:material-icons-core:1.7.6")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.core.ktx)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.runner)
    implementation(libs.androidx.rules)

    val work_version = "2.10.5"
    implementation("androidx.work:work-runtime-ktx:$work_version")

    val room_version = "2.7.2"
    implementation("androidx.room:room-runtime:${room_version}")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:${room_version}")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")

    val appcompat_version = "1.7.1"
    implementation("androidx.appcompat:appcompat:$appcompat_version")
    // For loading and tinting drawables on older versions of the platform
    implementation("androidx.appcompat:appcompat-resources:$appcompat_version")

    androidTestImplementation(libs.androidx.junit)
    val work_test_version = "2.4.0"
    androidTestImplementation("androidx.work:work-testing:$work_test_version")
    testImplementation("com.google.truth:truth:1.4.5")
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    val androidXTestVersion = "1.7.0"
}