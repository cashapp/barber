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

apply(from = "$rootDir/gradle-mvn-publish.gradle")
