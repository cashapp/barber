package com.squareup.barber

import com.squareup.barber.models.CopyModel
import com.squareup.barber.models.DocumentSpec

interface Renderer<C : CopyModel, D : DocumentSpec> {
  /**
   * @return a [DocumentSpec] with the values of a [CopyModel] instance rendered in the [DocumentCopy] template
   */
  fun render(copyModel: C): D
}