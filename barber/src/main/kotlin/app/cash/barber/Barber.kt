package app.cash.barber

import app.cash.barber.models.Document
import app.cash.barber.models.Locale
import app.cash.protos.barber.api.DocumentData

/**
 * Knows how to render a Document from a DocumentTemplate and context variables in DocumentData
 */
interface Barber<D : Document> {
  /** Render using a DocumentData Kotlin data class */
  fun <DD : app.cash.barber.models.DocumentData> render(documentData: DD, locale: Locale, version: Long = 0): D

  /** Render using a DocumentData proto */
  fun render(documentData: DocumentData, locale: Locale, version: Long): D
}
