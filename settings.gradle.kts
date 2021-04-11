pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
}

plugins {
    `gradle-enterprise`
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}


rootProject.name="SW"
include(":setupwizard", ":app")
