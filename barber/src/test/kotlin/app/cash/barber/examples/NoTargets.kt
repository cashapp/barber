package app.cash.barber.examples

import app.cash.barber.locale.Locale
import app.cash.barber.models.DocumentTemplate

val noTargetsDocumentTemplate = DocumentTemplate(
    fields = mapOf(),
    source = EmptyDocumentData::class,
    targets = emptySet(),
    locale = Locale.EN_US
)
