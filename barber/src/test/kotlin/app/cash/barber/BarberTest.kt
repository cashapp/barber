package app.cash.barber

import app.cash.barber.examples.EncodingTestDocument
import app.cash.barber.examples.InvestmentPurchase
import app.cash.barber.examples.NestedLoginCode
import app.cash.barber.examples.NullableSupportUrlReceipt
import app.cash.barber.examples.RecipientReceipt
import app.cash.barber.examples.TransactionalEmailDocument
import app.cash.barber.examples.TransactionalSmsDocument
import app.cash.barber.examples.investmentPurchaseEncodingDocumentTemplateEN_US
import app.cash.barber.examples.mcDonaldsInvestmentPurchase
import app.cash.barber.examples.nullSupportUrlReceipt
import app.cash.barber.examples.nullableSupportUrlReceipt_EN_US
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_CA
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_GB
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_US
import app.cash.barber.examples.sandy50Receipt
import app.cash.barber.models.BarberSignature
import app.cash.barber.models.DocumentTemplate
import app.cash.barber.models.Locale.Companion.EN_CA
import app.cash.barber.models.Locale.Companion.EN_GB
import app.cash.barber.models.Locale.Companion.EN_US
import app.cash.barber.models.Locale.Companion.ES_US
import kotlin.test.assertEquals
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * These our integration end to end tests
 */
class BarberTest {
  @Test
  fun `Render an SMS`() {
    val barber = BarbershopBuilder()
        .installDocument<TransactionalSmsDocument>()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .build()

    val spec = barber.getBarber<RecipientReceipt, TransactionalSmsDocument>()
        .render(sandy50Receipt, EN_US)

    assertThat(spec).isEqualTo(
        TransactionalSmsDocument(
            sms_body = "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123"
        )
    )
  }

  @Test
  fun `Rendered spec is of specific Document type and allows field access`() {
    val barber = BarbershopBuilder()
        .installDocument<TransactionalSmsDocument>()
        .installDocumentTemplate<NullableSupportUrlReceipt>(nullableSupportUrlReceipt_EN_US)
        .build()

    val spec = barber.getBarber<NullableSupportUrlReceipt, TransactionalSmsDocument>()
        .render(nullSupportUrlReceipt, EN_US)

    assertThat(spec.sms_body).isEqualTo(
        "You got sent \$50.00.")
  }

  @Test
  fun `Null DocumentData fields are rendered as empty string`() {
    val barber = BarbershopBuilder()
        .installDocument<TransactionalSmsDocument>()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .build()

    val spec = barber.getBarber<RecipientReceipt, TransactionalSmsDocument>()
        .render(sandy50Receipt, EN_US)

    // Spec matches
    assertThat(spec).isEqualTo(
        TransactionalSmsDocument(
            sms_body = "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123"
        )
    )

    // Returned rendered spec is type safely accessible
    assertThat(spec.sms_body).isEqualTo(
        "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123")
  }

  @Test
  fun `Render an email`() {
    val recipientReceiptDocumentData = DocumentTemplate(
        fields = mapOf(
            "subject" to "{{sender}} sent you {{amount}}",
            "headline" to "You received {{amount}}",
            "short_description" to "You’ve received a payment from {{sender}}! The money will be in your bank account " +
                "{{deposit_expected_at}}.",
            "primary_button" to "Cancel this payment",
            "primary_button_url" to "{{cancelUrl}}"
        ),
        source = RecipientReceipt::class,
        targets = setOf(TransactionalEmailDocument::class),
        locale = EN_US
    )

    val barber = BarbershopBuilder()
        .installDocument<TransactionalEmailDocument>()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptDocumentData)
        .build()

    val spec = barber.getBarber<RecipientReceipt, TransactionalEmailDocument>()
        .render(sandy50Receipt, EN_US)

