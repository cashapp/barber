package com.squareup.barber

import com.squareup.barber.models.BarberKey
import com.squareup.barber.models.Document
import com.squareup.barber.models.DocumentData
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
