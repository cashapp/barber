package app.cash.barber

import app.cash.barber.models.BarberKey
import app.cash.barber.models.Document
import app.cash.barber.models.DocumentData
import kotlin.reflect.KClass

internal class RealBarbershop(
  private val barbers: Map<BarberKey, Barber<DocumentData, Document>>
) : Barbershop {
  @Suppress("UNCHECKED_CAST")
  override fun <DD : DocumentData, D : Document> getBarber(
    documentDataClass: KClass<out DD>,
    documentClass: KClass<out D>
  ): Barber<DD, D> = barbers[BarberKey(documentDataClass, documentClass)] as Barber<DD, D>

  override fun getAllBarbers(): Map<BarberKey, Barber<DocumentData, Document>> = barbers
}
