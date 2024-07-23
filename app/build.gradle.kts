plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.easysmpc_porting"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.easysmpc_porting"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isShrinkResources = false
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "mozilla/public-suffix-list.txt"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/NOTICE.md"
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    implementation(libs.tv.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.proxy.vole)
    implementation(libs.jackson.databind)
    implementation(libs.log4j.core)
    implementation(libs.jakarta.ws.rs.api)
    implementation(libs.java.awt)
    implementation(libs.commons.validator)
    implementation(libs.httpclient5)
    implementation(libs.httpclient)
    implementation(libs.commons.cli)
    implementation(libs.jakarta.mail.api)
    implementation(libs.commons.math3)
    implementation(libs.commons.csv)
    implementation(libs.hppc)
    implementation(libs.poi)
    implementation(libs.poi.ooxml)
    implementation(libs.javax.ws.rs.api)
    implementation(libs.jakarta.activation.api)
    implementation(libs.jersey.client.v303)
    implementation(libs.jersey.hk2)
    implementation(libs.multidex)
    implementation(libs.gson)
    implementation(libs.material.v190)

}