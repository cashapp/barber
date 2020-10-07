package app.cash.barber.models

import app.cash.barber.BarberException
import app.cash.barber.BarberMustacheFactoryProvider
import app.cash.barber.BarbershopBuilder
import app.cash.barber.rootKey
import app.cash.protos.barber.api.DocumentTemplate
import com.github.mustachejava.Mustache
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import java.io.StringReader
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

/**
 * An intermediary data class used in processing [DocumentTemplate] that permits for null values in
 *  fields and pre-compilation of Mustache templates in fields.
 * This allows for a [CompiledDocumentTemplate].fields to contain the same keys as the target
 *  [Document] (even for [Document] keys that are nullable) and improve Mustache execution runtime.
 */
data class CompiledDocumentTemplate(
  val fields: Table<String, KClass<out Document>, Mustache?>,
  val targets: Set<KClass<out Document>>,
  val version: Long = 0
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
    /** Pre-compile and validate Mustache templates */
    internal fun DocumentTemplate.compileAndValidate(
      mustacheFactoryProvider: BarberMustacheFactoryProvider,
      installedDocuments: Table<BarberSignature, String, BarbershopBuilder.DocumentDb>,
      warnings: MutableList<String>,
      warningsAsErrors: Boolean
    ): CompiledDocumentTemplate {
      val templateToken = TemplateToken(template_token!!)

      // Initialize BarberException
      val errors: MutableList<String> = mutableListOf()

      // Common projections for the following validation
      val targetKParameterDocumentMap = target_signatures.map { signature ->
        installedDocuments.row(BarberSignature(signature)).values.map {
          it.document to it.kParameter
        }
      }.reduce { acc, list -> acc + list }
          .toSet()
          .fold(mapOf<KParameter, KClass<out Document>>()) { acc, (document, kParameter) ->
            acc + mapOf(kParameter to document)
          }
      val targetFieldNames = targetKParameterDocumentMap.keys.map {
        it.name
      }
      val nonNullableTargetKParameterDocumentMap = targetKParameterDocumentMap.filter {
        !it.key.type.isMarkedNullable
      }

      // Documents listed in DocumentTemplate.target_signatures must be installed
      val installedDocumentSignatures = installedDocuments.rowKeySet().map { it.signature }.toSet()
      val notInstalledDocumentSignatures = target_signatures.filterNot {
        installedDocumentSignatures.contains(it)
      }.map { BarberSignature(it) }
      if (notInstalledDocumentSignatures.isNotEmpty()) {
        errors.add("""
          |Attempted to install DocumentTemplate without the corresponding Document being installed.
          |Not installed DocumentTemplate.target_signatures:
          |$notInstalledDocumentSignatures
          """.trimMargin())
      }

      BarberException.maybeThrowBarberException(errors = errors, warnings = warnings,
          warningsAsErrors = warningsAsErrors)

      // Confirm that all field names required to render documents are present in DocumentTemplate
      val documentTemplateFieldNames = fields.map { it.key }.toSet()
      val nonNullableTargetFieldNames =
          nonNullableTargetKParameterDocumentMap.keys.map { it.name }.toSet()
      if (!documentTemplateFieldNames.containsAll(nonNullableTargetFieldNames)) {
        val missingFields = nonNullableTargetFieldNames.subtract(documentTemplateFieldNames)
        val documentsWithMissingFields = nonNullableTargetKParameterDocumentMap.filter {
          missingFields.contains(it.key.name)
        }.map { (kParameter, document) -> "[document=${document.qualifiedName}] requires missing [field=${kParameter.name}]" }
            .joinToString("\n")
        errors.add("""
              |Installed ${this.getKey()}
              |missing required fields for Document targets:
              |$documentsWithMissingFields
              """.trimMargin())
      }
      if (documentTemplateFieldNames.size > targetFieldNames.size) {
        val additionalFields = documentTemplateFieldNames.filter { field ->
          !targetFieldNames.contains(field)
        }
        errors.add("""
              |Installed DocumentTemplate has additional fields that are not used in any target Document
              |Additional fields:
              |${additionalFields.joinToString("\n")}
            """.trimMargin())
      }

      // Compile the DocumentTemplate to enable further validation
      // fieldName, Document, Mustache used to render the field
      val documentTemplateFields =
          HashBasedTable.create<String, KClass<out Document>, Mustache?>()
      fields.map { field ->
        val fieldName = field.key!!
        val fieldValue = field.template!!
        installedDocuments.column(fieldName).keys.forEach { signature ->
          // Render using a MustacheFactory that will respect any field BarberFieldEncoding annotations
          val barberField = installedDocuments.get(signature, fieldName).kParameter
              .annotations
              .firstOrNull { it is BarberField } as BarberField?
          val mustache = mustacheFactoryProvider.get(barberField?.encoding)
              .compile(StringReader(fieldValue), fieldValue)
          val document = installedDocuments.row(signature)[fieldName]!!.document
          documentTemplateFields.put(fieldName, document, mustache)
        }
      }

      val targets = documentTemplateFields.columnKeySet()
      val compiledDocumentTemplate = CompiledDocumentTemplate(
          fields = documentTemplateFields,
          targets = targets,
          version = version!!
      )

      // DocumentTemplates must only use variables from source DocumentData in their fields
      val signatureFieldNames = BarberSignature(source_signature!!).fields.keys
      compiledDocumentTemplate.reducedFieldCodeMap().forEach { (name, codes) ->
        // Check for missing variables in field templates
        codes.forEach { code ->
          if (code !in signatureFieldNames) {
            val field = fields.find { it.key == name }
            errors.add(
                "Missing variable [$code] for DocumentData with [templateToken=$templateToken] for DocumentTemplate field [$field]")
          }
        }
      }

      BarberException.maybeThrowBarberException(errors = errors, warnings = warnings,
          warningsAsErrors = warningsAsErrors)


      // Check for unused Source signature field not used in any installed DocumentTemplate field
      val codes = compiledDocumentTemplate.reducedFieldCodeSet()
      val signature = BarberSignature(source_signature)
      signature.fields.forEach { (fieldName, _) ->
        if (!codes.map { it.rootKey() }.contains(fieldName)) {
          warnings.add("""
          |Unused DocumentData variable [$fieldName] in Source signature [$source_signature] with no
          |usage in ${this.getKey()} 
        """.trimMargin())
        }
      }

      BarberException.maybeThrowBarberException(errors = errors, warnings = warnings,
          warningsAsErrors = warningsAsErrors)

      return compiledDocumentTemplate
    }

    fun DocumentTemplate.getKey() = "DocumentTemplate: [templateToken=$template_token][locale=$locale][version=$version]"

    /** Returns values from a Map as an aggregated set */
    fun Map<*, Set<String>>.reduceToValuesSet(): Set<String> =
        if (values.isNotEmpty()) {
          values.reduce { acc, codes -> acc + codes }
        } else {
          setOf()
        }
  }
}
