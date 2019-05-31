package com.squareup.barber

import com.squareup.barber.models.CopyModel
import com.squareup.barber.models.DocumentCopy
import com.squareup.barber.models.DocumentSpec
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * 1) Keeps the installed [CopyModel], [DocumentCopy], and [DocumentSpec] in memory
 * 2) Provides a render method to generate [DocumentSpec] from [DocumentCopy] template and [CopyModel] values
 */
interface Barber {
  fun <C : CopyModel, D : DocumentSpec> newRenderer(
    copyModelClass: KClass<out C>,
    documentSpecClass: KClass<out D>
  ): Renderer<C, D>

  fun getAllRenderers(): LinkedHashMap<RendererKey, Renderer<*, *>>

  class Builder {
    private val installedCopyModel: MutableSet<KClass<out CopyModel>> = mutableSetOf()
    private val installedDocumentCopy: MutableMap<KClass<out CopyModel>, DocumentCopy> = mutableMapOf()
    private val installedDocumentSpec: MutableSet<KClass<out DocumentSpec>> = mutableSetOf()

    /**
     * Consumes a [CopyModel] and corresponding [DocumentCopy] and persists in-memory
     * At boot, a service will call [installCopy] on all [CopyModel] and [DocumentCopy] to add to the in-memory Barber
     */
    fun installCopy(copyModel: KClass<out CopyModel>, documentCopy: DocumentCopy) = apply {
      if (installedDocumentCopy.containsKey(copyModel) && installedDocumentCopy[copyModel] != documentCopy) {
        throw BarberException(problems = listOf("""
        |Attempted to install DocumentCopy that will overwrite an already installed DocumentCopy and CopyModel.
        |Already Installed
        |CopyModel: $copyModel
        |DocumentCopy: ${installedDocumentCopy[copyModel]}
        |
        |Attempted to Install
        |$documentCopy
      """.trimMargin()))
      }
      installedCopyModel.add(copyModel)
      installedDocumentCopy[copyModel] = documentCopy
    }

    inline fun <reified C : CopyModel> installCopy(documentCopy: DocumentCopy) = installCopy(C::class, documentCopy)

    /**
     * Consumes a [DocumentSpec] and persists in-memory
     * At boot, a service will call [installDocumentSpec] on all [DocumentSpec] to add to the in-memory Barber instance
     */
    fun installDocumentSpec(documentSpec: KClass<out DocumentSpec>) = apply {
      installedDocumentSpec.add(documentSpec)
    }

    inline fun <reified D : DocumentSpec> installDocumentSpec() = installDocumentSpec(D::class)

    /**
     * Validates Builder inputs and returns a Barber instance with the installed and validated elements
     */
    private fun validate() {
      installedDocumentCopy.forEach { installedCopy ->
        val copyModelClass = installedCopy.key
        val documentCopy = installedCopy.value

        // DocumentCopy must be installed with a CopyModel that is listed in its Source
        if (copyModelClass != documentCopy.source) {
          throw BarberException(problems = listOf("""
            |Attempted to install DocumentCopy with a CopyModel not specific in the DocumentCopy source.
            |DocumentCopy.source: ${documentCopy.source}
            |CopyModel: $copyModelClass
            """.trimMargin()))
        }

        // DocumentSpecs listed in DocumentCopy.Targets must be installed
        val notInstalledDocumentSpec = documentCopy.targets.filter {
          !installedDocumentSpec.contains(it)
        }
        if (notInstalledDocumentSpec.isNotEmpty()) {
          throw BarberException(problems = listOf("""
            |Attempted to install DocumentCopy without the corresponding DocumentSpec being installed.
            |Not installed DocumentCopy.targets:
            |$notInstalledDocumentSpec
            """.trimMargin()))
        }

        // DocumentSpec targets must have primaryConstructor
        // and installedCopy must be able to fulfill DocumentSpec target parameter requirements
        val documentSpecs = documentCopy.targets
        documentSpecs.forEach { documentSpec ->
          // Validate that DocumentSpec has a Primary Constructor
          val documentSpecConstructor = documentSpec.primaryConstructor ?: throw BarberException(
            problems = listOf("No primary constructor for DocumentSpec class ${documentSpec::class}."))

          // Determine non-nullable required parameters
          val requiredParameterNames = documentSpecConstructor.parameters.filter {
            !it.type.isMarkedNullable
          }.map { it.name }

          // Confirm that required parameters are present in installedCopy
          if (!documentCopy.fields.keys.containsAll(requiredParameterNames)) {
            throw BarberException(problems = listOf("""
              |Installed DocumentCopy lacks the required non-null fields for DocumentSpec target
              |Missing fields: ${requiredParameterNames.filter{ !documentCopy.fields.containsKey(it) }}
              |DocumentSpec target: ${documentSpec::class}
              |DocumentCopy: $documentCopy
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
      return RealBarber(installedDocumentCopy.toMap())
    }
  }
}

inline fun <reified C : CopyModel, reified D : DocumentSpec> Barber.newRenderer() = newRenderer(C::class, D::class)
