package app.cash.barber

import app.cash.barber.models.CompiledDocumentTemplate
import app.cash.barber.models.DocumentTemplate
import app.cash.barber.models.Locale
import com.github.mustachejava.Mustache
import kotlin.reflect.KFunction

/**
 * Helper Functions
 */
internal fun Map<String, Mustache?>.asFieldCodesMap() =
  mapValues { (_, template: Mustache?) ->
    template?.codes?.mapNotNull { it.name }?.toSet() ?: setOf<String>()
  }

internal fun Map<*, Set<String>>.reduceSet() =
  values.reduce { acc, codes -> acc + codes }

internal fun Map<String, Mustache?>.reducedFieldCodes() =
  asFieldCodesMap().reduceSet()

internal fun MutableMap<Locale, Pair<DocumentTemplate, CompiledDocumentTemplate>>.reducedFieldCodeSet() =
  mapValues { (_, documentTemplate) ->
    documentTemplate.second.fields.reducedFieldCodes()
  }.reduceSet()

internal fun KFunction<*>.asParameterNames() = parameters.associateBy { it.name }

internal fun String.rootKey() = split(".").first()