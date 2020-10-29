package app.cash.barber.examples

import app.cash.barber.models.DocumentData
import app.cash.barber.models.DocumentTemplate
import app.cash.barber.locale.Locale

data class NullableSupportUrlReceipt(
  val amount: String,
  val hidableProblemUrl: String? = null
) : DocumentData

val nullableSupportUrlReceipt_EN_US = DocumentTemplate(
    fields = mapOf(
        "sms_body" to "You got sent {{ amount }}.{{ hidableProblemUrl }}"
    ),
    source = NullableSupportUrlReceipt::class,
    targets = setOf(TransactionalSmsDocument::class),
    locale = Locale.EN_US
)

val nullSupportUrlReceipt = NullableSupportUrlReceipt(
    amount = "$50.00"
)