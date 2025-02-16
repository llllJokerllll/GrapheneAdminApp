plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.grapheneadminapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.grapheneadminapp"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Habilita la ofuscación y optimización del código
            isShrinkResources = true // Elimina recursos no utilizados
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isDebuggable = false // Previene debugging en modo debug
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
        viewBinding = true // Habilita View Binding
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Dependencias de seguridad y cifrado
    implementation("org.bouncycastle:bcprov-jdk15on:1.70") // Cifrado avanzado PBKDF2 y AES
    implementation("androidx.security:security-crypto:1.1.0-alpha06") // Seguridad adicional
    implementation("androidx.enterprise:enterprise-feedback:1.1.0") // Device Policy Manager

    // Dependencias para envío de correos seguros
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")

    // Dependencias para la ubicacion
    implementation("com.google.android.gms:play-services-location:21.1.0")

    // Dependencias para el VPN
    implementation("androidx.core:core-ktx:1.15.0")

    // Dependencias para el Device Admin
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    // Dependencias para el manejo de procesos
    implementation("androidx.lifecycle:lifecycle-process:2.7.0")


    // Protección contra debugging y manipulación
    android.buildTypes.all {
        buildConfigField("boolean", "DEBUGGABLE", "true")
    }
}
