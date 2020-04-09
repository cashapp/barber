package app.cash.barber

import app.cash.barber.models.BarberKey
import app.cash.barber.models.CompiledDocumentTemplate
import app.cash.barber.models.Document
import app.cash.barber.models.DocumentData
import app.cash.barber.models.DocumentTemplate
import app.cash.barber.models.Locale
import com.github.mustachejava.DefaultMustacheFactory
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class BarbershopBuilder : Barbershop.Builder {
  private val installedDocumentTemplates =
    HashBasedTable.create<KClass<out DocumentData>, Locale, CompiledDocumentTemplate>()
  private val installedDocument = mutableSetOf<KClass<out Document>>()
  private val mustacheFactory = DefaultMustacheFactory()
  private var localeResolver: LocaleResolver = MatchOrFirstLocaleResolver
  private var warningsAsErrors: Boolean = false
  private val warnings = mutableListOf<String>()

  override fun installDocumentTemplate(
    documentDataClass: KClass<out DocumentData>,
    documentTemplate: DocumentTemplate
  ) = apply {
    if (installedDocumentTemplates.contains(documentDataClass, documentTemplate.locale)) {
      throw BarberException(errors = listOf("""
        |Attempted to install DocumentTemplate that will overwrite an already installed DocumentTemplate with locale
        |${documentTemplate.locale}.
        |Already Installed
        |DocumentData: $documentDataClass
        |Locales:
        |${installedDocumentTemplates.row(documentDataClass).keys.joinToString("\n")}
        |DocumentTemplates: [
        |${installedDocumentTemplates.row(documentDataClass).values.joinToString("\n")}]
        |
        |Attempted to Install
        |$documentTemplate
        """.trimMargin()))
    }
    installedDocumentTemplates.put(documentDataClass, documentTemplate.locale,
      documentTemplate.compile(mustacheFactory))
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

  override fun setWarningsAsErrors(): Barbershop.Builder = apply {
    warningsAsErrors = true
  }

  override fun build(): Barbershop = installedDocumentTemplates.validate().asBarbershop()

  /**
   * Validates BarbershopBuilder inputs and returns a Barbershop instance with the installed and
   * validated elements.
   */
  private fun Table<KClass<out DocumentData>, Locale, CompiledDocumentTemplate>.validate() = apply {
    val errors: MutableList<String> = mutableListOf()

    // Warn if Barber elements are not installed
    if (cellSet().isEmpty()) {
      warnings.add("""
        |No DocumentData or DocumentTemplates installed
      """.trimMargin())
    }
    if (installedDocument.isEmpty()) {
      warnings.add("""
        |No Documents installed
      """.trimMargin())
    }

    // Warn if Documents are unused in DocumentTemplates
    if (installedDocument.isNotEmpty() && cellSet().isNotEmpty()) {
      val usedDocuments = cellSet()
        .map { it.value!!.targets }
        .reduce { acc, targets ->
          acc + targets
        }.toSet()
      if (!usedDocuments.containsAll(installedDocument)) {
        val danglingDocuments = installedDocument.filter { document ->
          !usedDocuments.contains(document)
        }
        warnings.add("""
          |Document installed that is not used in any installed DocumentTemplates
          |$danglingDocuments
          |
          """.trimMargin())
      }
    }

    // Throwing early makes debugging simpler for Barber developers as the above simple warnings
    // can be raised before a flood of other errors below fail as a result of the above
    throwBarberException(errors = errors, warnings = warnings)

    // Standard validation
    cellSet().forEach { cell ->
      val documentDataClass = cell.rowKey!!
      val compiledDocumentTemplate = cell.value!!

      // DocumentTemplate must be installed with a DocumentData that is listed in its Source
      if (documentDataClass != compiledDocumentTemplate.source) {
        errors.add("""
          |Attempted to install DocumentTemplate with a DocumentData not specified in the DocumentTemplate source.
          |DocumentTemplate.source: ${compiledDocumentTemplate.source}
          |DocumentData: $documentDataClass
          """.trimMargin())
      }

      // Documents listed in DocumentTemplate.Targets must be installed
      val notInstalledDocument = compiledDocumentTemplate.targets.filter {
        !installedDocument.contains(it)
      }
      if (notInstalledDocument.isNotEmpty()) {
        errors.add("""
          |Attempted to install DocumentTemplate without the corresponding Document being installed.
          |Not installed DocumentTemplate.targets:
          |$notInstalledDocument
          """.trimMargin())
      }

      // DocumentTemplates must only use variables from source DocumentData in their fields
      val documentDataConstructor = documentDataClass.primaryConstructor!!
      val documentDataParameterNames = documentDataConstructor.asParameterNames()
      compiledDocumentTemplate.fields.asFieldCodesMap().forEach { (name, codes) ->
        // Check for missing variables in field templates
        codes.forEach { code ->
          if (!documentDataParameterNames.contains(code.rootKey())) {
            errors.add(
              "Missing variable [$code] in DocumentData [$documentDataClass] for DocumentTemplate field [${compiledDocumentTemplate.fields[name].asString()}]")
          }
        }
      }

      // Document targets must have primaryConstructor
      // and installedDocumentTemplates must be able to fulfill Document target parameter requirements
      val allTargetParameters = compiledDocumentTemplate.targets.map { documentClass ->
        // Validate that Document has a Primary Constructor
        val documentConstructor = documentClass.primaryConstructor
        if (documentConstructor == null) {
          errors.add("No primary constructor for Document [$documentClass]")
          listOf()
        } else if (documentConstructor.parameters.isEmpty()) {
          errors.add("No fields included for Document [$documentClass]")
          listOf()
        } else {
          documentConstructor.parameters
        }
      }.reduce { acc, params ->
        acc + params
      }.toSet()
      val allTargetFields = allTargetParameters.map {
        it.name
      }
      val requiredTargetFields = allTargetParameters.filter {
        !it.type.isMarkedNullable
      }.map { it.name }

      // Confirm that required field keys are present in installedDocumentTemplates
      if (!compiledDocumentTemplate.fields.keys.containsAll(requiredTargetFields)) {
        val missingFields = requiredTargetFields.filter {
          !compiledDocumentTemplate.fields.containsKey(it)
        }
        val documentsThatRequireMissingField =
          compiledDocumentTemplate.targets.map { documentClass ->
            documentClass to documentClass.primaryConstructor!!.parameters.map { it.name }.filter {
              missingFields.contains(it)
            }
          }.toMap().map { "[${it.key}] requires missing fields ${it.value}" }.joinToString("\n")

        errors.add("""
              |Installed DocumentTemplate missing required fields for Document targets
              |Missing fields:
              |$documentsThatRequireMissingField
              |
              |DocumentTemplate: ${compiledDocumentTemplate.toDocumentTemplate()}
              """.trimMargin())
      }
      if (compiledDocumentTemplate.fields.keys.size > allTargetFields.size) {
        val additionalFields = compiledDocumentTemplate.fields.keys.filter { field ->
          !allTargetFields.contains(field)
        }
        errors.add("""
              |Installed DocumentTemplate has additional fields that are not used in any target Document
              |Additional fields:
              |${additionalFields.joinToString("\n")}
            """.trimMargin())
      }

      compiledDocumentTemplate.targets.forEach { documentClass ->
        // Lookup installed DocumentTemplates that corresponds to DocumentData
        val documentTemplates = row(documentDataClass)

        if (documentTemplates.isEmpty()) {
          errors.add("""
            |Attempting to build Barber<$documentDataClass, $documentClass>.
            |No installed DocumentTemplates for DocumentData key: $documentDataClass.
            |Check usage of BarbershopBuilder to ensure that all DocumentTemplates are installed to prevent dangling DocumentData.
            """.trimMargin()
          )
        }

        // Confirm that output Document is a valid target for the DocumentTemplate
        documentTemplates.forEach { (_, documentTemplate) ->
          if (!documentTemplate.targets.contains(documentClass)) {
            errors.add("""
              |Specified target $documentClass not a valid target for DocumentData's corresponding DocumentTemplate.
              |Valid targets:
              |${documentTemplate.targets}
              """.trimMargin()
            )
          }
        }
      }
    }

    // Check for unused DocumentData variable not used in any installed DocumentTemplate field
    rowMap().forEach { (documentDataClass, documentTemplates) ->
      val codes = documentTemplates.reducedFieldCodeSet()

      val documentDataConstructor = documentDataClass.primaryConstructor
      if (documentDataConstructor == null) {
        errors.add("Null primary constructor for DocumentData $documentDataClass")
      } else {
        val documentDataParameterNames = documentDataConstructor.parameters.map { it.name }.toList()
        documentDataParameterNames.forEach { parameter ->
          if (!codes.map { it.rootKey() }.contains(parameter)) {
            warnings.add("""
                |Unused DocumentData variable [$parameter] in [$documentDataClass] with no usage in installed DocumentTemplate Locales:
                |${documentTemplates.keys.joinToString("\n")}
              """.trimMargin())
          }
        }
      }
    }

    throwBarberException(errors = errors, warnings = warnings)
  }

  private fun throwBarberException(errors: List<String>, warnings: List<String>) {
    if (errors.isNotEmpty() || (warnings.isNotEmpty() && warningsAsErrors)) {
      throw BarberException(errors = errors, warnings = warnings)
    }
  }

  private fun Table<KClass<out DocumentData>, Locale, CompiledDocumentTemplate>.asBarbershop(): Barbershop {
    val barbers: LinkedHashMap<BarberKey, Barber<DocumentData, Document>> = linkedMapOf()
    cellSet().forEach { cell ->
      val documentDataClass = cell.rowKey!!
      val documentTemplate = cell.value!!
      documentTemplate.targets.forEach { documentClass ->
        val documentTemplatesBySource = row(documentTemplate.source)
        barbers[BarberKey(documentDataClass, documentClass)] = RealBarber(
          documentConstructor = documentClass.primaryConstructor!!,
          compiledDocumentTemplateLocales = documentTemplatesBySource.mapValues { it.value },
          localeResolver = localeResolver)
      }
    }
    return RealBarbershop(barbers = barbers, warnings = warnings)
  }
}
