plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 30
    defaultConfig {
        minSdk = 15
        targetSdk = 30
        versionCode = 1
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(project(":setupwizard"))
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.fragment:fragment-ktx:1.3.6")
    implementation("me.shikhov:wlog:3.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
