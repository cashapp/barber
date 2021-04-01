apply(plugin = "com.vanniktech.maven.publish")

val jar by tasks.getting(Jar::class) {
  manifest {
    attributes("Automatic-Module-Name" to "app.cash.barber")
  }
}

dependencies {
  implementation(Dependencies.guava)
  implementation(Dependencies.kotlinStdLib)
  implementation(Dependencies.kotlinReflection)
  implementation(Dependencies.moshiCore)
  implementation(Dependencies.moshiKotlin)
  implementation(Dependencies.okio)
  implementation(Dependencies.mustacheCompiler)
  implementation(Dependencies.wireMoshiAdapter)
  implementation(Dependencies.wireRuntime)

  testImplementation(Dependencies.assertj)
  testImplementation(Dependencies.junitApi)
  testImplementation(Dependencies.junitEngine)
  testImplementation(Dependencies.kotlinTest)
}

wire {
  protoLibrary = true
  kotlin {
    javaInterop = true
  }
}

afterEvaluate {
  project.tasks.dokka {
    outputDirectory = "$rootDir/docs/0.x"
    outputFormat = "gfm"
  }
}

apply(from = "$rootDir/gradle-mvn-publish.gradle")
