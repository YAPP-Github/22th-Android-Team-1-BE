import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "2.7.11"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    id("org.sonarqube") version "3.5.0.2730"
    id("jacoco")
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    kotlin("plugin.jpa") version "1.6.21"
}

java.sourceCompatibility = JavaVersion.VERSION_17


allprojects {
    group = "com.yapp"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "jacoco")
    apply(plugin = "org.sonarqube")

    apply(plugin = "kotlin")
    apply(plugin = "kotlin-spring") //all-open
    apply(plugin = "kotlin-jpa")

    dependencies {
        // springboot
        implementation("org.springframework.boot:spring-boot-starter-batch")
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-data-redis")
//        implementation("org.springframework.boot:spring-boot-starter-security")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        developmentOnly("org.springframework.boot:spring-boot-devtools")

        // open feign
//        implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

        // kotlin
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

        //lombok
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        // DB
        runtimeOnly("org.mariadb.jdbc:mariadb-java-client")

        // test
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.batch:spring-batch-test")
        testImplementation("org.springframework.security:spring-security-test")
    }

    dependencyManagement {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }

        dependencies {
            dependency("net.logstash.logback:logstash-logback-encoder:6.6")
        }
    }

    sonarqube {
        properties {
            // 각 프로젝트마다 적용해야하는부분.
            property("sonar.java.binaries", "${buildDir}/classes")
            property("sonar.coverage.jacoco.xmlReportPaths", "${buildDir}/reports/jacoco.xml")
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    val testCoverage by tasks.registering {
        group = "verification"
        description = "Runs the unit tests with coverage"

        dependsOn(":test",
                ":jacocoTestReport",
                ":jacocoTestCoverageVerification")

        tasks["jacocoTestReport"].mustRunAfter(tasks["test"])
        tasks["jacocoTestCoverageVerification"].mustRunAfter(tasks["jacocoTestReport"])
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy("jacocoTestReport")
    }

    // jacoco ci
    tasks.jacocoTestReport {
        dependsOn("test")
        reports {
            html.isEnabled = true
            csv.isEnabled = true
            xml.isEnabled = true
            xml.destination = file("${buildDir}/reports/jacoco.xml")
        }
        // exclude q-object
        val qDomains = mutableListOf<String>()
        for (qPattern in listOf("**/QA".."**/QZ")) {
            qDomains.add("$qPattern*")
        }
        finalizedBy("jacocoTestCoverageVerification")
    }

    tasks.jacocoTestCoverageVerification {
        val qDomains = mutableListOf<String>()
        for (qPattern in listOf("**/QA".."**/QZ")) {
            qDomains.add("$qPattern*")
        }

        violationRules {
            rule {
                element = "CLASS"

                limit {
                    counter = "BRANCH"
                    value = "COVEREDRATIO"
                    minimum = "0.10".toBigDecimal()
                }
                excludes = listOf(
                    "**/*Application*",
                    "**/*Config*",
                    "**/*Dto*",
                    "**/*Request*",
                    "**/*Response*",
                    "**/*Interceptor*",
                    "**/*Exception*",
                    *qDomains.toTypedArray()
                )
            }
        }
    }

    configure<JacocoPluginExtension> {
        toolVersion = "0.8.8"
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "YAPP-Github_22nd-Android-Team-1-BE") // 본인 꺼 집어넣으세용
        property("sonar.organization", "yapp-github") // 이것두
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sources", "src")
        property("sonar.language", "java")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.test.inclusions", "**/*Test.java")
        // 테스트 커버리지에서 빼고싶은거 넣어야함
        property("sonar.exclusions", "**/test/**, **/Q*.java, **/*Doc*.java, **/resources/** ,**/*Application*.java , **/*Config*.java," +
        "**/*Dto*.java, **/*Request*.java, **/*Response*.java ,**/*Exception*.java ,**/*ErrorCode*.java")
        property("sonar.java.coveragePlugin", "jacoco")
    }
}

// module core 에 module api, consumer이 의존
project(":cvs-api") {
    dependencies {
        implementation(project(":cvs-domain"))
    }
}

project(":cvs-batch") {
    dependencies {
        implementation(project(":cvs-domain"))
    }
}

// core 설정
project(":cvs-domain") {
    val jar: Jar by tasks
    val bootJar: BootJar by tasks

    bootJar.enabled = false
    jar.enabled = true

}