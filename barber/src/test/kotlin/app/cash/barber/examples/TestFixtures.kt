package app.cash.barber.examples

import app.cash.barber.models.DocumentTemplate
import app.cash.barber.models.Locale.Companion.EN_CA
import app.cash.barber.models.Locale.Companion.EN_GB
import app.cash.barber.models.Locale.Companion.EN_US

val recipientReceiptSmsDocumentTemplateEN_US = DocumentTemplate(
    fields = mapOf(
        "sms_body" to "{{sender}} sent you {{amount}}. It will be available at {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}"
    ),
    source = RecipientReceipt::class,
    targets = setOf(TransactionalSmsDocument::class),
    locale = EN_US
)

val investmentPurchaseEncodingDocumentTemplateEN_US = DocumentTemplate(
    fields = mapOf(
        "no_annotation_field" to "You purchased {{ shares }} shares of {{ ticker }}.",
        "default_field" to "You purchased {{ shares }} shares of {{ ticker }}.",
        "html_field" to "You purchased {{ shares }} shares of {{ ticker }}.",
        "plaintext_field" to "You purchased {{ shares }} shares of {{ ticker }}."
    ),
    source = InvestmentPurchase::class,
    targets = setOf(EncodingTestDocument::class),
    locale = EN_US
)

val investmentPurchaseShadowEncodingDocumentTemplateEN_US = DocumentTemplate(
    fields = mapOf(
        "no_annotation_field" to "You purchased {{ shares }} shares of {{ ticker }}.",
        "default_field" to "You purchased {{ shares }} shares of {{ ticker }}.",
        "html_field" to "You purchased {{ shares }} shares of {{ ticker }}.",
        "plaintext_field" to "You purchased {{ shares }} shares of {{ ticker }}.",
        "non_shadow_field" to "You purchased {{ shares }} shares of {{ ticker }}."
    ),
    source = InvestmentPurchase::class,
    targets = setOf(EncodingTestDocument::class, ShadowEncodingEverythingPlaintextTestDocument::class),
    locale = EN_US
)

val recipientReceiptSmsEmailDocumentTemplateEN_US = DocumentTemplate(
    fields = mapOf(
        "subject" to "{{sender}} sent you {{amount}}",
        "headline" to "You received {{amount}}",
        "short_description" to "You’ve received a payment from {{sender}}! The money will be in your bank account " +
            "{{deposit_expected_at}}.",
        "primary_button" to "Cancel this payment",
        "primary_button_url" to "{{cancelUrl}}",
        "sms_body" to "{{sender}} sent you {{amount}}"
    ),
    source = RecipientReceipt::class,
    targets = setOf(TransactionalEmailDocument::class, TransactionalSmsDocument::class),
    locale = EN_US
)

val senderReceiptEmailDocumentTemplateEN_US = DocumentTemplate(
    fields = mapOf(
        "subject" to "You sent {{amount}} to {{recipient}}",
        "headline" to "You sent {{amount}}",
        "short_description" to "You sent a payment to {{recipient}}! The money will be in their bank account " +
            "{{deposit_expected_at}}.",
        "primary_button" to "Cancel this payment",
        "primary_button_url" to "{{cancelUrl}}"
    ),
    source = SenderReceipt::class,
    targets = setOf(TransactionalEmailDocument::class),
    locale = EN_US
)

val recipientReceiptSmsDocumentTemplateEN_CA = recipientReceiptSmsDocumentTemplateEN_US.copy(
    fields = recipientReceiptSmsDocumentTemplateEN_US.fields.mapValues { it.value + " Eh?" },
    locale = EN_CA
)

val recipientReceiptSmsDocumentTemplateEN_GB = recipientReceiptSmsDocumentTemplateEN_US.copy(
    fields = recipientReceiptSmsDocumentTemplateEN_US.fields.mapValues { it.value + " The Queen approves." },
    locale = EN_GB
)
