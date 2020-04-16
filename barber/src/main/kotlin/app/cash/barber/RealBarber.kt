package app.cash.barber

import app.cash.barber.models.CompiledDocumentTemplate
import app.cash.barber.models.Document
import app.cash.barber.models.DocumentData
import app.cash.barber.models.Locale
import com.github.mustachejava.Mustache
import com.google.common.collect.Table
import java.io.StringWriter
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

internal class RealBarber<C : DocumentData, D : Document>(
  private val document: KClass<out D>,
  private val installedDocuments: Table<String, KClass<out Document>, KParameter>,
  private val compiledDocumentTemplateLocales: Map<Locale, CompiledDocumentTemplate>,
  private val localeResolver: LocaleResolver
) : Barber<C, D> {
  override fun render(documentData: C, locale: Locale): D {
    val documentTemplate = localeResolver.resolve(locale, compiledDocumentTemplateLocales)

    // Render each field of DocumentTemplate with passed in DocumentData context
    // Some of these fields now will be null since any missing fields will have been added with null values
    val renderedDocumentTemplateFields: Map<String, String?> =
        documentTemplate.fields.column(document)
            .mapValues {
              it.value.renderMustache(documentData)
            }

    // Zips the KParameters with corresponding rendered values from DocumentTemplate
    val parameters = installedDocuments.column(document).map { (fieldName, kParameter) ->
      kParameter to when {
        renderedDocumentTemplateFields.containsKey(fieldName) -> {
          renderedDocumentTemplateFields.getValue(fieldName)
        }
        // Initialize any nullable not included fields as null
        kParameter.type.isMarkedNullable -> {
          null
        }
        // This case should never be hit because it is covered in BarbershopBuilder validation
        else -> {
          throw BarberException(errors = listOf("""
            |RealBarber unable to render 
            |DocumentData: $documentData
            |DocumentTemplate: $documentTemplate
            |Document: $document
            |""".trimMargin()))
        }
      }
    }.toMap()

    // Build the Document instance with the rendered DocumentTemplate parameters
    return document.primaryConstructor!!.callBy(parameters)
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
