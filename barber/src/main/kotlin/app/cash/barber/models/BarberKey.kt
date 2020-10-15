package app.cash.barber.models

import app.cash.barber.models.TemplateToken.Companion.getTemplateToken
import kotlin.reflect.KClass

data class BarberKey(
  val templateToken: TemplateToken,
  val document: KClass<out Document>
) {
  constructor(
    documentData: KClass<out DocumentData>,
    document: KClass<out Document>
  ) : this(documentData.getTemplateToken(), document)
}
