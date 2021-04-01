import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://plugins.gradle.org/m2/")
  }

  dependencies {
    classpath(Dependencies.kotlinGradlePlugin)
    classpath(Dependencies.spotlessPlugin)
    classpath(Dependencies.mavenPublishGradlePlugin)
    classpath(Dependencies.wireGradlePlugin)
  }
}

allprojects {
  group = property("GROUP") as String
  version = property("VERSION_NAME") as String
}

subprojects {
  apply(plugin = "java")
  apply(plugin = "kotlin")
  apply(plugin = "org.jetbrains.dokka")
  // TODO bring back once Kotlin 1.4 trailing commas are supported
  //  apply(plugin = "com.diffplug.gradle.spotless")
  apply(plugin = "com.squareup.wire")

  val compileKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    // dependsOn("spotlessKotlinApply")
  }

  val compileTestKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
  }

  tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
  }

  repositories {
    mavenCentral()
    jcenter()
  }

  // TODO bring back once Kotlin 1.4 trailing commas are supported
  //  spotless {
  //    kotlin {
  //      target "**/*.kt"
  //      ktlint(dep.ktlintVersion).userData(listOf("indent_size": "2", "continuation_indent_size" : "4"))
  //    }
  //  }

  tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
      events("started", "passed", "skipped", "failed")
      exceptionFormat = TestExceptionFormat.FULL
      showExceptions = true
      showStackTraces = true
    }
  }

  // We have to set the dokka configuration after evaluation since the com.vanniktech.maven.publish
  // plugin overwrites our dokka configuration on projects where it's applied.
  afterEvaluate {
    val dokka by tasks.getting(DokkaTask::class) {
      reportUndocumented = false
      skipDeprecated = true
      jdkVersion = 8
    }
  }

  if (file("$rootDir/hooks.gradle").exists()) {
    apply(from = file("$rootDir/hooks.gradle"))
  }
}
