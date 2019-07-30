package app.cash.barber

import app.cash.barber.models.BarberKey
import app.cash.barber.models.Document
import app.cash.barber.models.DocumentData
import app.cash.barber.models.DocumentTemplate
import kotlin.reflect.KClass

/**
 * A registry of templates with their input document data types and their output document types.
 */
interface Barbershop {
  fun <DD : DocumentData, D : Document> getBarber(
    documentDataClass: KClass<out DD>,
    documentClass: KClass<out D>
  ): Barber<DD, D>

  fun getAllBarbers(): Map<BarberKey, Barber<*, *>>

  interface Builder {
    /**
     * Configures this barbershop so that instances of [documentDataClass] will rendered by
     * [documentTemplate] for its target locale.
     */
    fun installDocumentTemplate(
      documentDataClass: KClass<out DocumentData>,
      documentTemplate: DocumentTemplate
    ): Builder

    /**
     * Prepares this barbershop to render instances of [document].
     */
    fun installDocument(document: KClass<out Document>): Builder

    /**
     * Configures this barbershop to use [LocaleResolver] to map requested locales to available
     * templates. By default Barber does an exact match, and if nothing matches it uses the first
     * installed template.
     */
    fun setLocaleResolver(resolver: LocaleResolver): Builder

    /**
     * Validates that all templates, document datas, and documents are mutually consistent and
     * returns a new Barbershop.
     */
    fun build(): Barbershop
  }
}

inline fun <reified DD : DocumentData, reified D : Document> Barbershop.getBarber() = getBarber(
  DD::class, D::class)
