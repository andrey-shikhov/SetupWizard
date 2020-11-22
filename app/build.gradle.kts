plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId("me.shikhov.setupwizardapp")
        minSdkVersion(15)
        targetSdkVersion(29)
        versionCode(1)
        versionName("1.0")
        testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")
    }
}

dependencies {
    implementation(project(":setupwizard"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.10")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.core:core-ktx:1.3.1")
    implementation("androidx.fragment:fragment-ktx:1.2.5")
    implementation("me.shikhov:wlog:2.3.1")

    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}
