package app.cash.barber.models

import app.cash.barber.BarberMustacheFactoryProvider
import com.github.mustachejava.Mustache
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import java.io.StringReader
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

/**
 * For each DocumentData we have a DocumentTemplate that provides a natural language for the document.
 * It uses Mustache templates to provide openings for the DocumentData fields.
 *
 * Each DocumentTemplate is specific to a locale.
 *
 * @param [fields] Map of a Document output key to a template String value that can contain DocumentData input values
 * @param [source] KClass of DocumentData
 * @param [targets] Set of Documents that DocumentTemplate can render to
 * @param [locale] Barbershop.Locale that scopes DocumentTemplate to a languages/country Locale
 */
data class DocumentTemplate(
  val fields: Map<String, String>,
  val source: KClass<out DocumentData>,
  val targets: Set<KClass<out Document>>,
  val locale: Locale
) {
  override fun toString(): String = """
    |DocumentTemplate(
    | fields = mapOf(
    |   ${fields.map { "$it" }.joinToString("\n")}
    | ),
    | source = $source,
    | targets = $targets,
    | locale = $locale
    |)
  """.trimMargin()

  fun compile(
    mustacheFactoryProvider: BarberMustacheFactoryProvider,
    installedDocuments: Table<String, KClass<out Document>, KParameter>
  ): CompiledDocumentTemplate {
    // Pre-compile Mustache templates
    val documentTemplateFields =
        HashBasedTable.create<String, KClass<out Document>, Mustache?>()
    fields.mapValues { (fieldName, fieldValue) ->
      installedDocuments.row(fieldName).keys.forEach { document ->
        // Render using a MustacheFactory that will respect any field BarberFieldEncoding annotations
        val barberField = installedDocuments.get(fieldName, document)
            .annotations
            .firstOrNull { it is BarberField } as BarberField?
        val mustache = mustacheFactoryProvider.get(barberField?.encoding)
            .compile(StringReader(fieldValue), fieldValue)
        documentTemplateFields.put(fieldName, document, mustache)
      }
    }

    return CompiledDocumentTemplate(
        fields = documentTemplateFields,
        source = source,
        targets = targets,
        locale = locale
    )
  }
}
