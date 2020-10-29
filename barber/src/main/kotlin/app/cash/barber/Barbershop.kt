package app.cash.barber

import app.cash.barber.locale.LocaleResolver
import app.cash.barber.models.BarberFieldEncoding
import app.cash.barber.models.BarberKey
import app.cash.barber.models.Document
import app.cash.barber.models.TemplateToken
import app.cash.protos.barber.api.DocumentTemplate
import kotlin.reflect.KClass

/**
 * A registry of templates with their input document data types and their output document types.
 */
interface Barbershop {
  /** Get Barber that can handle static DocumentData Kotlin data class */
  fun <DD : app.cash.barber.models.DocumentData, D : Document> getBarber(
    documentDataClass: KClass<out DD>,
    documentClass: KClass<out D>
  ): Barber<D>

  /** Get barber that can handle dynamic DocumentData proto */
  fun <D : Document> getBarber(
    templateToken: TemplateToken,
    documentClass: KClass<out D>
  ): Barber<D>

  /** Get Documents that a DocumentData Kotlin data class targets */
  fun <DD : app.cash.barber.models.DocumentData> getTargetDocuments(documentDataClass: KClass<out DD>):
      Set<KClass<out Document>>

  /** Get Documents that a templateToken targets */
  fun getTargetDocuments(templateToken: TemplateToken): Set<KClass<out Document>>

  fun getAllBarbers(): Map<BarberKey, Barber<*>>

  fun getWarnings(): List<String>

  interface Builder {
    /**
     * Configures this Barbershop so that instances of documentTemplate.templateToken will
     * rendered by [documentTemplate] for its target locale.
     */
    fun installDocumentTemplate(
      documentTemplate: DocumentTemplate
    ): Builder

    /**
     * Prepares this Barbershop to render instances of [document].
     */
    fun installDocument(document: KClass<out Document>): Builder

    /**
     * Configures this Barbershop to use [LocaleResolver] to map requested locales to available
     * templates. By default Barber does an exact match, and if nothing matches it uses the first
     * installed template.
     */
    fun setLocaleResolver(resolver: LocaleResolver): Builder

    /**
     * Configures this Barbershop to treat warnings as errors during validataion. By default,
     * only errors, not warnings, lead to fatal BarberException during validation.
     */
    fun setWarningsAsErrors(): Builder

    /**
     * Configures this Barbershop to use a given [BarberFieldEncoding] when no annotation to override
     * is present. By default, [BarberFieldEncoding.STRING_HTML] is used.
     */
    fun setDefaultBarberFieldEncoding(encoding: BarberFieldEncoding): Builder

    /**
     * Validates that all templates, document datas, and documents are mutually consistent and
     * returns a new Barbershop.
     */
    fun build(): Barbershop
  }
}

inline fun <reified DD : app.cash.barber.models.DocumentData, reified D : Document> Barbershop.getBarber() = getBarber(
    DD::class, D::class)

inline fun <reified D : Document> Barbershop.getBarber(templateToken: TemplateToken) = getBarber(
    templateToken, D::class)

inline fun <reified DD : app.cash.barber.models.DocumentData> Barbershop.getTargetDocuments() = getTargetDocuments(
    DD::class)
