plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.yoav_s.viewmodel"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation (libs.play.services.tasks)
    implementation(libs.firebase.firestore)
    implementation(project(":MODEL"))
    implementation(project(":REPOSITORY"))
    implementation(project(":HELPER"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.guava)
    //implementation("com.google.guava:guava:33.5.0-android")

    // The Gemini SDK itself
    //implementation("com.google.ai.client.generativeai:generativeai:0.9.0") // Or your version catalog alias
    implementation(libs.generativeai) // Or your version catalog alias

    // Its dependency for Java Futures compatibility
    //implementation("com.google.guava:guava:32.1.3-android") // Or your version catalog
    implementation(libs.guava.v3213android) // Or your version catalog
}