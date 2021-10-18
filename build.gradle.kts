buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.0-alpha13")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.5.30")
    }
}

val clean by tasks.creating(Delete::class) {
    delete(rootProject.buildDir)
}