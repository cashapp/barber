package com.squareup.barber

import com.github.mustachejava.DefaultMustacheFactory
import com.squareup.barber.models.CopyModel
import com.squareup.barber.models.DocumentSpec
import java.io.StringReader
import java.io.StringWriter
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class RealRenderer<C : CopyModel, D : DocumentSpec>(
  private val documentSpecConstructor: KFunction<D>,
  private val documentSpecParametersByName: Map<String?, KParameter>,
  private val documentCopyFields: Map<String, String?>
) : Renderer<C, D> {
  override fun render(copyModel: C): D {
    // Render each field of DocumentCopy with passed in CopyModel context
    // Some of these fields now will be null since any missing fields will have been added with null values
    val renderedDocumentCopyFields = documentCopyFields.mapValues {
      when (it.value) {
        null -> it.value
        else -> renderMustache(it.value!!, copyModel)
      }
    }

    // Zips the KParameters with corresponding rendered values from DocumentCopy
    val parameters = renderedDocumentCopyFields.filter {
      documentSpecParametersByName .containsKey(it.key)
    }.mapKeys {
      documentSpecParametersByName [it.key] ?: throw BarberException(
        problems = listOf("Missing KParameter for ${it.key}"))
    }

    // Build the DocumentSpec instance with the rendered DocumentCopy parameters
    return documentSpecConstructor.callBy(parameters)
  }

  companion object {
    private val mustacheFactory = DefaultMustacheFactory()
    // TODO split off compile and execute functions to allow for precompilation on install of DocumentCopy
    fun renderMustache(mustacheTemplate: String, copyModel: CopyModel): String {
      val writer = StringWriter()
      val compiledMustache = mustacheFactory.compile(StringReader(mustacheTemplate), mustacheTemplate)
      compiledMustache.execute(writer, copyModel)
      writer.flush()
      return writer.toString()
    }
  }
}