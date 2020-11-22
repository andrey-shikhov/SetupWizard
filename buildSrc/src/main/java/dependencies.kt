@file:Suppress("ClassName", "SpellCheckingInspection")

import org.gradle.api.JavaVersion
import java.io.FileInputStream
import java.nio.file.Paths
import java.time.LocalDate
import java.time.temporal.ChronoField
import java.text.SimpleDateFormat
import java.util.*

object deps {
    object versions {
        const val kotlin = "1.4.10"
        val java = JavaVersion.VERSION_1_8
    }

    object kotlin {
        val stdLib     = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${versions.kotlin}"
        val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.1"
        val testCommon = "org.jetbrains.kotlin:kotlin-test"
        val testJUnit  = "org.jetbrains.kotlin:kotlin-test-junit"
    }
}