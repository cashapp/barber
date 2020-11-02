package app.cash.barber

import app.cash.barber.models.BarberKey
import app.cash.barber.models.Document
import app.cash.barber.models.DocumentData
import app.cash.barber.models.TemplateToken
import app.cash.barber.models.TemplateToken.Companion.getTemplateToken
import kotlin.reflect.KClass

internal class RealBarbershop(
  private val barbers: Map<BarberKey, Barber<Document>>,
  private val warnings: List<String>
) : Barbershop {
  @Suppress("UNCHECKED_CAST")
  override fun <DD : DocumentData, D : Document> getBarber(
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

  override fun <DD : DocumentData> getTargetDocuments(
    documentDataClass: KClass<out DD>
  ): Set<KClass<out Document>> = getTargetDocuments(documentDataClass.getTemplateToken())

  override fun getTargetDocuments(templateToken: TemplateToken): Set<KClass<out Document>> = barbers.keys
      .filter { it.templateToken == templateToken }
      .map { it.document }
      .toSet()

  override fun getTargetDocuments(documentData: app.cash.protos.barber.api.DocumentData): Set<KClass<out Document>> =
      getTargetDocuments(TemplateToken(documentData.template_token!!))

  override fun getAllBarbers(): Map<BarberKey, Barber<Document>> = barbers

  override fun getWarnings(): List<String> = warnings
}
