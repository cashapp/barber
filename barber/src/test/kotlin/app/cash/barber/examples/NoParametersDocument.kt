package app.cash.barber.examples

import app.cash.barber.models.Document
import app.cash.barber.models.DocumentData
import app.cash.barber.models.DocumentTemplate
import app.cash.barber.models.Locale

class NoParametersDocument : Document

class EmptyDocumentData : DocumentData

val noParametersDocumentTemplate = DocumentTemplate(
    fields = mapOf(),
    source = EmptyDocumentData::class,
    targets = setOf(NoParametersDocument::class),
    locale = Locale.EN_US
)
