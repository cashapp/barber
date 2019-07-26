package app.cash.barber.models

import app.cash.barber.asParameterNames
import com.github.mustachejava.Mustache
import com.github.mustachejava.MustacheFactory
import java.io.StringReader
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * For each DocumentData we have a DocumentTemplate that provides a natural language for the document.
 * It uses Mustache templates to provide openings for the DocumentData fields.
 *
 * Each DocumentTemplate is specific to a locale.
 *
 * @param [fields] Map of a Document output key to a template String value that can contain DocumentData input values
 * @param [source] KClass of DocumentData
 * @param [targets] Set of Documents that DocumentTemplate can render to
 * @param [locale] Barbershop.Locale that scopes DocumentTemplate to a languages/country Locale
 */
data class DocumentTemplate(
  val fields: Map<String, String>,
  val source: KClass<out DocumentData>,
  val targets: Set<KClass<out Document>>,
  val locale: Locale
) {
  fun compile(mustacheFactory: MustacheFactory): CompiledDocumentTemplate {
    // Pre-compile Mustache templates
    val documentDataFields: MutableMap<String, Mustache?> =
      fields.mapValues {
        mustacheFactory.compile(StringReader(it.value), it.value)
      }.toMutableMap()

    // Find missing fields in DocumentTemplate
    // Missing fields occur when a nullable field in Document is not an included key in the DocumentTemplate fields
    // In the Parameters Map in the Document constructor though, all parameter keys must be present (including
    // nullable)
    val combinedDocumentParameterNames = targets.map { target ->
      target.primaryConstructor!!.asParameterNames().keys.filterNotNull()
    }.reduce { acc, names -> acc + names }.toSet()

    // Initialize keys for missing fields in DocumentTemplate
    combinedDocumentParameterNames.mapNotNull { documentDataFields.putIfAbsent(it, null) }

    return CompiledDocumentTemplate(
      fields = documentDataFields,
      source = source,
      targets = targets,
      locale = locale)
  }
}