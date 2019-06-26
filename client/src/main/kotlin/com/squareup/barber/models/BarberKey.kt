package com.squareup.barber.models

import kotlin.reflect.KClass

data class BarberKey(
  val documentData: KClass<out DocumentData>,
  val document: KClass<out Document>
)