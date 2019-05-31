package com.squareup.barber

import com.github.mustachejava.DefaultMustacheFactory
import com.squareup.barber.models.DocumentData
import com.squareup.barber.models.Document
import java.io.StringReader
import java.io.StringWriter
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class RealRenderer<C : DocumentData, D : Document>(
  private val documentConstructor: KFunction<D>,
  private val documentParametersByName: Map<String?, KParameter>,
  private val documentTemplateFields: Map<String, String?>
) : Renderer<C, D> {
  override fun render(documentData: C): D {
    // Render each field of DocumentTemplate with passed in DocumentData context
    // Some of these fields now will be null since any missing fields will have been added with null values
    val renderedDocumentDataFields = documentTemplateFields.mapValues {
      when (it.value) {
        null -> it.value
        else -> renderMustache(it.value!!, documentData)
      }
    }

    // Zips the KParameters with corresponding rendered values from DocumentTemplate
    val parameters = renderedDocumentDataFields.filter {
      documentParametersByName .containsKey(it.key)
    }.mapKeys {
      documentParametersByName.getValue(it.key)
    }

    // Build the Document instance with the rendered DocumentTemplate parameters
    return documentConstructor.callBy(parameters)
  }

  companion object {
    private val mustacheFactory = DefaultMustacheFactory()
    // TODO split off compile and execute functions to allow for precompilation on install of DocumentTemplate
    fun renderMustache(mustacheTemplate: String, documentData: DocumentData): String {
      val writer = StringWriter()
      val compiledMustache = mustacheFactory.compile(StringReader(mustacheTemplate), mustacheTemplate)
      compiledMustache.execute(writer, documentData)
      writer.flush()
      return writer.toString()
    }
  }
}