    assertThat(spec).isEqualTo(
        TransactionalEmailDocument(
            subject = "Sandy Winchester sent you $50",
            headline = "You received $50",
            short_description = "You’ve received a payment from Sandy Winchester! The money will be in your bank account 2019-05-21T16:02:00Z.",
            primary_button = "Cancel this payment",
            primary_button_url = "https://cash.app/cancel/123",
            secondary_button = null,
            secondary_button_url = null
        )
    )
  }

  @Test
  fun `Render an email and sms with nested data`() {
    val nestedLoginCode = NestedLoginCode(
        code = "123-456",
        button = NestedLoginCode.EmailButton(
            color = "#B3B3B3",
            text = "Login",
            link = "https://cash.app/login",
            size = "regular"
        )
    )

    val nestedLoginCodeEN_US = DocumentTemplate(
        fields = mapOf(
            "subject" to "Your login code is {{code}}",
            "headline" to "Your login code is {{code}}",
            "short_description" to "Your login code is {{code}}",
            "primary_button" to "<Button color=\"{{button.color}}\" size=\"{{button.size}}\">{{button.text}}</Button>",
            "primary_button_url" to "{{button.link}}",
            "sms_body" to "Your login code is {{code}}. Go to {{button.link}} to login."
        ),
        source = NestedLoginCode::class,
        targets = setOf(TransactionalEmailDocument::class, TransactionalSmsDocument::class),
        locale = EN_US
    )

    val barber = BarbershopBuilder()
        .installDocument<TransactionalEmailDocument>()
        .installDocument<TransactionalSmsDocument>()
        .installDocumentTemplate<NestedLoginCode>(nestedLoginCodeEN_US)
        .build()

    val emailSpec = barber.getBarber<NestedLoginCode, TransactionalEmailDocument>()
        .render(nestedLoginCode, EN_US)

    assertThat(emailSpec).isEqualTo(
        TransactionalEmailDocument(
            subject = "Your login code is 123-456",
            headline = "Your login code is 123-456",
            short_description = "Your login code is 123-456",
            primary_button = "<Button color=\"#B3B3B3\" size=\"regular\">Login</Button>",
            primary_button_url = "https://cash.app/login",
            secondary_button = null,
            secondary_button_url = null
        )
    )

    val smsSpec = barber.getBarber<NestedLoginCode, TransactionalSmsDocument>()
        .render(nestedLoginCode, EN_US)

    assertThat(smsSpec).isEqualTo(
        TransactionalSmsDocument(
            sms_body = "Your login code is 123-456. Go to https://cash.app/login to login."
        )
    )
  }

  @Test
  fun `Can install and render multiple locales`() {
    val barbershop = BarbershopBuilder()
        .installDocument<TransactionalSmsDocument>()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_CA)
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_GB)
        .build()

    val recipientReceiptSms = barbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()

    val specEN_US = recipientReceiptSms.render(sandy50Receipt, EN_US)
    assertEquals(
        "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123",
        specEN_US.sms_body)
    val specEN_CA = recipientReceiptSms.render(sandy50Receipt, EN_CA)
    assertEquals(
        "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123 Eh?",
        specEN_CA.sms_body)
    val specEN_GB = recipientReceiptSms.render(sandy50Receipt, EN_GB)
    assertEquals(
        "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123 The Queen approves.",
        specEN_GB.sms_body)
  }

  @Test
  fun `Render succeeds by fallback for a requested locale that is not installed`() {
    val barber = BarbershopBuilder()
        .installDocument<TransactionalSmsDocument>()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .build()

    val spec = barber.getBarber<RecipientReceipt, TransactionalSmsDocument>()
        .render(sandy50Receipt, EN_CA)

    assertThat(spec).isEqualTo(
        TransactionalSmsDocument(
            sms_body = "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123"
        )
    )
  }

  @Test
  fun `BarberField annotation configures Mustache render encoding per field`() {
    val barber = BarbershopBuilder()
        .installDocument<EncodingTestDocument>()
        .installDocumentTemplate<InvestmentPurchase>(investmentPurchaseEncodingDocumentTemplateEN_US)
        .build()

    val spec = barber.getBarber<InvestmentPurchase, EncodingTestDocument>()
        .render(mcDonaldsInvestmentPurchase, EN_US)

    assertThat(spec).isEqualTo(
        EncodingTestDocument(
            no_annotation_field = "You purchased 100 shares of McDonald&#39;s.",
            default_field = "You purchased 100 shares of McDonald&#39;s.",
            html_field = "You purchased 100 shares of McDonald&#39;s.",
            plaintext_field = "You purchased 100 shares of McDonald's."
        )
    )
  }

  @Test
  fun `Can install and render multiple versions`() {
    val key = recipientReceiptSmsDocumentTemplateEN_US.fields.keys.first()
    val field = recipientReceiptSmsDocumentTemplateEN_US.fields.values.first()
    val v1 = recipientReceiptSmsDocumentTemplateEN_US.copy(fields = mapOf(key to "$field v1"), version = 1)
    val v2 = recipientReceiptSmsDocumentTemplateEN_US.copy(fields = mapOf(key to "$field v2"), version = 2)
    val v3 = recipientReceiptSmsDocumentTemplateEN_US.copy(fields = mapOf(key to "$field v3"), version = 3)

    val barbershop = BarbershopBuilder()
        .installDocument<TransactionalSmsDocument>()
        .installDocumentTemplate<RecipientReceipt>(v1)
        .installDocumentTemplate<RecipientReceipt>(v2)
        .installDocumentTemplate<RecipientReceipt>(v3)
        .build()

    val recipientReceiptSms = barbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()

    val specV1 = recipientReceiptSms.render(sandy50Receipt, EN_US, 1)
    assertEquals(
        "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123 v1",
        specV1.sms_body)
    val specV2 = recipientReceiptSms.render(sandy50Receipt, EN_US, 2)
    assertEquals(
        "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123 v2",
        specV2.sms_body)
    val specV3 = recipientReceiptSms.render(sandy50Receipt, EN_US, 3)
    assertEquals(
        "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123 v3",
        specV3.sms_body)
  }

  @Test
  fun `Can install multiple versions and only render compatible`() {
    val key = recipientReceiptSmsDocumentTemplateEN_US.fields.keys.first()
    val field = recipientReceiptSmsDocumentTemplateEN_US.fields.values.first()

    val v1 = recipientReceiptSmsDocumentTemplateEN_US.copy(fields = mapOf(key to "New template with no fields v1"), version = 1)
        .toProto()
        .copy(source_signature = "")

    val v2 = recipientReceiptSmsDocumentTemplateEN_US.copy(fields = mapOf(key to "$field v2"), version = 2)

    val v3sourceSignature = BarberSignature(BarberSignature(v2.toProto().source_signature!!).fields + mapOf("new_variable_not_supported_yet" to app.cash.protos.barber.api.BarberSignature.Type.STRING))
    val v3 = recipientReceiptSmsDocumentTemplateEN_US.copy(fields = mapOf(key to "$field {{ new_variable_not_supported_yet }} v3"), version = 3)
        .toProto()
        .copy(source_signature = v3sourceSignature.signature)

    val barbershop = BarbershopBuilder()
        .installDocument<TransactionalSmsDocument>()
        .installDocumentTemplate(v1)
        .installDocumentTemplate(v2.toProto())
        .installDocumentTemplate(v3)
        .build()

    val recipientReceiptSms = barbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()

    val specV1 = recipientReceiptSms.render(sandy50Receipt, EN_US, 1)
    assertEquals(
        "New template with no fields v1",
        specV1.sms_body)
    val specV2 = recipientReceiptSms.render(sandy50Receipt, EN_US, 2)
    assertEquals(
        "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123 v2",
        specV2.sms_body)
    val specV3 = recipientReceiptSms.render(sandy50Receipt, EN_US, 3)
    assertEquals(
        "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123 v2",
        specV3.sms_body)
    val specMaxCompatible = recipientReceiptSms.render(sandy50Receipt, EN_US)
    assertEquals(
        "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123 v2",
        specMaxCompatible.sms_body)
  }

  @Test
  fun `Can install multiple versions and only render compatible with locale fallback`() {
    val key = recipientReceiptSmsDocumentTemplateEN_US.fields.keys.first()
    val field = recipientReceiptSmsDocumentTemplateEN_US.fields.values.first()

    val v1 = recipientReceiptSmsDocumentTemplateEN_US.copy(fields = mapOf(key to "New template with no fields v1"), version = 1)
        .toProto()
        .copy(source_signature = "")

    val v2 = recipientReceiptSmsDocumentTemplateEN_US.copy(fields = mapOf(key to "$field v2"), version = 2)

    val v3sourceSignature = BarberSignature(BarberSignature(v2.toProto().source_signature!!).fields + mapOf("new_variable_not_supported_yet" to app.cash.protos.barber.api.BarberSignature.Type.STRING))
    val v3 = recipientReceiptSmsDocumentTemplateEN_US.copy(fields = mapOf(key to "$field {{ new_variable_not_supported_yet }} v3"), version = 3)
        .toProto()
        .copy(
            source_signature = v3sourceSignature.signature,
            locale = ES_US.locale
        )

    val barbershop = BarbershopBuilder()
        .installDocument<TransactionalSmsDocument>()
        .installDocumentTemplate(v1)
        .installDocumentTemplate(v2.toProto())
        .installDocumentTemplate(v3)
        .build()

    val recipientReceiptSms = barbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()

    val specV1 = recipientReceiptSms.render(sandy50Receipt, ES_US, 1)
    assertEquals(
        "New template with no fields v1",
        specV1.sms_body)
    val specV2 = recipientReceiptSms.render(sandy50Receipt, ES_US, 2)
    assertEquals(
        "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123 v2",
        specV2.sms_body)
    val specV3 = recipientReceiptSms.render(sandy50Receipt, ES_US, 3)
    assertEquals(
        "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123 v2",
        specV3.sms_body)
    val specMaxCompatible = recipientReceiptSms.render(sandy50Receipt, ES_US)
    assertEquals(
        "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123 v2",
        specMaxCompatible.sms_body)
  }

  @Disabled @Test
  fun fieldStemming() {
    val barber = BarbershopBuilder()
        .installDocument<TransactionalEmailDocument>()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .build()

    val spec = barber.getBarber<RecipientReceipt, TransactionalEmailDocument>()
        .render(sandy50Receipt, EN_US)

    assertThat(spec).isEqualTo(
        TransactionalEmailDocument(
            subject = "Sandy Winchester sent you $50",
            headline = "You received $50",
            short_description = "You’ve received a payment from Sandy Winchester! The money will be in your bank account " +
                "in 2 days.",
            primary_button = "Cancel this payment",
            primary_button_url = "https://cash.app/cancel/123",
            secondary_button = null,
            secondary_button_url = null
        )
    )
  }
}
