import java.time.LocalDateTime
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.detekt)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
}

fun readProperties(fileName: String): Properties {
    val propsFile = project.rootProject.file(fileName)
    if (!propsFile.exists()) {
        throw GradleException("$fileName doesn't exist")
    }
    if (!propsFile.canRead()) {
        throw GradleException("Cannot read $fileName")
    }
    return Properties().apply {
        propsFile.inputStream().use { load(it) }
    }
}

inline fun updateProperties(fileName: String, block: Properties.() -> Unit) {
    val propsFile = project.rootProject.file(fileName)
    val props = readProperties(propsFile.name)
    block(props)
    propsFile.outputStream().use {
        val date = LocalDateTime.now()
        props.store(it, "Updated at $date")
    }
}

open class PlatformVersion(
    open val versionName: String
)

data class PlatformVersionWithCode(
    override val versionName: String,
    val versionCode: Int
) : PlatformVersion(versionName)

typealias AndroidVersion = PlatformVersionWithCode
typealias IOSVersion = PlatformVersion

sealed class Platform<VersionType : PlatformVersion> {
    object Android : Platform<PlatformVersionWithCode>()
    object IOS : Platform<IOSVersion>()
}

fun <VersionType : PlatformVersion> getVersionForPlatform(platform: Platform<VersionType>?): VersionType {
    val versionProperties = readProperties("version.properties")

    fun getAndReplaceVersion(key: String): String {
        val versionName = versionProperties.getProperty("VERSION_NAME")
        return versionProperties.getProperty(key).replace("\$VERSION_NAME", versionName)
    }

    @Suppress("UNCHECKED_CAST")
    return when (platform) {
        Platform.Android -> PlatformVersionWithCode(
            getAndReplaceVersion("VERSION_ANDROID"),
            versionProperties.getProperty("VERSION_ANDROID_CODE").toInt()
        ) as VersionType

        Platform.IOS -> IOSVersion(getAndReplaceVersion("VERSION_NAME")) as VersionType

        else -> PlatformVersion(versionProperties.getProperty("VERSION_NAME")) as VersionType
    }
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    targets.all {
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
    
    sourceSets {
        all {
            languageSettings {
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }
        commonMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.runtime)
            implementation(compose.ui)

            // Compose - Voyager
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)

            // Multiplatform Settings
            implementation(libs.multiplatformSettings.base)
            implementation(libs.multiplatformSettings.coroutines)
            implementation(libs.multiplatformSettings.serialization)

            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.json)

            // Logging library
            implementation(libs.napier)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)

            // Multiplatform Settings
            implementation(libs.datastore)
            implementation(libs.multiplatformSettings.datastore)

            // Accompanist
            implementation(libs.accompanist.permissions)

            // Nearby Connections
            implementation(libs.playServices.nearby)

            implementation(libs.compose.ui.tooling.preview)
        }
    }
}

android {
    namespace = "com.arnyminerz.pocketchips"
    compileSdk = 34

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.arnyminerz.pocketchips"
        minSdk = 24
        targetSdk = 34

        val versionProps = readProperties("version.properties")

        versionCode = versionProps.getProperty("VERSION_ANDROID_CODE").toInt()
        versionName = versionProps.getProperty("VERSION_ANDROID")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

fun increaseNumberInProperties(key: String) {
    var code = 0
    updateProperties("version.properties") {
        code = getProperty(key).toInt() + 1
        setProperty(key, code.toString())
    }

    println("Increased $key to $code")
}

val increaseVersionCode = task("increaseVersionCode") {
    doFirst {
        increaseNumberInProperties("VERSION_ANDROID_CODE")
    }
}

tasks.findByName("bundleRelease")?.dependsOn?.add(increaseVersionCode)
