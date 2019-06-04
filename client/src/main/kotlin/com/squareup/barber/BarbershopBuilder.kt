package com.squareup.barber

import com.squareup.barber.models.Document
import com.squareup.barber.models.DocumentData
import com.squareup.barber.models.DocumentTemplate
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class BarbershopBuilder: Barbershop.Builder {
  private val installedDocumentData: MutableSet<KClass<out DocumentData>> = mutableSetOf()
  private val installedDocumentTemplate: MutableMap<KClass<out DocumentData>, DocumentTemplate> = mutableMapOf()
  private val installedDocument: MutableSet<KClass<out Document>> = mutableSetOf()

  override fun installDocumentTemplate(documentData: KClass<out DocumentData>, documentTemplate: DocumentTemplate) = apply {
    if (installedDocumentTemplate.containsKey(documentData) && installedDocumentTemplate[documentData] != documentTemplate) {
      throw BarberException(problems = listOf("""
        |Attempted to install DocumentTemplate that will overwrite an already installed DocumentTemplate and DocumentData.
        |Already Installed
        |DocumentData: $documentData
        |DocumentTemplate: ${installedDocumentTemplate[documentData]}
        |
        |Attempted to Install
        |$documentTemplate
      """.trimMargin()))
    }
    installedDocumentData.add(documentData)
    installedDocumentTemplate[documentData] = documentTemplate
  }

  inline fun <reified DD : DocumentData> installDocumentTemplate(documentTemplate: DocumentTemplate) = installDocumentTemplate(DD::class, documentTemplate)

  override fun installDocument(document: KClass<out Document>) = apply {
    installedDocument.add(document)
  }

  inline fun <reified D : Document> installDocument() = installDocument(D::class)

  /**
   * Validates BarbershopBuilder inputs and returns a Barbershop instance with the installed and validated elements
   */
  private fun validate() {
    installedDocumentTemplate.forEach { installedDocumentTemplate ->
      val documentDataClass = installedDocumentTemplate.key
      val documentTemplate = installedDocumentTemplate.value

      // DocumentTemplate must be installed with a DocumentData that is listed in its Source
      if (documentDataClass != documentTemplate.source) {
        throw BarberException(problems = listOf("""
            |Attempted to install DocumentTemplate with a DocumentData not specific in the DocumentTemplate source.
            |DocumentTemplate.source: ${documentTemplate.source}
            |DocumentData: $documentDataClass
            """.trimMargin()))
      }

      // Documents listed in DocumentTemplate.Targets must be installed
      val notInstalledDocument = documentTemplate.targets.filter {
        !installedDocument.contains(it)
      }
      if (notInstalledDocument.isNotEmpty()) {
        throw BarberException(problems = listOf("""
            |Attempted to install DocumentTemplate without the corresponding Document being installed.
            |Not installed DocumentTemplate.targets:
            |$notInstalledDocument
            """.trimMargin()))
      }

      // Document targets must have primaryConstructor
      // and installedDocumentTemplate must be able to fulfill Document target parameter requirements
      val documents = documentTemplate.targets
      documents.forEach { document ->
        // Validate that Document has a Primary Constructor
        val documentConstructor = document.primaryConstructor ?: throw BarberException(
          problems = listOf("No primary constructor for Document class ${document::class}."))

        // Determine non-nullable required parameters
        val requiredParameterNames = documentConstructor.parameters.filter {
          !it.type.isMarkedNullable
        }.map { it.name }

        // Confirm that required parameters are present in installedDocumentTemplate
        if (!documentTemplate.fields.keys.containsAll(requiredParameterNames)) {
          throw BarberException(problems = listOf("""
              |Installed DocumentTemplate lacks the required non-null fields for Document target
              |Missing fields: ${requiredParameterNames.filter { !documentTemplate.fields.containsKey(it) }}
              |Document target: ${document::class}
              |DocumentTemplate: $documentTemplate
            """.trimMargin()))
        }
      }
    }
  }

  private fun <C : DocumentData, D : Document> buildBarber(
    documentDataClass: KClass<out C>,
    documentClass: KClass<out D>
  ): Barber<C, D> {
    // Lookup installed DocumentTemplate that corresponds to DocumentData
    val documentTemplate: DocumentTemplate = installedDocumentTemplate[documentDataClass] ?: throw BarberException(
      problems = listOf("""
      |Attempted to render with DocumentTemplate that has not been installed for DocumentData: $documentDataClass.
    """.trimMargin()))

    // Confirm that output Document is a valid target for the DocumentTemplate
    if (!documentTemplate.targets.contains(documentClass)) {
      throw BarberException(problems = listOf("""
        |Specified target $documentClass not a valid target for DocumentData's corresponding DocumentTemplate.
        |Valid targets:
        |${documentTemplate.targets}
      """.trimMargin()))
    }

    // Pull out required parameters from Document constructor
    val documentConstructor = documentClass.primaryConstructor!!
    val documentParametersByName = documentConstructor.parameters.associateBy { it.name }

    // Find missing fields in DocumentTemplate
    // Missing fields occur when a nullable field in Document is not an included key in the DocumentTemplate fields
    // In the Parameters Map in the Document constructor though, all parameter keys must be present (including
    // nullable)
    val missingFields = documentParametersByName.filterKeys {
      !it.isNullOrBlank() && !documentTemplate.fields.containsKey(it)
    }

    // Initialize keys for missing fields in DocumentTemplate
    val documentDataFields: MutableMap<String, String?> = documentTemplate.fields.toMutableMap()
    missingFields.map { documentDataFields.putIfAbsent(it.key!!, null) }

    return RealBarber(documentConstructor, documentParametersByName, documentDataFields)
  }

  private fun buildAllBarbers(): LinkedHashMap<BarberKey, Barber<DocumentData, Document>> {
    val barbers: LinkedHashMap<BarberKey, Barber<DocumentData, Document>> = linkedMapOf()
    for (entry in installedDocumentTemplate) {
      val documentData = entry.value
      documentData.targets.forEach { barbers[BarberKey(documentData.source, it)] = buildBarber(documentData.source, it) }
    }
    return barbers
  }

  override fun build(): Barbershop {
    validate()
    return RealBarbershop(buildAllBarbers())
  }
}