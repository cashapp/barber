package app.cash.barber

import app.cash.barber.models.Document
import app.cash.barber.locale.Locale
import app.cash.barber.models.VersionRange
import app.cash.protos.barber.api.DocumentData

/**
 * Knows how to render a Document from a DocumentTemplate and context variables in DocumentData
 */
interface Barber<D : Document> {
  /**
   * Set of the DocumentTemplate version ranges that are supported by this Barber
   * This prevents the case of a DocumentTemplate changing which documents are targetted and a Barber
   * not being able to support the change and blowing up at runtime
   */
  val supportedVersionRanges: Set<VersionRange>

  /**
   * Render using a DocumentData Kotlin data class
   * @param version is optional additional parameter; latest compatible will be used if not provided
   */
  fun <DD : app.cash.barber.models.DocumentData> render(documentData: DD, locale: Locale, version: Long? = null): D

  /**
   * Render using a DocumentData proto
   * @param version is optional additional parameter; latest compatible will be used if not provided
   */
  fun render(documentData: DocumentData, locale: Locale, version: Long? = null): D
}
