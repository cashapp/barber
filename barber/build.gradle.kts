apply(plugin = "com.vanniktech.maven.publish")

val jar by tasks.getting(Jar::class) {
  manifest {
    attributes("Automatic-Module-Name" to "app.cash.barber")
  }
}

dependencies {
  api(project(":barber-protos"))

  implementation(Dependencies.guava)
  implementation(Dependencies.kotlinStdLibJdk8)
  implementation(Dependencies.kotlinReflection)
  implementation(Dependencies.moshiCore)
  implementation(Dependencies.moshiKotlin)
  implementation(Dependencies.okio)
  implementation(Dependencies.okHttp)
  implementation(Dependencies.mustacheCompiler)
  implementation(Dependencies.wireMoshiAdapter)
  implementation(Dependencies.wireRuntime)

  testImplementation(Dependencies.assertj)
  testImplementation(Dependencies.junitApi)
  testImplementation(Dependencies.junitEngine)
  testImplementation(Dependencies.kotlinTest)
}

afterEvaluate {
  project.tasks.dokka {
    outputDirectory = "$rootDir/docs/0.x"
    outputFormat = "gfm"
  }
}

apply(from = "$rootDir/gradle-mvn-publish.gradle")
