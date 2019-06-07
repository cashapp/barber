package com.squareup.barber

import com.squareup.barber.models.Document
import com.squareup.barber.models.DocumentData
import com.squareup.barber.models.DocumentTemplate
import com.squareup.barber.models.Locale

interface Barber<C : DocumentData, D : Document> {
  /**
   * @return a [Document] with the values of a [DocumentData] instance rendered in the [DocumentTemplate]
   */
  fun render(documentData: C, locale: Locale): D
}