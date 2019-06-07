package com.squareup.barber.models

import kotlin.reflect.KClass

/**
 * An intermediary data class used in processing [DocumentTemplate] that permits for null values in fields
 * This allows for a [FieldNullableDocumentTemplate].fields to contain the same keys as the target [Document]
 *  even for [Document] keys that are nullable.
 */
data class FieldNullableDocumentTemplate(
  val fields: Map<String, String?>,
  val source: KClass<out DocumentData>,
  val targets: Set<KClass<out Document>>,
  val locale: Locale
)