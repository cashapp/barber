package app.cash.barber

import app.cash.barber.locale.Locale
import app.cash.barber.locale.LocaleResolver
import app.cash.barber.models.BarberSignature
import app.cash.barber.models.BarberSignature.Companion.getBarberSignature
import app.cash.barber.models.Document
import app.cash.barber.models.VersionRange
import app.cash.barber.models.VersionRange.Companion.supports
import app.cash.protos.barber.api.DocumentData
import com.github.mustachejava.Mustache
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import java.io.StringWriter
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

internal class RealBarber<D : Document>(
  private val document: KClass<out D>,
  private val installedDocuments: Table<BarberSignature, String, BarbershopBuilder.DocumentDb>,
  private val installedDocumentTemplates: Map<DocumentTemplateKey, BarbershopBuilder.DocumentTemplateDb>,
  private val localeResolver: LocaleResolver,
  override val supportedVersionRanges: Set<VersionRange>
) : Barber<D> {
  internal data class DocumentTemplateKey(
    val locale: Locale,
    val sourceBarberSignature: BarberSignature,
    val version: Long
  )

  override fun <DD : app.cash.barber.models.DocumentData> render(
    documentData: DD,
    locale: Locale,
    version: Long?
  ): D = render(documentData.toProto(), locale, version)

  override fun render(documentData: DocumentData, locale: Locale, version: Long?): D {
    val documentDataMap = documentData.fields.associate { field ->
      val value = extractAndFormatFieldValue(field)
      field.key!! to value
    }

    val documentDataSignature = documentData.getBarberSignature()

    val compatibleLocaleVersions =
        installedDocumentTemplates.filter { (it, _) ->
          // Only allow locale lookup on installed versions that can be satisfied by the provided DocumentData
          documentDataSignature.canSatisfy(it.sourceBarberSignature)
        }.filter { (key, _) ->
          // Only allow locale lookup on versions that can be satisfied by this specific Barber and target Document
          supportedVersionRanges.supports(key.version)
        }

    // Choose a Locale based on available installed versions
    val localeLookupTable =
        HashBasedTable.create<Locale, Long, BarbershopBuilder.DocumentTemplateDb>()
    compatibleLocaleVersions.forEach { (documentTemplateKey, documentTemplateDb) ->
      localeLookupTable.put(documentTemplateKey.locale, documentTemplateKey.version,
          documentTemplateDb)
    }
    val versionsForLocale = localeResolver.resolve(locale, localeLookupTable)

    // Resolve exact version or latest compatible based on DocumentData BarberSignature
    val documentTemplateDB = versionsForLocale[version ?: supportedVersionRanges
        .maxByOrNull { it.max }?.max] ?: versionsForLocale.maxByOrNull { it.key }?.value
    val compiledDocumentTemplate = documentTemplateDB?.compiledDocumentTemplate
        ?: throw BarberException(errors = listOf("""
            |RealBarber unable to find a DocumentTemplate version that matches the input DocumentData signature 
            |DocumentData Signature: $documentDataSignature
            |""".trimMargin()))

    // Render each field of DocumentTemplate with passed in DocumentData context
    // Some of these fields now will be null since any missing fields will have been added with null values
    val renderedDocumentTemplateFields: Map<String, String?> =
        compiledDocumentTemplate.fields.column(document)
            .mapValues {
              it.value.renderMustache(documentDataMap)
            }

    // Zips the KParameters with corresponding rendered values from DocumentTemplate
    val parameters =
        installedDocuments.row(document.getBarberSignature()).map { (fieldName, documentDB) ->
          documentDB.kParameter to when {
            renderedDocumentTemplateFields.containsKey(fieldName) -> {
              renderedDocumentTemplateFields.getValue(fieldName)
            }
            // Initialize any nullable not included fields as null
            documentDB.kParameter.type.isMarkedNullable -> {
              null
            }
            // This case should never be hit because it is covered in BarbershopBuilder validation
            else -> {
              throw BarberException(errors = listOf("""
            |RealBarber unable to render 
            |DocumentData: $documentData
            |DocumentTemplate: $compiledDocumentTemplate
            |Document: $document
            |""".trimMargin()))
            }
          }
        }.toMap()

    // Build the Document instance with the rendered DocumentTemplate parameters
    return document.primaryConstructor!!.callBy(parameters)
  }

  /** TODO add configurable formatters based on locale for non-string types */
  private fun extractAndFormatFieldValue(field: DocumentData.Field): String = field.value_string
      ?: field.value_long?.toString()
      ?: field.value_duration?.toString()
      ?: field.value_instant?.toString()
      // If all values are null, render null as empty string
      ?: ""

  /* Render a pre-compiled nullable Mustache template */
  private fun Mustache?.renderMustache(context: Map<String, String>): String? = if (this == null) {
    null
  } else {
    val writer = StringWriter()
    execute(writer, context)
    writer.flush()
    writer.toString()
  }
}
