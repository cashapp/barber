package app.cash.barber.examples

import app.cash.barber.locale.Locale
import app.cash.barber.models.DocumentData
import app.cash.barber.models.DocumentTemplate
import java.time.Instant

class MultiVersionDocumentTargetChangeDocumentData : DocumentData

val multiVersionDocumentTarget_v3_emailSms = DocumentTemplate(
    fields = mapOf(
        "sms_body" to "v3",
        "subject" to "v3",
        "headline" to "v3",
        "short_description" to "v3",
        "primary_button" to "v3",
        "primary_button_url" to "v3",
        "secondary_button" to "v3",
        "secondary_button_url" to "v3",
    ),
    locale = Locale.EN_US,
    source = MultiVersionDocumentTargetChangeDocumentData::class,
    targets = setOf(TransactionalEmailDocument::class, TransactionalSmsDocument::class),
    version = 3L
)

val multiVersionDocumentTarget_v4_email = DocumentTemplate(
    fields = mapOf(
        "subject" to "v4", 
        "headline" to "v4", 
        "short_description" to "v4", 
        "primary_button" to "v4", 
        "primary_button_url" to "v4",
        "secondary_button" to "v4", 
        "secondary_button_url" to "v4", 
    ),
    locale = Locale.EN_US,
    source = MultiVersionDocumentTargetChangeDocumentData::class,
    targets = setOf(TransactionalEmailDocument::class),
    version = 4L
)

val multiVersionDocumentTargetChange = MultiVersionDocumentTargetChangeDocumentData()
