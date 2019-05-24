package com.squareup.barber

import com.github.mustachejava.DefaultMustacheFactory
import com.squareup.barber.models.CopyModel
import java.io.StringReader
import java.io.StringWriter

/**
 * Barber Render uses Mustache to render templates
 */
internal class BarberRender {
  private val mustacheFactory = DefaultMustacheFactory()

  // TODO split off compile and execute functions to allow for precompilation on install of DocumentCopy
  fun render(mustacheTemplate: String, copyModel: CopyModel): String {
    val writer = StringWriter()
    val compiledMustache = mustacheFactory.compile(StringReader(mustacheTemplate), mustacheTemplate)
    compiledMustache.execute(writer, copyModel)
    writer.flush()
    return writer.toString()
  }
}