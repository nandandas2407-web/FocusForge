plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.facebook.react.rootproject")
}

react {
    // Codegen config
    root = file("../../")
    reactNativeDir = file("../../node_modules/react-native/")
    codegenDir = file("../../node_modules/@react-native/codegen/")
    cliDir = file("../../node_modules/react-native/cli/")
    bundleCommand = "export:embed"
}

android {
    namespace "com.focusforge.native"
    compileSdk 35

    defaultConfig {
        applicationId "com.focusforge.app"
        minSdkVersion 24
        targetSdkVersion 35
        versionCode 200
        versionName "2.0.0"
    }

    signingConfigs {
        debug {
            storeFile file('debug.keystore')
            storePassword 'android'
            keyAlias 'androiddebugkey'
            keyPassword 'android'
        }
    }

    buildTypes {
        debug {
            signingConfig signingConfigs.debug
        }
        release {
            signingConfig signingConfigs.debug // Use release keystore for production
            minifyEnabled true
            proguardFiles getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("com.facebook.react:react-android")

    if (hermesEnabled.toBoolean()) {
        implementation("com.facebook.react:hermes-android")
    } else {
        implementation jscFlavor
    }

    implementation "androidx.core:core-ktx:1.12.0"
    implementation "androidx.appcompat:appcompat:1.6.1"
}

apply from: file("../../node_modules/@react-native-community/cli-platform-android/native_modules.gradle")
applyNativeModulesAppBuildGradle(project)
