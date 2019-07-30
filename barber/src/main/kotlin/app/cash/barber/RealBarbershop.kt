package app.cash.barber

import app.cash.barber.models.BarberKey
import app.cash.barber.models.Document
import app.cash.barber.models.DocumentData
import kotlin.reflect.KClass

internal class RealBarbershop(
  private val barbers: Map<BarberKey, Barber<DocumentData, Document>>,
  private val warnings: List<String>
) : Barbershop {
  @Suppress("UNCHECKED_CAST")
  override fun <DD : DocumentData, D : Document> getBarber(
    documentDataClass: KClass<out DD>,
    documentClass: KClass<out D>
  ): Barber<DD, D> {
    val barber = barbers[BarberKey(documentDataClass, documentClass)]
    if (barber == null) {
      val problems = mutableListOf<String>()
      val documentInstalled = barbers.keys.any { it.document == documentClass }
      val documentDataInstalled = barbers.keys.any { it.documentData == documentDataClass }
      if (documentInstalled && documentDataInstalled) {
        problems.add("""
          |Failed to get Barber<$documentDataClass, $documentClass>
          |Requested Document [$documentClass] is installed
          |Requested DocumentData [$documentDataClass] is installed
          |DocumentTemplate with source=[$documentDataClass] does not have target=[$documentClass]
        """.trimMargin())
      }
      if (!documentInstalled) {
        problems.add("""
          |Failed to get Barber<$documentDataClass, $documentClass>
          |Document [$documentClass] is not installed in Barbershop
        """.trimMargin())
      }
      if (!documentDataInstalled) {
        problems.add("""
          |Failed to get Barber<$documentDataClass, $documentClass>
          |DocumentData [$documentDataClass] and corresponding DocumentTemplate(s) are not installed in Barbershop
        """.trimMargin())
      }
      throw BarberException(problems)
    }
    return barber as Barber<DD, D>
  }

  override fun getAllBarbers(): Map<BarberKey, Barber<DocumentData, Document>> = barbers

  override fun getWarnings(): List<String> = warnings
}
