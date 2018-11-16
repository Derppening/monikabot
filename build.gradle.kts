/*
 *  This file is part of MonikaBot.
 *
 *  Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 *  MonikaBot is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MonikaBot is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MonikaBot.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import java.io.FileWriter
import java.util.Properties
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    java
    kotlin("jvm") version "1.3.10"
}

group = "com.derppening"
version = "1.2.0-beta.13"

application {
    mainClassName = "com.derppening.monikabot.Main"
    applicationDefaultJvmArgs = listOf("-XX:+UseG1GC", "-Xms128M", "-Xmx1G")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    val kotlin = "1.3.10"
    val kotlinCoroutines = "1.0.1"
    val discord4j = "2.10.1"
    val logback = "1.2.3"
    val jacksonCore = "2.9.7"
    val jacksonDatabind = "2.9.7"
    val jacksonAnnotations = "2.9.7"
    val jacksonModuleKotlin = "2.9.7"
    val jacksonDatatypeJSR310 = "2.9.7"
    val jsoup = "1.11.3"
    val junit = "5.3.1"
    val junitPlatform = "1.3.1"
    val apacheCommonsText = "1.6"

    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin")
    compile("org.jetbrains.kotlin:kotlin-reflect:$kotlin")
    compile("com.discord4j:Discord4J:$discord4j")
    compile("ch.qos.logback:logback-classic:$logback")
    compile("com.fasterxml.jackson.core:jackson-core:$jacksonCore")
    compile("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabind")
    compile("com.fasterxml.jackson.core:jackson-annotations:$jacksonAnnotations")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonModuleKotlin")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonDatatypeJSR310")
    compile("org.jsoup:jsoup:$jsoup")
    compile("org.apache.commons:commons-text:$apacheCommonsText")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutines")

    testCompile("org.jetbrains.kotlin:kotlin-test-junit5:$kotlin")
    testCompile("org.junit.jupiter:junit-jupiter-api:$junit")
    testCompile("org.junit.jupiter:junit-jupiter-params:$junit")
    testCompile("org.junit.platform:junit-platform-runner:$junitPlatform")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junit")
    testRuntime("org.junit.platform:junit-platform-console:$junitPlatform")
}

fun getWorkingBranch(): String {
    try {
        val workingDir = File("${project.projectDir}")
        val result = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD", null, workingDir)
        result.waitFor()
        if (result.exitValue() == 0) {
            return result.inputStream.bufferedReader().readText().trim()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return "unknown"
}

task("createGradleProperties") {
    dependsOn("processResources")
    doLast {
        FileWriter(File("$buildDir/resources/main/properties/version.properties")).use {
            val p = Properties()
            p["version"] = project.version.toString()
            p["gitbranch"] = getWorkingBranch()

            p.store(it, null)
        }
    }
}

tasks {
    getByName<KotlinCompile>("compileKotlin") {
        kotlinOptions.jvmTarget = "1.8"
    }

    getByName<KotlinCompile>("compileTestKotlin") {
        kotlinOptions.jvmTarget = "1.8"
    }

    getByName<DefaultTask>("classes") {
        dependsOn("createGradleProperties")
    }

    getByName<Test>("test") {
        useJUnitPlatform()
    }

    getByName<Wrapper>("wrapper") {
        gradleVersion = "4.10.2"
        distributionType = Wrapper.DistributionType.ALL
    }
}
