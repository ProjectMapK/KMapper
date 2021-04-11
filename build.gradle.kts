plugins {
    id("maven")
    id("java")
    kotlin("jvm") version "1.4.32"
    // その他補助系
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    id("jacoco")
    id("com.github.ben-manes.versions") version "0.28.0"
}

group = "com.mapk"
version = "0.36"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    implementation(kotlin("reflect"))
    api("com.github.ProjectMapK:Shared:0.20")

    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter", version = "5.7.1") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    // 現状プロパティ名の変換はテストでしか使っていないのでtestImplementation
    // https://mvnrepository.com/artifact/com.google.guava/guava
    testImplementation(group = "com.google.guava", name = "guava", version = "29.0-jre")
}

tasks {
    compileKotlin {
        dependsOn("ktlintFormat")
        kotlinOptions {
            jvmTarget = "1.8"
            allWarningsAsErrors = true
        }
    }

    compileTestKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }
    test {
        useJUnitPlatform()
        // テスト終了時にjacocoのレポートを生成する
        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        reports {
            xml.isEnabled = true
            csv.isEnabled = false
            html.isEnabled = true
        }
    }
}
