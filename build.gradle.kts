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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileWriter
import java.time.Instant
import java.util.*

plugins {
    application
    java
    kotlin("jvm") version "1.3.41"
}

group = "com.derppening"
version = "1.2.0"

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
    val kotlin = "1.3.41"
    val kotlinCoroutines = "1.2.2"
    val discord4j = "2.10.1"
    val logback = "1.2.3"
    val jacksonJson = "2.9.9"
    val jsoup = "1.12.1"
    val junit = "5.4.2"
    val junitPlatform = "1.4.2"
    val apacheCommonsText = "1.6"

    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin")
    compile("org.jetbrains.kotlin:kotlin-reflect:$kotlin")
    compile("com.discord4j:Discord4J:$discord4j")
    compile("ch.qos.logback:logback-classic:$logback")
    compile("com.fasterxml.jackson.core:jackson-core:$jacksonJson")
    compile("com.fasterxml.jackson.core:jackson-databind:$jacksonJson")
    compile("com.fasterxml.jackson.core:jackson-annotations:$jacksonJson")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonJson")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonJson")
    compile("org.jsoup:jsoup:$jsoup")
    compile("org.apache.commons:commons-text:$apacheCommonsText")
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutines")

    testCompile("org.jetbrains.kotlin:kotlin-test-junit5:$kotlin")
    testCompile("org.junit.jupiter:junit-jupiter:$junit")
    testCompile("org.junit.platform:junit-platform-runner:$junitPlatform")
    testRuntime("org.junit.platform:junit-platform-console:$junitPlatform")
}

data class GitInfo(val branch: String, val commitTime: String)

fun getGitInfo(): GitInfo {
    val projectDir = project.projectDir

    return try {
        val branchProcess = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD", null, projectDir)
        val timeProcess = Runtime.getRuntime().exec("git log -1 --format=%at", null, projectDir)

        branchProcess.waitFor()
        timeProcess.waitFor()

        val branch = branchProcess.takeIf { it.exitValue() == 0 }?.inputStream?.reader()?.readText()?.trim() ?: "unknown"
        val time = timeProcess.takeIf { it.exitValue() == 0 }?.inputStream?.reader()?.readText()?.trim() ?: "unknown"

        GitInfo(branch, time)
    } catch (e: Throwable) {
        GitInfo("unknown", "unknown")
    }
}

task("createGradleProperties") {
    dependsOn("processResources")
    doLast {
        FileWriter(File("$buildDir/resources/main/properties/version.properties")).use {
            val gitInfo = getGitInfo()

            val p = Properties()
            p["version"] = project.version.toString()
            p["gitbranch"] = gitInfo.branch
            p["buildtime"] = Instant.now().epochSecond.toString()
            p["committime"] = gitInfo.commitTime

            p.store(it, null)
        }
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    getByName<DefaultTask>("classes") {
        dependsOn("createGradleProperties")
    }

    getByName<Test>("test") {
        useJUnitPlatform()
    }

    getByName<Wrapper>("wrapper") {
        gradleVersion = "5.5"
        distributionType = Wrapper.DistributionType.ALL
    }
}
