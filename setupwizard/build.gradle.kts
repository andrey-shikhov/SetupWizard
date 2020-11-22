import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.library")
    kotlin("android")
    `maven-publish`
    id("com.jfrog.bintray")
}

val bintrayRepo = "SetupWizard"
val bintrayName = "me.shikhov.setupwizard"

val publishedGroupId = "me.shikhov.setupwizard"
val libraryName = "setupwizard"
val artifact = "setupwizard"

val libraryDescription = "A multi step setup flow creator android library for kotlin android projects"

val siteUrl = "https://github.com/andrey-shikhov/SetupWizard"
val gitUrl = "https://github.com/andrey-shikhov/SetupWizard.git"

val libraryVersion = "0.10.0"

val developerId = "andrey-shikhov"
val developerName = "Andrew Shikhov"
val developerEmail = "andrew@shikhov.me"

val licenseName = "The Apache Software License, Version 2.0"
val licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt"
val allLicenses = arrayOf("Apache-2.0")

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(15)
        targetSdkVersion(29)
        versionCode(9)
        versionName(libraryVersion)

        testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            minifyEnabled(false)
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(deps.kotlin.stdLib)
    implementation(deps.kotlin.coroutines)
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")

    testImplementation("junit:junit:4.13.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}


// Bintray

val bintrayUser: String = gradleLocalProperties(rootDir).getProperty("bintray_user")
val bintrayApikey: String = gradleLocalProperties(rootDir).getProperty("bintray_apikey")
val bintrayGpgPassword: String = gradleLocalProperties(rootDir).getProperty("bintray_gpg_password")

bintray {
    user = bintrayUser
    key = bintrayApikey

    pkg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
        repo = bintrayRepo
        name = bintrayName
        desc = libraryDescription
        websiteUrl = siteUrl
        vcsUrl = gitUrl
        setLicenses(*allLicenses)
        setLabels("kotlin", "android")
        publish = true
        publicDownloadNumbers = true
        desc = libraryDescription
        version(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.VersionConfig>{
            gpg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.GpgConfig> {
                sign = true //Determines whether to GPG sign the files. The default is false
                passphrase = bintrayGpgPassword
            })
        })
    })
}