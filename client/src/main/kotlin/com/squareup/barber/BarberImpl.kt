package com.squareup.barber

import com.squareup.barber.models.CopyModel
import com.squareup.barber.models.DocumentCopy
import com.squareup.barber.models.DocumentSpec
import java.lang.reflect.Constructor
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class BarberImpl : Barber {
  private val installedCopyModel: MutableSet<KClass<out CopyModel>> = mutableSetOf()
  private val installedDocumentCopy: MutableMap<KClass<out CopyModel>, DocumentCopy> = mutableMapOf()
  private val installedDocumentSpec: MutableSet<KClass<out DocumentSpec>> = mutableSetOf()

  override fun render(copyModel: CopyModel, documentSpecClass: KClass<out DocumentSpec>): DocumentSpec {
    // Lookup installed DocumentCopy that corresponds to CopyModel
    val copyModelClass = copyModel::class
    val documentCopy = installedDocumentCopy[copyModelClass] ?: throw BarberException(problems = listOf("""
      |Attempted to render with DocumentCopy that has not been installed for CopyModel: $copyModelClass.
    """.trimMargin()))

    // Confirm that output DocumentSpec is a valid target for the DocumentCopy
    if (!documentCopy.targets.contains(documentSpecClass)) {
      throw BarberException(problems = listOf("""
        |Specified target $documentSpecClass not a valid target for CopyModel's corresponding DocumentCopy.
        |Valid targets:
        |${documentCopy.targets}
      """.trimMargin()))
    }

    // TODO move the "No primary constructor" exception to validate on install instead of here
    // Validate that DocumentSpec has a Primary Constructor
    val documentSpecConstructor= documentSpecClass.primaryConstructor ?: throw BarberException(
      problems = listOf("No primary constructor for DocumentSpec class $documentSpecClass."))

    // Pull out required parameters from DocumentSpec constructor
    val documentSpecParametersByName =  documentSpecConstructor.parameters.associateBy { it.name }

    // Find missing fields in DocumentCopy
    // Missing fields occur when a nullable field in DocumentSpec is not an included key in the DocumentCopy fields
    // In the Parameters Map in the DocumentSpec constructor though, all parameter keys must be present (including
    // nullable)
    val missingFields = documentSpecParametersByName.filterKeys {
      !it.isNullOrBlank() && !documentCopy.fields.containsKey(it)
    }

    // Initialize keys for missing fields in DocumentCopy
    val documentCopyFields: MutableMap<String, String?> = documentCopy.fields.toMutableMap()
    missingFields.map { documentCopyFields.putIfAbsent(it.key!!, null) }

    // Render each field of DocumentCopy with passed in CopyModel context
    // Some of these fields now will be null since any missing fields will have been added with null values
    val renderedDocumentCopyFields = documentCopyFields.mapValues {
      when (it.value) {
        null -> it.value
        else -> BarberRender().render(it.value!!, copyModel)
      }
    }

    // Zips the KParameters with corresponding rendered values from DocumentCopy
    val parameters = renderedDocumentCopyFields.filter {
      documentSpecParametersByName .containsKey(it.key)
    }.mapKeys {
      documentSpecParametersByName [it.key] ?: throw BarberException(problems = listOf("Missing KParameter for ${it.key}"))
    }

    // Build the DocumentSpec instance with the rendered DocumentCopy parameters
    return documentSpecConstructor.callBy(parameters)
  }

  override fun installCopy(copyModel: KClass<out CopyModel>, documentCopy: DocumentCopy) {
    if (documentCopy.source != copyModel) {
      throw BarberException(problems = listOf("""
        |Attempted to install DocumentCopy with a CopyModel not specific in the DocumentCopy source.
        |DocumentCopy.source: ${documentCopy.source}
        |CopyModel: $copyModel
        """.trimMargin()))
    }
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
    if (installedDocumentCopy.containsKey(copyModel) && installedDocumentCopy[copyModel] != documentCopy) {
      throw BarberException(problems = listOf("""
        |Attempted to install DocumentCopy that matches an already installed CopyModel.
        |Already Installed
        |CopyModel: $copyModel
        |DocumentCopy: $installedDocumentCopy[copyModel]
        |
        |Attempted to Install
        |$documentCopy
      """.trimMargin()))
    }
    installedCopyModel.add(copyModel)
    installedDocumentCopy[copyModel] = documentCopy
  }

  override fun installDocumentSpec(documentSpec: KClass<out DocumentSpec>) {
    installedDocumentSpec.add(documentSpec)
  }
}
