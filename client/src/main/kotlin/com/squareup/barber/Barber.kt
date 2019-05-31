package com.squareup.barber

import com.squareup.barber.models.DocumentData
import com.squareup.barber.models.DocumentTemplate
import com.squareup.barber.models.Document
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * 1) Keeps the installed [DocumentData], [DocumentTemplate], and [Document] in memory
 * 2) Provides a render method to generate [Document] from [DocumentTemplate] template and [DocumentData] values
 */
interface Barber {
  fun <C : DocumentData, D : Document> newRenderer(
    documentDataClass: KClass<out C>,
    documentClass: KClass<out D>
  ): Renderer<C, D>

  fun getAllRenderers(): LinkedHashMap<RendererKey, Renderer<*, *>>

  class Builder {
    private val installedDocumentData: MutableSet<KClass<out DocumentData>> = mutableSetOf()
    private val installedDocumentTemplate: MutableMap<KClass<out DocumentData>, DocumentTemplate> = mutableMapOf()
    private val installedDocument: MutableSet<KClass<out Document>> = mutableSetOf()

    /**
     * Consumes a [DocumentData] and corresponding [DocumentTemplate] and persists in-memory
     * At boot, a service will call [installDocumentTemplate] on all [DocumentData] and [DocumentTemplate] to add to the in-memory Barber
     */
    fun installDocumentTemplate(documentData: KClass<out DocumentData>, documentTemplate: DocumentTemplate) = apply {
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

    inline fun <reified C : DocumentData> installDocumentTemplate(documentTemplate: DocumentTemplate) = installDocumentTemplate(C::class, documentTemplate)

    /**
     * Consumes a [Document] and persists in-memory
     * At boot, a service will call [installDocument] on all [Document] to add to the in-memory Barber instance
     */
    fun installDocument(document: KClass<out Document>) = apply {
      installedDocument.add(document)
    }

    inline fun <reified D : Document> installDocument() = installDocument(D::class)

    /**
     * Validates Builder inputs and returns a Barber instance with the installed and validated elements
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
              |Missing fields: ${requiredParameterNames.filter{ !documentTemplate.fields.containsKey(it) }}
              |Document target: ${document::class}
              |DocumentTemplate: $documentTemplate
            """.trimMargin()))
          }
        }
      }
    }

    /**
     * Validates Builder inputs and returns a Barber instance with the installed and validated elements
     */
    fun build(): Barber {
      validate()
      return RealBarber(installedDocumentTemplate.toMap())
    }
  }
}

inline fun <reified C : DocumentData, reified D : Document> Barber.newRenderer() = newRenderer(C::class, D::class)
