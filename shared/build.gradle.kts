import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
}

// Skiko 0.8.18 is published with a truncated iOS simulator klib (the
// repository metadata advertises 42 MB, but the artifact is only 19 MB).
// Keep the Compose dependency graph otherwise unchanged while resolving the
// first complete compatible Skiko release.
configurations.configureEach {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.jetbrains.skiko") {
      useVersion("0.9.22.2")
      because("Skiko 0.8.18 iOS klib is truncated upstream")
    }
  }
}

// Use one explicit schema export path on native and AGP 9 Android-KMP targets.
// Room 2.7's Gradle adapter does not configure the Android-KMP KSP task.
ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
}

compose.resources {
  publicResClass = true
}

kotlin {
  androidLibrary {
    namespace = "com.example.shared"
    compileSdk = 36
    minSdk = 24
  }

  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "shared"
      isStatic = false
      binaryOption("bundleId", "com.example.shared")
    }
  }

  sourceSets {
    commonTest.dependencies {
      implementation(kotlin("test"))
    }

    commonMain.dependencies {
      // Compose Multiplatform
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.materialIconsExtended)

      // Room KMP (2.7+ has official multiplatform support)
      implementation(libs.androidx.room.runtime)
      implementation(libs.sqlite.bundled)

      // Coroutines
      implementation(libs.kotlinx.coroutines.core)

      // Ktor (KMP HTTP client — replaces Retrofit in shared code)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.content.negotiation)
      implementation(libs.ktor.serialization.kotlinx.json)

      // KMP-safe date/time (replaces java.util.Calendar)
      implementation(libs.kotlinx.datetime)

      // Lifecycle / ViewModel (KMP)
      implementation(libs.androidx.lifecycle.viewmodel.compose)
      implementation(libs.androidx.lifecycle.runtime.compose)
    }

    androidMain.dependencies {
      implementation(libs.kotlinx.coroutines.android)
      implementation(libs.androidx.activity.compose)

      // Ktor Android engine
      implementation(libs.ktor.client.okhttp)
    }

    iosMain.dependencies {
      // Ktor iOS engine (Darwin = URLSession-based)
      implementation(libs.ktor.client.darwin)
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest>().configureEach {
  providers.environmentVariable("TAKAKOI_TEST_SIMULATOR_ID").orNull?.let { device.set(it) }
}

dependencies {
  add("kspCommonMainMetadata", libs.androidx.room.compiler)
  add("kspIosSimulatorArm64", libs.androidx.room.compiler)
  add("kspIosX64", libs.androidx.room.compiler)
  add("kspIosArm64", libs.androidx.room.compiler)
  add("kspAndroid", libs.androidx.room.compiler)
}
