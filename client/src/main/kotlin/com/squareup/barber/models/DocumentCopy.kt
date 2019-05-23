package com.squareup.barber.models

import kotlin.reflect.KClass

/**
 * For each CopyModel we have a DocumentCopy that provides a natural language for the document.
 * It uses Mustache templates to provide openings for the CopyModel fields.
 *
 * Each DocumentCopy is specific to a locale.
 *
 * @param [fields] Map of a DocumentSpec output key to a template String value that can contain CopyModel input values
 * @param [source] KClass of CopyModel
 * @param [targets] Set of DocumentSpecs that DocumentCopy can render to
 * @param [locale] Barber.Locale that scopes DocumentCopy to a languages/country Locale
 */
data class DocumentCopy(
  val fields: Map<String, String>,
  val source: KClass<out CopyModel>,
  val targets: Set<KClass<out DocumentSpec>>,
  val locale: Locale
)