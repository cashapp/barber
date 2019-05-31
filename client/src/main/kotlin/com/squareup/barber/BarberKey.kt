package com.squareup.barber

import com.squareup.barber.models.DocumentData
import com.squareup.barber.models.Document
import kotlin.reflect.KClass

data class BarberKey (
  val documentData: KClass<out DocumentData>,
  val document: KClass<out Document>
)