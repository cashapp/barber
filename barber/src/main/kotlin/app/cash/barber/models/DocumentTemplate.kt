package app.cash.barber.models

import app.cash.barber.models.BarberSignature.Companion.getBarberSignature
import app.cash.barber.models.TemplateToken.Companion.getTemplateToken
import kotlin.reflect.KClass

/**
 * For each DocumentData we have a DocumentTemplate that provides a natural language for the document.
 * It uses Mustache templates to provide openings for the DocumentData fields.
 *
 * Each DocumentTemplate is specific to a locale.
 *
 * @property [fields] Map of a Document output key to a template String value that can contain DocumentData input values
 * @property [source] KClass of DocumentData
 * @property [targets] Set of Documents that DocumentTemplate can render to
 * @property [locale] Barber Locale that scopes DocumentTemplate to a languages/country Locale
 * @property [version] Increment for newer versions
 */
data class DocumentTemplate(
  val fields: Map<String, String>,
  val source: KClass<out DocumentData>,
  val targets: Set<KClass<out Document>>,
  val locale: Locale,
  val version: Long = 1
) {
  override fun toString(): String = """
    |DocumentTemplate(
    | fields = mapOf(
    |   ${fields.map { "$it" }.joinToString("\n")}
    | ),
    | source = ${source.qualifiedName},
    | targets = ${targets.map { it.qualifiedName }},
    | locale = $locale
    |)
  """.trimMargin()

  fun toProto() = app.cash.protos.barber.api.DocumentTemplate(
      template_token = source.getTemplateToken().token,
      version = version,
      locale = locale.locale,
      source_signature = source.getBarberSignature().signature,
      target_signatures = targets.map { it.getBarberSignature().signature },
      fields = fields.entries.map { (k, v) -> app.cash.protos.barber.api.DocumentTemplate.Field(k, v) },
  )
}
