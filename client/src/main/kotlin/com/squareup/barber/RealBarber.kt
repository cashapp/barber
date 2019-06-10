package com.squareup.barber

import com.github.mustachejava.Mustache
import com.squareup.barber.LocaleResolver.Companion.resolveEntry
import com.squareup.barber.models.CompiledDocumentTemplate
import com.squareup.barber.models.Document
import com.squareup.barber.models.DocumentData
import com.squareup.barber.models.Locale
import java.io.StringWriter
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class RealBarber<C : DocumentData, D : Document>(
  private val documentConstructor: KFunction<D>,
  private val documentParametersByName: Map<String?, KParameter>,
  private val compiledDocumentTemplateLocales: Map<Locale, CompiledDocumentTemplate>,
  private val localeResolver: LocaleResolver
) : Barber<C, D> {
  override fun render(documentData: C, locale: Locale): D {
    val documentTemplate = compiledDocumentTemplateLocales.resolveEntry(localeResolver, locale)
    val documentTemplateFields = documentTemplate.fields

    // Render each field of DocumentTemplate with passed in DocumentData context
    // Some of these fields now will be null since any missing fields will have been added with null values
    val renderedDocumentDataFields: Map<String, String?> = documentTemplateFields.mapValues {
      it.value.renderMustache(documentData)
    }

    // Zips the KParameters with corresponding rendered values from DocumentTemplate
    val parameters = renderedDocumentDataFields.filter {
      documentParametersByName.containsKey(it.key)
    }.mapKeys {
      documentParametersByName.getValue(it.key)
    }

    // Build the Document instance with the rendered DocumentTemplate parameters
    return documentConstructor.callBy(parameters)
  }

  /* Render a pre-compiled nullable Mustache template */
  private fun Mustache?.renderMustache(documentData: DocumentData): String? = if (this == null) {
    null
  } else {
    val writer = StringWriter()
    execute(writer, documentData)
    writer.flush()
    writer.toString()
  }
}