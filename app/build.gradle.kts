import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

// Release signing credentials and ad credentials are read from local.properties (never hardcoded in VCS).
val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun resolveProp(vararg keys: String, defaultValue: String = ""): String {
    for (key in keys) {
        val gradleProp = project.findProperty(key) as? String
        if (!gradleProp.isNullOrBlank()) return gradleProp
        val localProp = localProps.getProperty(key)
        if (!localProp.isNullOrBlank()) return localProp
    }
    return defaultValue
}

fun buildConfigString(vararg keys: String, defaultValue: String = ""): String {
    val value = resolveProp(*keys, defaultValue = defaultValue)
    return "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""
}

android {
    namespace = "ir.sharif.xo"
    compileSdk = 37

    defaultConfig {
        applicationId = "ir.sharif.xo"
        minSdk = 24
        targetSdk = 37
        versionCode = 9
        versionName = "1.0.8"

        buildConfigField("String", "AD_PROVIDER", buildConfigString("AD_PROVIDER", defaultValue = "mixed"))
        buildConfigField("String", "TAPSELL_APP_ID", buildConfigString("TAPSELL_KEY", "TAPSELL_APP_ID"))
        buildConfigField("String", "ADIVERY_APP_ID", buildConfigString("ADIVERY_KEY", "ADIVERY_APP_ID"))
        buildConfigField("String", "AD_REMOTE_CONFIG_URL", buildConfigString("AD_REMOTE_CONFIG_URL"))
        buildConfigField("String", "SENTRY_DSN", buildConfigString("SENTRY_DSN_XO", "SENTRY_DSN"))

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePath = resolveProp("KEYSTORE_PATH")
            if (keystorePath.isNotEmpty()) {
                storeFile = file(keystorePath)
                storePassword = localProps.getProperty("KEYSTORE_PASSWORD", "")
                keyAlias = localProps.getProperty("KEY_ALIAS", "")
                keyPassword = localProps.getProperty("KEY_PASSWORD", "")
            }
        }
    }

    buildTypes {
        release {
            if (localProps.getProperty("KEYSTORE_PATH", "").isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug { applicationIdSuffix = ".debug" }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.core)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.sentry.android)
    implementation(libs.tapsell.plus)
    implementation(libs.adivery.sdk)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
