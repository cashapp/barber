plugins {
  id("com.squareup.wire")
}
apply(plugin = "com.vanniktech.maven.publish")

wire {
  protoLibrary = true
  kotlin {
    javaInterop = true
  }
}

val jar by tasks.getting(Jar::class) {
  manifest {
    attributes("Automatic-Module-Name" to "app.cash.barber")
  }
}

afterEvaluate {
  project.tasks.dokka {
    outputDirectory = "$rootDir/docs/0.x"
    outputFormat = "gfm"
  }
}

apply(from = "$rootDir/gradle-mvn-publish.gradle")
