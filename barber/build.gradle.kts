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

  implementation(libs.guava)
  implementation(libs.kotlinStdLibJdk8)
  implementation(libs.kotlinReflection)
  implementation(libs.moshiCore)
  implementation(libs.moshiKotlin)
  implementation(libs.okio)
  implementation(libs.okHttp)
  implementation(libs.mustacheCompiler)
  implementation(libs.wireMoshiAdapter)
  implementation(libs.wireRuntime)

  testImplementation(libs.assertj)
  testImplementation(libs.junitApi)
  testImplementation(libs.junitEngine)
  testImplementation(libs.kotlinTest)
}

configure<MavenPublishBaseExtension> {
  configure(
    KotlinJvm(javadocJar = JavadocJar.Dokka("dokkaGfm"))
  )
}
