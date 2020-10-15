package app.cash.barber.models

import kotlin.reflect.KClass

/**
 * Identifies a semantic template and is shared across multiple versions and locales of
 * DocumentData and DocumentTemplate
 */
data class TemplateToken(
  val token: String
) {
  override fun toString(): String = token

  companion object {
    /**
     * Provides interoperability with DocumentData API of always having a templateToken string
     *
     * Class: AppLoginDocumentData
     * TemplateToken: appLogin
     */
    fun KClass<out DocumentData>.getTemplateToken(): TemplateToken {
      val capitalizedNotificationId = simpleName!!.removeSuffix("DocumentData")
      val firstLetter = capitalizedNotificationId.first().toLowerCase()
      val rest = capitalizedNotificationId.drop(1)
      val token = firstLetter + rest
      return TemplateToken(token)
    }
  }
}