package app.cash.barber

import app.cash.barber.models.Document
import app.cash.barber.models.DocumentData
import app.cash.barber.models.DocumentTemplate
import app.cash.barber.models.Locale

interface Barber<C : DocumentData, D : Document> {
  /**
   * @return a [Document] with the values of a [DocumentData] instance rendered in the [DocumentTemplate]
   */
  fun render(documentData: C, locale: Locale): D
}