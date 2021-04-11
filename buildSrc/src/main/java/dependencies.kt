@file:Suppress("ClassName", "SpellCheckingInspection")

import org.gradle.api.JavaVersion

object deps {
    object versions {
        const val kotlin = "1.4.31"
        val java = JavaVersion.VERSION_1_8
    }

    object kotlin {
        val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.3"
        val testCommon = "org.jetbrains.kotlin:kotlin-test"
        val testJUnit  = "org.jetbrains.kotlin:kotlin-test-junit"
    }
}