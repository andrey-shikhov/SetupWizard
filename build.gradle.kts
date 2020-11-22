import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.0-alpha16")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.10")

        classpath("org.jetbrains.dokka:dokka-android-gradle-plugin:0.9.18")
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
    }
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}

allprojects {
    repositories {
        google()
        jcenter()
        gradlePluginPortal()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = deps.versions.java.toString()
            freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
        }
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = deps.versions.java.toString()
    targetCompatibility = deps.versions.java.toString()

    // Show all warnings except boot classpath
    options.apply {
        compilerArgs.apply {
            add("-Xlint:all")                // Turn on all warnings
            add("-Xlint:-deprecation")       // Allow deprecations from Dagger 2
            add("-Xlint:-classfile")         // Ignore Java 8 method param meta data
            add("-Xlint:-unchecked")         // Dagger 2 unchecked issues
            add("-Werror")                   // Turn warnings into errors
        }
        encoding = "utf-8"
        isFork = true
    }
}


tasks.all {
    when (this) {
        is JavaForkOptions -> {
            // should improve memory on a 64bit JVM
            jvmArgs("-XX:+UseCompressedOops")
            // should avoid GradleWorkerMain to steal focus
            jvmArgs("-Djava.awt.headless=true")
            jvmArgs("-Dapple.awt.UIElement=true")
        }
    }
}