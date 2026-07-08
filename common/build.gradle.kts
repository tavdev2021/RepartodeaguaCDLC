plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.common"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    //Firebase
    api(libs.firebase.auth)
    api(libs.firebase.firestore.ktx)

    //Room
    api(libs.room.runtime)
    api(libs.room.ktx)
    ksp(libs.room.compiler)

    //Google Maps
    api(libs.play.services.location)

    // Jetpack Compose y navegación
    api("androidx.compose.animation:animation")
    api("androidx.compose.animation:animation-core:1.10.3")
    api("androidx.navigation:navigation-compose:2.9.7")
    //implementation("androidx.compose.ui:ui:1.10.0")
    //implementation("androidx.compose.material3:material3:1.3.2")

    // Lifecycle + Flow
    api("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    api(libs.androidx.lifecycle.runtime.ktx)

    //Cargar imagen en pantalla

    api("io.coil-kt:coil-compose:2.7.0")

    //Animaciones Lottie

    api("com.airbnb.android:lottie-compose:6.7.1")

    //Api SplashScreen

    api("androidx.core:core-splashscreen:1.2.0")

    //Icons

    api("androidx.compose.material:material-icons-extended:1.7.8")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}