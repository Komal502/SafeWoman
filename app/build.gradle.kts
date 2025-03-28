import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.safewoman"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.safewoman"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ Securely load API key from local.properties
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { properties.load(it) }
        }
        val mapsApiKey = properties.getProperty("MAPS_API_KEY", "")

        // ✅ Add API key to BuildConfig
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }

    kotlinOptions {
        jvmTarget = "19"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true  // ✅ Enable BuildConfig
    }
}

dependencies {
    // ✅ AndroidX libraries
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)

    // ✅ Google services and location
    implementation(libs.play.services.location.v1800)

    // ✅ Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database.v2100)
    implementation(libs.androidx.activity.v160)
    implementation(libs.androidx.constraintlayout.v214)

    // ✅ Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ✅ UI libraries
    implementation(libs.ssp.android)
    implementation(libs.sdp.android)

    // ✅ Image slider (ViewPager2)
    implementation(libs.androidx.viewpager2)

    // ✅ Bottom navigation
    implementation(libs.material.v190)

    // ✅ News API
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.glide)

    // ✅ Google Play services
    implementation(libs.places) // 4.2.0
    implementation(libs.androidx.recyclerview) // 1.4.0

    implementation(libs.volley)

    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

}
