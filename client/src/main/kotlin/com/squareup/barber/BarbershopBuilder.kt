package com.squareup.barber

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import com.squareup.barber.models.BarberKey
import com.squareup.barber.models.CompiledDocumentTemplate
import com.squareup.barber.models.Document
import com.squareup.barber.models.DocumentData
import com.squareup.barber.models.DocumentTemplate
import com.squareup.barber.models.Locale
import java.io.StringReader
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class BarbershopBuilder : Barbershop.Builder {
  private val installedDocumentTemplates: Table<KClass<out DocumentData>, Locale, DocumentTemplate> =
    HashBasedTable.create()
  private val installedDocument: MutableSet<KClass<out Document>> = mutableSetOf()
  private val mustacheFactory = DefaultMustacheFactory()
  private var localeResolver: LocaleResolver = MatchOrFirstLocaleResolver()

  override fun installDocumentTemplate(
    documentDataClass: KClass<out DocumentData>,
    documentTemplate: DocumentTemplate
  ) = apply {
    val installedDocumentTemplatesHasKey = installedDocumentTemplates.containsRow(documentDataClass)
    val installedDocumentTemplateIsNotAlreadyInstalled =
      installedDocumentTemplates.get(documentDataClass, documentTemplate.locale) == documentTemplate
    if (installedDocumentTemplatesHasKey && installedDocumentTemplateIsNotAlreadyInstalled) {
      throw BarberException(problems = listOf("""
        |Attempted to install DocumentTemplate that will overwrite an already installed DocumentTemplate with locale
        |${documentTemplate.locale}.
        |Already Installed
        |DocumentData: $documentDataClass
        |DocumentTemplate: ${installedDocumentTemplates.row(documentDataClass)}
        |
        |Attempted to Install
        |$documentTemplate
        """.trimMargin()))
    }
    installedDocumentTemplates.put(documentDataClass, documentTemplate.locale, documentTemplate)
  }

  inline fun <reified DD : DocumentData> installDocumentTemplate(documentTemplate: DocumentTemplate) = installDocumentTemplate(
    DD::class, documentTemplate)

  override fun installDocument(document: KClass<out Document>) = apply {
    installedDocument.add(document)
  }

  inline fun <reified D : Document> installDocument() = installDocument(D::class)

  override fun setLocaleResolver(resolver: LocaleResolver): Barbershop.Builder = apply {
    localeResolver = resolver
  }

  /**
   * Validates BarbershopBuilder inputs and returns a Barbershop instance with the installed and validated elements
   */
  private fun validate() {
    val problems: MutableList<String> = mutableListOf()
    installedDocumentTemplates.cellSet().forEach { cell ->
      val documentDataClass = cell.rowKey!!
      val documentTemplate = cell.value!!

      // DocumentTemplate must be installed with a DocumentData that is listed in its Source
      if (documentDataClass != documentTemplate.source) {
        problems.add("""
          |Attempted to install DocumentTemplate with a DocumentData not specified in the DocumentTemplate source.
          |DocumentTemplate.source: ${documentTemplate.source}
          |DocumentData: $documentDataClass
          """.trimMargin())
      }

      // Documents listed in DocumentTemplate.Targets must be installed
      val notInstalledDocument = documentTemplate.targets.filter {
        !installedDocument.contains(it)
      }
      if (notInstalledDocument.isNotEmpty()) {
        problems.add("""
          |Attempted to install DocumentTemplate without the corresponding Document being installed.
          |Not installed DocumentTemplate.targets:
          |$notInstalledDocument
          """.trimMargin())
      }

      // Document targets must have primaryConstructor
      // and installedDocumentTemplates must be able to fulfill Document target parameter requirements
      val documents = documentTemplate.targets
      documents.forEach { document ->
        // Validate that Document has a Primary Constructor
        val documentConstructor = document.primaryConstructor

        if (documentConstructor == null) {
          problems.add("No primary constructor for Document class ${document::class}.")
        } else {
          // Determine non-nullable required parameters
          val requiredParameterNames = documentConstructor.parameters.filter {
            !it.type.isMarkedNullable
          }.map { it.name }

          // Confirm that required parameters are present in installedDocumentTemplates
          if (!documentTemplate.fields.keys.containsAll(requiredParameterNames)) {
            val missingFields = requiredParameterNames.filter {
              !documentTemplate.fields.containsKey(it)
            }
            problems.add("""
              |Installed DocumentTemplate lacks the required non-null fields for Document target
              |Missing fields: $missingFields
              |Document target: ${document::class}
              |DocumentTemplate: $documentTemplate
              """.trimMargin())
          }
        }
      }
    }
    if (problems.isNotEmpty()) {
      throw BarberException(problems = problems)
    }
  }

  private fun <DD : DocumentData, D : Document> buildBarber(
    documentDataClass: KClass<out DD>,
    documentClass: KClass<out D>
  ): Barber<DD, D> {
    // Lookup installed DocumentTemplate that corresponds to DocumentData
    val documentTemplates = installedDocumentTemplates.row(documentDataClass)

    if (documentTemplates.isEmpty()) {
      throw BarberException(problems = listOf("""
        |Attempting to build Barber<$documentDataClass, $documentClass>.
        |No installed DocumentTemplates for DocumentData key: $documentDataClass.
        |Check usage of BarbershopBuilder to ensure that all DocumentTemplates are installed to prevent dangling DocumentData.
        """.trimMargin()))
    }

    // Confirm that output Document is a valid target for the DocumentTemplate
    documentTemplates.values.forEach { documentTemplate: DocumentTemplate ->
      if (!documentTemplate.targets.contains(documentClass)) {
        throw BarberException(problems = listOf("""
        |Specified target $documentClass not a valid target for DocumentData's corresponding DocumentTemplate.
        |Valid targets:
        |${documentTemplate.targets}
        """.trimMargin()))
      }
    }

    // Pull out required parameters from Document constructor
    val documentConstructor = documentClass.primaryConstructor!!
    val documentParametersByName = documentConstructor.parameters.associateBy { it.name }

    return RealBarber(
      documentConstructor = documentConstructor,
      documentParametersByName = documentParametersByName,
      compiledDocumentTemplateLocales = compileDocumentTemplates(documentTemplates, documentParametersByName),
      localeResolver = localeResolver)
  }

  private fun compileDocumentTemplates(
    documentTemplates: MutableMap<Locale, DocumentTemplate>,
    documentParametersByName: Map<String?, KParameter>
  ): Map<Locale, CompiledDocumentTemplate> = documentTemplates.mapValues { it: Map.Entry<Locale, DocumentTemplate> ->
    val documentTemplate = it.value

    // Find missing fields in DocumentTemplate
    // Missing fields occur when a nullable field in Document is not an included key in the DocumentTemplate fields
    // In the Parameters Map in the Document constructor though, all parameter keys must be present (including
    // nullable)
    val missingFields = documentParametersByName.filterKeys {
      !it.isNullOrBlank() && !documentTemplate.fields.containsKey(it)
    }

    // Pre-compile Mustache templates
    val documentDataFields: MutableMap<String, Mustache?> =
      documentTemplate.fields.mapValues {
        mustacheFactory.compile(StringReader(it.value), it.value)
      }.toMutableMap()

    // Initialize keys for missing fields in DocumentTemplate
    missingFields.map { documentDataFields.putIfAbsent(it.key!!, null) }

    CompiledDocumentTemplate(
      fields = documentDataFields,
      source = documentTemplate.source,
      targets = documentTemplate.targets,
      locale = documentTemplate.locale)
  }

  private fun buildAllBarbers(): LinkedHashMap<BarberKey, Barber<DocumentData, Document>> {
    val barbers: LinkedHashMap<BarberKey, Barber<DocumentData, Document>> = linkedMapOf()
    installedDocumentTemplates.cellSet().forEach { cell ->
      val documentTemplate = cell.value!!
      documentTemplate.targets.forEach {
        barbers[BarberKey(documentTemplate.source, it)] = buildBarber(documentTemplate.source, it)
      }
    }
    return barbers
  }

  override fun build(): Barbershop {
    validate()
    return RealBarbershop(buildAllBarbers())
  }
}