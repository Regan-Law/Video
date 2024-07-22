plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.jetbrains.kotlin.android)
}

android {
	namespace = "pers.reganlaw.video"
	compileSdk = 34

	defaultConfig {
		applicationId = "pers.reganlaw.video"
		minSdk = 29
		targetSdk = 34
		versionCode = 1
		versionName = "1.0"
		ndk {
			abiFilters.addAll(mutableSetOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
		}
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
//			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}
	kotlinOptions {
		jvmTarget = "1.8"
	}
	buildFeatures {
		viewBinding = true
	}
	dataBinding {
		enable = true
	}
}

dependencies {

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.material)
	implementation(libs.androidx.activity)
	implementation(libs.androidx.constraintlayout)
	implementation(libs.androidx.camera.camera2)
	implementation(libs.androidx.camera.core)
	implementation(libs.androidx.camera.extensions)
	implementation(libs.androidx.camera.lifecycle)
	implementation(libs.androidx.camera.video)
	implementation(libs.androidx.camera.view)
	implementation(libs.xxpermissions)
	implementation(libs.lightcompressor)
	implementation(libs.kotlinx.coroutines.android)
	implementation(libs.kotlinx.coroutines.core)
	implementation(libs.videoprocessor)
	implementation(libs.rxffmpeg)
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}