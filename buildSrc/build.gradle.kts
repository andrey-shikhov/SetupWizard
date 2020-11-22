repositories {
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.10")
//    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.10")
}