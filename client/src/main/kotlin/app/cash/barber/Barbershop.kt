package app.cash.barber

import app.cash.barber.models.BarberKey
import app.cash.barber.models.Document
import app.cash.barber.models.DocumentData
import app.cash.barber.models.DocumentTemplate
import kotlin.reflect.KClass

/**
 * Holds validated elements that have eagerly built Barbers between type [DocumentData] and [Document]
 */
interface Barbershop {
  fun <DD : DocumentData, D : Document> getBarber(
    documentDataClass: KClass<out DD>,
    documentClass: KClass<out D>
  ): Barber<DD, D>

  fun getAllBarbers(): Map<BarberKey, Barber<DocumentData, Document>>

  interface Builder {
    /**
     * Consumes a [DocumentData] and corresponding [DocumentTemplate] and persists in-memory
     * At boot, a service will call [installDocumentTemplate] on all [DocumentData] and [DocumentTemplate] to add to the in-memory Barbershop
     */
    fun installDocumentTemplate(
      documentDataClass: KClass<out DocumentData>,
      documentTemplate: DocumentTemplate
    ): Builder

    /**
     * Consumes a [Document] and persists in-memory
     * At boot, a service will call [installDocument] on all [Document] to add to the in-memory Barbershop instance
     */
    fun installDocument(document: KClass<out Document>): Builder

    /**
     * Set a [LocaleResolver] to be used when resolving a localized [DocumentTemplate].
     * Default: [MatchOrFirstLocaleResolver].
     */
    fun setLocaleResolver(resolver: LocaleResolver): Builder

    /**
     * Validates BarbershopBuilder inputs and returns a Barbershop instance with the installed and validated elements
     */
    fun build(): Barbershop
  }
}

inline fun <reified DD : DocumentData, reified D : Document> Barbershop.getBarber() = getBarber(
    DD::class, D::class)
