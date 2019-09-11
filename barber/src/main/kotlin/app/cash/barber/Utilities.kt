package app.cash.barber

import app.cash.barber.models.CompiledDocumentTemplate
import app.cash.barber.models.Locale
import com.github.mustachejava.Mustache
import kotlin.reflect.KFunction

internal fun Map<String, Mustache?>.asFieldCodesMap() =
  mapValues { (_, template: Mustache?) ->
    template?.codes?.mapNotNull { it.name }?.toSet() ?: setOf<String>()
  }

internal fun Map<*, Set<String>>.reduceSet(): Set<String> =
  if (values.isNotEmpty()) {
    values.reduce { acc, codes -> acc + codes }
  } else {
    setOf()
  }

internal fun Map<String, Mustache?>.reducedFieldCodes() =
  asFieldCodesMap().reduceSet()

internal fun MutableMap<Locale, CompiledDocumentTemplate>.reducedFieldCodeSet() =
  mapValues { (_, documentTemplate) ->
    documentTemplate.fields.reducedFieldCodes()
  }.reduceSet()

internal fun KFunction<*>.asParameterNames() = parameters.associateBy { it.name }

internal fun Mustache?.asString(): String = if (this == null) {
  ""
} else {
  name
}

/**
 * Nested objects is supported in DocumentData but makes variable validation more of a challenge.
 * Mustache templates referencing nested objects have keys with the flattened hierarchy separated by
 * dots as is clear in the example below where variables are references as "button.url" and
 * "button.label".
 *
 * ```
 * data class ButtonClass(
 *   val color: String,
 *   val label: String,
 *   val url: String
 * )
 *
 * data class ButtonDocumentData(
 *   val button: ButtonClass
 * )
 *
 * val template = DocumentTemplate(
 *   fields = mapOf(
 *     "btn" to "<Button url={{button.url}}>{{button.label}}</Button>"
 *   ),
 *   source = ButtonDocumentData::class,
 *   ...
 * )
 * ```
 *
 * A compiled Mustache template reveals parsed codes that then are used to check against the
 * provided variables from a DocumentData. Unfortunately the parsed codes don't take into account
 * nested objects, and only are returned as "button.url". To provide rudimentary validation that at
 * minimum "button" exists in the DocumentData, the following function is used to pull out the root
 * key from the object path. For example, "button.url".rootKey() => "button".
 *
 * TODO add validation for nested object access in Mustache templates
 * ```
 */
internal fun String.rootKey() = split(".").first()