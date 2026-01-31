// This part reads the properties file from the root directory.
import java.util.Properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.yoav_s.repository"
    compileSdk = 36

    // *** החלק הקריטי מספר 1 ***
    // השורה הזו אומרת ל-Gradle לייצר את הקובץ BuildConfig.java
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 24

        // *** החלק הקריטי מספר 2 ***
        // השורה הזו יוצרת את המשתנה GEMINI_API_KEY בתוך BuildConfig.java
        // וטוענת את הערך שלו מהקובץ local.properties
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${localProperties.getProperty("GEMINI_API_KEY")}\""
        )

        // Field for ChatGPT API Key
        buildConfigField(
            "String",
            "CHATGPT_API_KEY",
            "\"${localProperties.getProperty("CHATGPT_API_KEY")}\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(project(":MODEL"))
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