package app.cash.barber

import app.cash.barber.models.Document
import app.cash.barber.models.DocumentData
import app.cash.barber.models.DocumentTemplate
import app.cash.barber.models.Locale

/**
 * Uses a template to turn document datas into a rendered document.
 */
interface Barber<DD : DocumentData, D : Document> {
  fun render(documentData: DD, locale: Locale): D
}