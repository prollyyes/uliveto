import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "it.uliveto.browser"
    compileSdk = 36

    defaultConfig {
        applicationId = "it.uliveto.browser"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Split APKs by ABI to reduce download size
        splits {
            abi {
                isEnable = true
                reset()
                include("arm64-v8a", "armeabi-v7a", "x86_64")
                isUniversalApk = false
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            // GeckoView .so files are not 16 KB page-aligned; extract to disk at install time
            useLegacyPackaging = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// iCloud Drive running on ~/Documents creates " 2" conflict copies inside the build
// directory, which break the aapt2 file-name validation. Remove them before every build.
tasks.named("preBuild") {
    doFirst {
        val buildDir = layout.buildDirectory.get().asFile
        if (buildDir.exists()) {
            fileTree(buildDir)
                .matching { include("**/* 2", "**/* 2.*") }
                .forEach { it.deleteRecursively() }
        }
    }
}

// Exclude service-glean from all AC transitive dependencies
configurations.all {
    exclude(group = "org.mozilla.components", module = "service-glean")
    exclude(group = "org.mozilla.telemetry", module = "glean-native")
    exclude(group = "org.mozilla.telemetry", module = "glean")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)

    // Android Components (AC)
    // Note: geckoview-omni is brought in transitively via browser-engine-gecko.
    implementation(libs.ac.concept.engine)
    implementation(libs.ac.browser.engine.gecko)
    implementation(libs.ac.browser.state)
    implementation(libs.ac.browser.session.storage)
    implementation(libs.ac.feature.session)
    implementation(libs.ac.feature.tabs)
    implementation(libs.ac.feature.search)
    implementation(libs.ac.feature.findinpage)
    implementation(libs.ac.feature.readerview)
    implementation(libs.ac.feature.downloads)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.swiperefreshlayout)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.ui.graphics)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
