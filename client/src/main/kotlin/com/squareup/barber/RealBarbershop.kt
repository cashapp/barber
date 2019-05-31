package com.squareup.barber

import com.squareup.barber.models.DocumentData
import com.squareup.barber.models.DocumentTemplate
import com.squareup.barber.models.Document
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

internal class RealBarbershop(val installedDocumentTemplate: Map<KClass<out DocumentData>, DocumentTemplate>) : Barbershop {
  override fun <C : DocumentData, D : Document> getBarber(
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

  override fun getAllBarbers(): LinkedHashMap<BarberKey, Barber<*, *>> {
    val barbers: LinkedHashMap<BarberKey, Barber<*,*>> = linkedMapOf()
    for (entry in installedDocumentTemplate) {
      val documentData = entry.value
      documentData.targets.forEach { barbers[BarberKey(documentData.source, it)] = getBarber(documentData.source, it) }
    }
    return barbers
  }
}
