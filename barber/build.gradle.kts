import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
  kotlin("jvm")
  `java-library`
  id("com.vanniktech.maven.publish.base")
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

configure<MavenPublishBaseExtension> {
  configure(
    KotlinJvm(javadocJar = JavadocJar.Dokka("dokkaGfm"))
  )
}
