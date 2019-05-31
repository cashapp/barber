package com.squareup.barber.models

import kotlin.reflect.KClass

/**
 * For each DocumentData we have a DocumentTemplate that provides a natural language for the document.
 * It uses Mustache templates to provide openings for the DocumentData fields.
 *
 * Each DocumentTemplate is specific to a locale.
 *
 * @param [fields] Map of a Document output key to a template String value that can contain DocumentData input values
 * @param [source] KClass of DocumentData
 * @param [targets] Set of Documents that DocumentTemplate can render to
 * @param [locale] Barber.Locale that scopes DocumentTemplate to a languages/country Locale
 */
data class DocumentTemplate(
  val fields: Map<String, String>,
  val source: KClass<out DocumentData>,
  val targets: Set<KClass<out Document>>,
  val locale: Locale
)