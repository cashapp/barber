package app.cash.barber

import app.cash.barber.models.BarberKey
import app.cash.barber.models.Document
import app.cash.barber.models.TemplateToken
import app.cash.barber.models.TemplateToken.Companion.getTemplateToken
import app.cash.barber.version.VersionRange.Companion.supports
import app.cash.protos.barber.api.DocumentData
import app.cash.protos.barber.api.DocumentTemplate
import kotlin.reflect.KClass

internal class RealBarbershop(
  private val barbers: Map<BarberKey, Barber<Document>>,
  private val warnings: List<String>,
  /**
   * The newest version of the TemplateToken, used to support Barbershop.getTargetDocuments when
   * an explicit version is not specified. It is the latest version across all Barbers and at times
   * may not even be supported by the Barber in the case of a newer version of a Document Template
   * removing the Barber's Document target.
   */
  private val templateLatestVersions: Map<TemplateToken, Long>,
  private val documentTemplates: Set<DocumentTemplate>
) : Barbershop {
  @Suppress("UNCHECKED_CAST")
  override fun <DD : app.cash.barber.models.DocumentData, D : Document> getBarber(
    documentDataClass: KClass<out DD>,
    documentClass: KClass<out D>
  ): Barber<D> = getBarber(documentDataClass.getTemplateToken(), documentClass)

  @Suppress("UNCHECKED_CAST")
  override fun <D : Document> getBarber(
    templateToken: TemplateToken,
    documentClass: KClass<out D>
  ): Barber<D> {
    val barber = barbers[BarberKey(templateToken, documentClass)]
    if (barber == null) {
      val problems = mutableListOf<String>()
      val documentInstalled = barbers.keys.any { it.document == documentClass }
      val documentDataInstalled = barbers.keys.any { it.templateToken == templateToken }
      if (documentInstalled && documentDataInstalled) {
        problems.add("""
          |Failed to get Barber<${documentClass.qualifiedName}>(templateToken=$templateToken)
          |Requested Document [$documentClass] is installed
          |Requested DocumentData [templateToken=$templateToken] is installed
          |DocumentTemplate with [templateToken=$templateToken] does not have target=[${documentClass.qualifiedName}]
        """.trimMargin())
      }
      if (!documentInstalled) {
        problems.add("""
          |Failed to get Barber<${documentClass.qualifiedName}>(templateToken=$templateToken)
          |Document [$documentClass] is not installed in Barbershop
        """.trimMargin())
      }
      if (!documentDataInstalled) {
        problems.add("""
          |Failed to get Barber<${documentClass.qualifiedName}>(templateToken=$templateToken)
          |DocumentData [$templateToken] and corresponding DocumentTemplate(s) are not installed in Barbershop
        """.trimMargin())
      }
      if (problems.isEmpty()) {
        problems.add(
          "Failed to get Barber<${documentClass.qualifiedName}>(templateToken=$templateToken), unknown error"
        )
      }

      throw BarberException(problems)
    }
    return barber as Barber<D>
  }

  override fun <DD : app.cash.barber.models.DocumentData> getTargetDocuments(
    documentDataClass: KClass<out DD>,
    version: Long?
  ): Set<KClass<out Document>> = getTargetDocuments(documentDataClass.getTemplateToken(), version)

  override fun getTargetDocuments(
    templateToken: TemplateToken,
    version: Long?
  ): Set<KClass<out Document>> = barbers.filter { (barberKey, barber) ->
    barberKey.templateToken == templateToken &&
      // If version is not explicitly specified, use latestTemplateVersionAcrossAllBarbers
      barber.supportedVersionRanges.supports(version
        ?: templateLatestVersions.getValue(barberKey.templateToken)
      )
  }.keys.map { it.document }.toSet()

  override fun getTargetDocuments(
    documentData: DocumentData,
    version: Long?
  ): Set<KClass<out Document>> =
    getTargetDocuments(
      TemplateToken(documentData.template_token!!),
      version
    )

  override fun getAllBarbers(): Map<BarberKey, Barber<Document>> = barbers

  override fun newBuilder() = BarbershopBuilder().apply {
    barbers.keys.forEach {
      installDocument(it.document)
    }
    documentTemplates.forEach {
      installDocumentTemplate(it)
    }
  }

  override fun getWarnings(): List<String> = warnings
}
