package com.squareup.barber

import com.squareup.barber.models.DocumentData
import com.squareup.barber.models.DocumentTemplate
import com.squareup.barber.models.Document
import kotlin.reflect.KClass

/**
 * Holds validated elements that have eagerly built Barbers between type [DocumentData] and [Document]
 */
interface Barbershop {
  fun <DD : DocumentData, D : Document> getBarber(
    documentDataClass: KClass<out DD>,
    documentClass: KClass<out D>
  ): Barber<DD, D>

  fun getAllBarbers(): LinkedHashMap<BarberKey, Barber<*, *>>

  interface Builder {
    /**
     * Consumes a [DocumentData] and corresponding [DocumentTemplate] and persists in-memory
     * At boot, a service will call [installDocumentTemplate] on all [DocumentData] and [DocumentTemplate] to add to the in-memory Barbershop
     */
    fun installDocumentTemplate(documentData: KClass<out DocumentData>, documentTemplate: DocumentTemplate): Builder

    /**
     * Consumes a [Document] and persists in-memory
     * At boot, a service will call [installDocument] on all [Document] to add to the in-memory Barbershop instance
     */
    fun installDocument(document: KClass<out Document>): Builder

    /**
     * Validates BarbershopBuilder inputs and returns a Barbershop instance with the installed and validated elements
     */
    fun build(): Barbershop
  }
}

inline fun <reified DD : DocumentData, reified D : Document> Barbershop.getBarber() = getBarber(DD::class, D::class)
