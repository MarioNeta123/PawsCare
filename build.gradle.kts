// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
    id("org.sonarqube") version "7.5.0.8588"
}

configure<org.sonarqube.gradle.SonarExtension> {
    properties {
        property("sonar.projectName", "PawsCare2")
        property("sonar.projectKey", "PawsCare2")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.sourceEncoding", "UTF-8")
    }
}
