plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "ru.sitronics.velobike"
    compileSdk = 34

    defaultConfig {
        applicationId = "ru.sitronics.velobike"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "3.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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

    buildFeatures {
        buildConfig = true
        compose = true
        dataBinding = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("development") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://dev-velobike.sitronics-it.com\"")
            buildConfigField("String", "BASE_URL_OLD", "\"https://apivelobike.legacy.sitronics-it.com\"")
            buildConfigField("String", "QRATOR_SECRET", "\"\"")
            // TODO: FileProvider for TakePhoto may be won't work with applicationIdSuffix
//            applicationIdSuffix = ".dev"
        }
        create("staging") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://qrator-test-velobike.sitronics-it.com\"")
            buildConfigField("String", "BASE_URL_OLD", "\"https://apivelobike.legacy-test.sitronics-it.com\"")
            buildConfigField("String", "QRATOR_SECRET", "\"qqsFJjHdpFcQ\"")
//            applicationIdSuffix = ".test"
        }
        create("production") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://iot.velobike.ru\"")
            buildConfigField("String", "BASE_URL_OLD", "\"https://apivelobike.velobike.ru\"")
            buildConfigField("String", "QRATOR_SECRET", "\"qqsFJjHdpFcQ\"")
        }
    }

/*  // for CI
    // run in terminal: .\gradlew phoneproductionDebugAndroidTest
    testOptions {
        managedDevices {
            localDevices {
                create("phone") {
                    // Use device profiles you typically see in Android Studio.
                    device = "Pixel 2"
                    // Use only API levels 27 and higher.
                    apiLevel = 35
                    // To include Google services, use "google".
                    systemImageSource = "google"
                }
            }
        }
    }
*/
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")

    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-viewbinding")
    implementation("androidx.compose.material3:material3")

    // TODO: not update 2.7.0 while androidx.compose.ui:ui 1.7.0 will release
    val lifecycleVersion = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    // Yandex Maps
    implementation ("com.yandex.android:maps.mobile:4.4.0-lite")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-android-compiler:2.50")

    // retrofit
    val retrofitVersion = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-scalars:$retrofitVersion")

    // pagination
    val paging_version = "3.3.0"
    implementation("androidx.paging:paging-runtime-ktx:$paging_version")
    implementation("androidx.paging:paging-compose:$paging_version")

    // log
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // biometric auth
    implementation("androidx.biometric:biometric:1.1.0")
    // Secure Device Storage
    implementation("de.adorsys.android:securestoragelibrary:1.2.4")

    // QR code
    implementation("com.github.yuriy-budiyev:code-scanner:2.3.2")

    // Coil
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")

    // Chat
    releaseImplementation("im.threads:threads-release:4.33.1")
    debugImplementation("im.threads:threads-debug:4.33.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test.ext:junit-ktx:1.2.1")
    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6")
    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("org.mockito:mockito-android:5.12.0")

    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
