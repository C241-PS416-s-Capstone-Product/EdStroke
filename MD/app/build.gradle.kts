import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.capstone.edstroke"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.capstone.edstroke"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            val properties = Properties()
            properties.load(localPropertiesFile.inputStream())
            val mapsApiKey = properties.getProperty("MAPS_API_KEY")
            checkNotNull(mapsApiKey) { "MAPS_API_KEY must be defined in local.properties" }

            buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
        } else {
            // Handle the case where local.properties is missing (optional)
            // You could throw an exception, warn the user, or use a default value
            // (consider using environment variables for more secure defaults)
            println("Warning: local.properties is missing. Please create it with your API keys.")
        }
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
        buildConfig = true
        mlModelBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.ml.modeldownloader.ktx)
    implementation(libs.common)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.activity.ktx)

    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.glide)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    implementation(libs.tensorflow.lite.task.vision)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)

    implementation(libs.play.services.tflite.support)
    implementation(libs.play.services.tflite.gpu)
    implementation(libs.play.services.tasks)
    implementation(libs.tensorflow.lite.task.vision.play.services)
    implementation(libs.tensorflow.lite.gpu)

//    implementation(libs.tensorflow.lite.task.vision)

    implementation(libs.tasks.vision)

//    implementation(libs.core)

    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ml.modeldownloader)

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation(libs.firebase.analytics)
    implementation(libs.circle.imageview)
    implementation(libs.simple.location)
    implementation(libs.google.direction.library)
    implementation(libs.android.youtube.player)
}