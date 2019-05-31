package com.squareup.barber

import com.squareup.barber.models.DocumentData
import com.squareup.barber.models.Document

interface Renderer<C : DocumentData, D : Document> {
  /**
   * @return a [Document] with the values of a [DocumentData] instance rendered in the [DocumentTemplate]
   */
  fun render(documentData: C): D
}