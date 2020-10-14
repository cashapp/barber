package app.cash.barber

import app.cash.barber.models.BarberFieldEncoding
import app.cash.barber.models.BarberKey
import app.cash.barber.models.BarberSignature
import app.cash.barber.models.BarberSignature.Companion.getBarberSignature
import app.cash.barber.models.CompiledDocumentTemplate
import app.cash.barber.models.CompiledDocumentTemplate.Companion.compileAndValidate
import app.cash.barber.models.CompiledDocumentTemplate.Companion.getKey
import app.cash.barber.models.Document
import app.cash.barber.models.Locale
import app.cash.barber.models.TemplateToken
import app.cash.protos.barber.api.DocumentTemplate
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class BarbershopBuilder : Barbershop.Builder {
  /**
   * In memory store for various forms of a given DocumentTemplate
   * @property documentTemplate Set on install
   * @property compiledDocumentTemplate Compiled when Barbershop is built so is set as null on install
   */
  internal data class DocumentTemplateDb(
    val documentTemplate: DocumentTemplate,
    val compiledDocumentTemplate: CompiledDocumentTemplate?
  )

  internal data class DocumentTemplateKey(
    val templateToken: TemplateToken,
    val locale: Locale,
    val sourceBarberSignature: BarberSignature,
    val version: Long
  )

  private val installedDocumentTemplates: MutableMap<DocumentTemplateKey, DocumentTemplateDb> =
      mutableMapOf()

  internal data class DocumentDb(
    val kParameter: KParameter,
    val document: KClass<out Document>
  )

  private val installedDocuments =
      HashBasedTable.create<BarberSignature, String, DocumentDb>()

  private var mustacheFactoryProvider = BarberMustacheFactoryProvider()
  private var localeResolver: LocaleResolver = MatchOrFirstLocaleResolver
  private var warningsAsErrors: Boolean = false
  private val warnings = mutableListOf<String>()

  override fun installDocumentTemplate(
    documentDataClass: KClass<out app.cash.barber.models.DocumentData>,
    documentTemplate: app.cash.barber.models.DocumentTemplate
  ) = apply {
    if (documentDataClass != documentTemplate.source) {
      throw BarberException(errors = listOf("""
        |Attempted to install DocumentTemplate with a DocumentData not specified in the DocumentTemplate source
        |DocumentTemplate.source: ${documentTemplate.source.qualifiedName}
        |DocumentData: ${documentDataClass.qualifiedName}
        """.trimMargin()))
    }
    installDocumentTemplate(documentTemplate.toProto())
  }

  override fun installDocumentTemplate(documentTemplate: DocumentTemplate): Barbershop.Builder = apply {
    val templateToken = TemplateToken(documentTemplate.template_token!!)
    val signature = BarberSignature(documentTemplate.source_signature!!)
    val version = documentTemplate.version!!
    val locale = Locale(documentTemplate.locale!!)

    val versions = installedDocumentTemplates.filter { (it,_) -> templateToken == it.templateToken && locale == it.locale }
    if (versions.isNotEmpty()) {
      val alreadyInstalledVersion = versions
          .filter { (it, _) -> signature == it.sourceBarberSignature && version == it.version }
          .entries
          .firstOrNull()
      if (alreadyInstalledVersion != null) {
        val localeVersions = installedDocumentTemplates.filter { (it,_) -> templateToken == it.templateToken }
        val installedLocales = localeVersions.keys.map { it.locale }.joinToString("\n")
        val installedVersions = localeVersions.keys.map { it.version }
        throw BarberException(errors = listOf("""
            |Attempted to install DocumentTemplate that will overwrite an already installed locale version
            |${documentTemplate.getKey()}
            |
            |Already Installed DocumentTemplates
            |TemplateToken: $templateToken
            |Locales: $installedLocales
            |Installed Versions: $installedVersions
            """.trimMargin()))
      }
    }
    installedDocumentTemplates[DocumentTemplateKey(
        templateToken = templateToken,
        locale = locale,
        sourceBarberSignature = signature,
        version = version
    )] = DocumentTemplateDb(documentTemplate = documentTemplate, compiledDocumentTemplate = null)
  }

  inline fun <reified DD : app.cash.barber.models.DocumentData> installDocumentTemplate(
    documentTemplate: app.cash.barber.models.DocumentTemplate
  ) =
      installDocumentTemplate(DD::class, documentTemplate)

  override fun installDocument(document: KClass<out Document>) = apply {
    val documentConstructor = document.primaryConstructor
    if (documentConstructor == null) {
      throw BarberException(errors = listOf("No primary constructor for Document [$document]"))
    } else if (documentConstructor.parameters.isEmpty()) {
      throw BarberException(errors = listOf("No fields included for Document [$document]"))
    }
    documentConstructor.asParameterNames().forEach { (fieldName, kParameter) ->
      fieldName?.let {
        installedDocuments.put(document.getBarberSignature(), fieldName,
            DocumentDb(kParameter, document))
      }
    }
  }

  inline fun <reified D : Document> installDocument() = installDocument(D::class)

  override fun setLocaleResolver(resolver: LocaleResolver): Barbershop.Builder = apply {
    localeResolver = resolver
  }

  override fun setWarningsAsErrors(): Barbershop.Builder = apply {
    warningsAsErrors = true
  }

  override fun setDefaultBarberFieldEncoding(encoding: BarberFieldEncoding): Barbershop.Builder = apply {
    mustacheFactoryProvider = BarberMustacheFactoryProvider(encoding)
  }

  override fun build(): Barbershop = installedDocumentTemplates.validateAndCompile().asBarbershop()

  /**
   * Validates BarbershopBuilder inputs and returns a Barbershop instance with the installed and
   * validated elements.
   */
  private fun Map<DocumentTemplateKey, DocumentTemplateDb>.validateAndCompile(): Map<DocumentTemplateKey, DocumentTemplateDb> {
    val errors: MutableList<String> = mutableListOf()

    // Warn if Barber elements are not installed
    if (isEmpty()) {
      warnings.add("""
        |No DocumentData or DocumentTemplates installed
      """.trimMargin())
    }
    val installedDocumentsIsEmpty = installedDocuments.cellSet().isEmpty()
    if (installedDocumentsIsEmpty) {
      warnings.add("""
        |No Documents installed
      """.trimMargin())
    }

    // Warn if Documents are unused in DocumentTemplates
    if (!installedDocumentsIsEmpty && isNotEmpty()) {
      val usedDocumentSignatures = map { (_, db) ->
        db.documentTemplate.target_signatures
      }.reduce { acc, list -> acc + list }.toSet()
      val installedDocumentSignatures = installedDocuments.rowKeySet().map { it.signature }.toSet()
      if (!usedDocumentSignatures.containsAll(installedDocumentSignatures)) {
        val danglingDocumentSignatures = installedDocumentSignatures.filterNot { signature ->
          usedDocumentSignatures.contains(signature)
        }.toSet()
        val danglingDocuments = danglingDocumentSignatures.map { signature ->
          installedDocuments.row(BarberSignature(signature)).values.fold(
              setOf<String?>()) { acc, documentDB ->
            acc + documentDB.document.qualifiedName
          }
        }.reduce { acc, set -> acc + set }.toSet()
        warnings.add("""
          |Document installed that is not used in any installed DocumentTemplates
          |$danglingDocuments
          """.trimMargin())
      }
    }

    BarberException.maybeThrowBarberException(errors = errors, warnings = warnings,
        warningsAsErrors = warningsAsErrors)

    // Compile DocumentTemplates and perform initial validation
    forEach { (key, db) ->
      // Compile templates according to the MustacheFactory matching to the Document field encoding
      val compiledDocumentTemplate = db.documentTemplate
          .compileAndValidate(
              mustacheFactoryProvider = mustacheFactoryProvider,
              installedDocuments = installedDocuments,
              warnings = warnings,
              warningsAsErrors = warningsAsErrors
          )

      installedDocumentTemplates[key] = db.copy(compiledDocumentTemplate = compiledDocumentTemplate)
    }

    BarberException.maybeThrowBarberException(errors = errors, warnings = warnings,
        warningsAsErrors = warningsAsErrors)

    return installedDocumentTemplates
  }

  private fun Map<DocumentTemplateKey, DocumentTemplateDb>.asBarbershop(): Barbershop {
    val barbers = linkedMapOf<BarberKey, Barber<Document>>()
    keys.map { it.templateToken }.toSet().map { templateToken ->
      val versions = filter { (it, _) -> templateToken == it.templateToken }
      val documentTargets = versions.values
          .map { it.compiledDocumentTemplate?.targets ?: setOf() }
          .reduce { acc, set -> acc + set }
          .toSet()
      val localeVersionsMap = versions.entries.fold(mapOf<Locale, Table<BarberSignature, Long, DocumentTemplateDb>>()) { acc, (key, db) ->
        val (_, locale, sourceBarberSignature, version) = key
        val localeVersions = acc[locale] ?: HashBasedTable.create()
          localeVersions.put(sourceBarberSignature, version, db)
        acc + mapOf(locale to localeVersions)
      }
      documentTargets.forEach { document ->
        barbers[BarberKey(templateToken, document)] = RealBarber(
            document = document,
            installedDocuments = installedDocuments,
            compiledDocumentTemplateLocaleVersions = localeVersionsMap,
            localeResolver = localeResolver
        )
      }
    }
    return RealBarbershop(barbers = barbers, warnings = warnings)
  }
}
