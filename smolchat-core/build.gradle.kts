import java.net.URI

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
}

tasks.register("downloadTestModel") {
    val modelUrl =
        "https://huggingface.co/HuggingFaceTB/SmolLM2-360M-Instruct-GGUF/resolve/main/smollm2-360m-instruct-q8_0.gguf"
    val outputDir = file("src/androidTest/assets/")
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
    implementation("com.google.code.gson:gson:2.11.0")

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.mockk.android)
}
