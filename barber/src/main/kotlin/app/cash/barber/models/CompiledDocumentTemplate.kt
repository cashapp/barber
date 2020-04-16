package app.cash.barber.models

import com.github.mustachejava.Mustache
import com.google.common.collect.Table
import kotlin.reflect.KClass

/**
 * An intermediary data class used in processing [DocumentTemplate] that permits for null values in
 *  fields and pre-compilation of Mustache templates in fields.
 * This allows for a [CompiledDocumentTemplate].fields to contain the same keys as the target
 *  [Document] (even for [Document] keys that are nullable) and improve Mustache execution runtime.
 */
data class CompiledDocumentTemplate(
  val fields: Table<String, KClass<out Document>, Mustache?>,
  val source: KClass<out DocumentData>,
  val targets: Set<KClass<out Document>>,
  val locale: Locale
) {
  /** Return map of fieldName to set of Mustache codes in the field template */
  fun reducedFieldCodeMap() = fields.columnMap().values.map { fieldNameMustacheMap ->
    fieldNameMustacheMap.mapValues { (_, template: Mustache?) ->
      template?.codes?.mapNotNull { it.name }?.toSet() ?: setOf()
    }
  }.let {
    if (it.isNotEmpty()) {
      return@let it.reduce { acc, codes -> acc + codes }
    } else {
      return@let mapOf<String, Set<String>>()
    }
  }

  /** Returns set of all Mustache codes in DocumentTemplate */
  fun reducedFieldCodeSet() = reducedFieldCodeMap().reduceToValuesSet()

  companion object {
    /** Returns values from a Map as an aggregated set */
    fun Map<*, Set<String>>.reduceToValuesSet(): Set<String> =
        if (values.isNotEmpty()) {
          values.reduce { acc, codes -> acc + codes }
        } else {
          setOf()
        }
  }
}
