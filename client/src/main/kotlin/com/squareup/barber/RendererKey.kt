package com.squareup.barber

import com.squareup.barber.models.CopyModel
import com.squareup.barber.models.DocumentSpec
import kotlin.reflect.KClass

data class RendererKey (
  val copyModel: KClass<out CopyModel>,
  val documentSpec: KClass<out DocumentSpec>
)