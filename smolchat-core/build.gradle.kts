import java.net.URL
import java.net.URI
import java.io.File

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.shubham0204.smolchat.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
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
    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }

    androidResources {
        // https://developer.android.com/reference/tools/gradle-api/8.13/com/android/build/api/dsl/AndroidResources
//        noCompress("")
//        noCompress += listOf("gguf") // this works for assets/ but not for res/raw
//        noCompress += listOf("gguf", "GGUF", ".gguf", "raw/test_model.gguf")
    }

    aaptOptions {
        // https://docs.unity3d.com/2023.2/Documentation/ScriptReference/Unity.Android.Gradle.AaptOptions.NoCompress.html

//        noCompress("gguf", "GGUF", "test_model.gguf")
    }
}

tasks.register("downloadTestModel") {
    val modelUrl =
        "https://huggingface.co/HuggingFaceTB/SmolLM2-360M-Instruct-GGUF/resolve/main/smollm2-360m-instruct-q8_0.gguf"
//    val modelUrl = "https://huggingface.co/HuggingFaceTB/SmolLM2-135M-Instruct-GGUF/resolve/main/smollm2-135m-instruct-q8_0.gguf"
    val outputDir = file("src/androidTest/res/raw/")
    val outputFile = File(outputDir, "test_model.gguf")

    outputs.file(outputFile)

    doLast {
        if (!outputDir.exists()) outputDir.mkdirs()
        if (!outputFile.exists()) {
            println("Downloading test model from $modelUrl...")
            URI(modelUrl).toURL().openStream().use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            println("Download complete: ${outputFile.absolutePath}")
        } else {
            println("Test model already exists, skipping download: ${outputFile.path} - ${outputFile.length() / 1024 / 1024} MB")
        }
    }
}

// Ensure the model is downloaded before building the instrumented test
tasks.matching {
    it.name.contains("connectedDebugAndroidTest") ||
    it.name.contains("packageDebugAndroidTest") ||
    (it.name.startsWith("merge") && it.name.endsWith("AndroidTestAssets")) ||
    (it.name.startsWith("process") && it.name.endsWith("Resources")) ||
    (it.name.startsWith("generate") && it.name.endsWith("TestResources"))

}.all {
    dependsOn("downloadTestModel")
}


dependencies {
    implementation(project(":smollm"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.mockk.android)
}
