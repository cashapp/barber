package app.cash.barber

import app.cash.barber.models.BarberSignature
import app.cash.barber.models.BarberSignature.Companion.getBarberSignature
import app.cash.barber.models.Document
import app.cash.barber.models.Locale
import app.cash.protos.barber.api.DocumentData
import com.github.mustachejava.Mustache
import com.google.common.collect.Table
import java.io.StringWriter
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

internal class RealBarber<D : Document>(
  private val document: KClass<out D>,
  private val installedDocuments: Table<BarberSignature, String, BarbershopBuilder.DocumentDb>,
  private val compiledDocumentTemplateLocaleVersions: Map<Locale, Table<BarberSignature, Long, BarbershopBuilder.DocumentTemplateDb>>,
  private val localeResolver: LocaleResolver
) : Barber<D> {
  override fun <DD : app.cash.barber.models.DocumentData> render(documentData: DD, locale: Locale, version: Long): D =
      render(documentData.toProto(), locale, version)

  override fun render(
    documentData: DocumentData,
    locale: Locale,
    version: Long
  ): D {
    val documentDataMap = documentData.fields.fold(mapOf<String, String>()) { acc, field ->
      val value = (field.value_string
      // TODO add configurable formatters based on locale for non-string types
          ?: field.value_long?.toString()
          ?: field.value_duration?.toString()
          ?: field.value_instant.toString())
      acc + mapOf(field.key!! to value)
    }

    val documentDataSignature = documentData.getBarberSignature()

    // Only allow locale lookup on installed versions that can be satisfied by the provided DocumentData
    val compatibleLocaleVersions = compiledDocumentTemplateLocaleVersions.mapValues { (_, versionRecords) ->
      val validVersions = mutableMapOf<Long, BarbershopBuilder.DocumentTemplateDb>()
      versionRecords.cellSet().map { cell ->
        if (documentDataSignature.canSatisfy(cell.rowKey!!)) {
          validVersions[cell.columnKey!!] = cell.value!!
        }
      }
      if (validVersions.isEmpty()) {
        null
      } else {
        validVersions
      }
    }.toMap().filterValues { it != null }

    // Choose a Locale based on available installed versions
    val versionsForLocale = localeResolver.resolve(locale, compatibleLocaleVersions)

    // Resolve exact version or latest compatible based on DocumentData BarberSignature
    val documentTemplateDB = versionsForLocale[version]
    // TODO (adrw) confirm this chooses highest possible version
        ?: let {
          val sortedVersionMap = versionsForLocale.toSortedMap()
          val maxVersion = sortedVersionMap.entries.last()
          maxVersion.value
        }


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
    val parameters = installedDocuments.row(document.getBarberSignature()).map { (fieldName, documentDB) ->
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
