package app.cash.barber

import app.cash.barber.examples.BadMapleSyrupLocaleResolver
import app.cash.barber.examples.MapleSyrupOrFirstLocaleResolver
import app.cash.barber.examples.RecipientReceipt
import app.cash.barber.examples.TransactionalEmailDocument
import app.cash.barber.examples.TransactionalSmsDocument
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_CA
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_GB
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_US
import app.cash.barber.examples.sandy50Receipt
import app.cash.barber.models.DocumentData
import app.cash.barber.models.DocumentTemplate
import app.cash.barber.models.Locale.Companion.EN_CA
import app.cash.barber.models.Locale.Companion.EN_GB
import app.cash.barber.models.Locale.Companion.EN_US
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
        sms_body = "Sandy Winchester sent you $50"
      )
    )
  }

  @Test
  fun `Rendered spec is of specific Document type and allows field access`() {
    val barber = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .build()

    val spec = barber.getBarber<RecipientReceipt, TransactionalSmsDocument>()
      .render(sandy50Receipt, EN_US)

    // Spec matches
    assertThat(spec).isEqualTo(
      TransactionalSmsDocument(
        sms_body = "Sandy Winchester sent you $50"
      )
    )

    // Returned rendered spec is type safely accessible
    assertThat(spec.sms_body).isEqualTo("Sandy Winchester sent you $50")
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
  fun `Can install and render multiple locales`() {
    val barbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_CA)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_GB)
      .build()

    val recipientReceiptSms = barbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()

    val specEN_US = recipientReceiptSms.render(sandy50Receipt, EN_US)
    assertEquals("Sandy Winchester sent you \$50", specEN_US.sms_body)
    val specEN_CA = recipientReceiptSms.render(sandy50Receipt, EN_CA)
    assertEquals("Sandy Winchester sent you \$50 Eh?", specEN_CA.sms_body)
    val specEN_GB = recipientReceiptSms.render(sandy50Receipt, EN_GB)
    assertEquals("Sandy Winchester sent you \$50 The Queen approves.", specEN_GB.sms_body)
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
        sms_body = "Sandy Winchester sent you $50"
      )
    )
  }

  @Test
  fun `Use custom LocaleResolver that entirely replaces the default LocaleResolver`() {
    val customResolver = MapleSyrupOrFirstLocaleResolver()
    val allLocaleBarbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_CA)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_GB)
      .setLocaleResolver(customResolver)
      .build()

    val recipientReceiptSms =
      allLocaleBarbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()

    // You always get EN_CA response back with [MapleSyrupOrFirstLocaleResolver]
    val specEN_US = recipientReceiptSms.render(sandy50Receipt, EN_US)
    assertEquals("Sandy Winchester sent you \$50 Eh?", specEN_US.sms_body)
    val specEN_CA = recipientReceiptSms.render(sandy50Receipt, EN_CA)
    assertEquals("Sandy Winchester sent you \$50 Eh?", specEN_CA.sms_body)
    val specEN_GB = recipientReceiptSms.render(sandy50Receipt, EN_GB)
    assertEquals("Sandy Winchester sent you \$50 Eh?", specEN_GB.sms_body)

    // ...and if EN_CA is not installed then [MapleSyrupOrFirstLocaleResolver] returns the first option
    val onlyUsBarbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .setLocaleResolver(customResolver)
      .build()

    val specEN_US2 = onlyUsBarbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()
      .render(sandy50Receipt, EN_CA)
    assertEquals("Sandy Winchester sent you \$50", specEN_US2.sms_body)
  }

  @Test
  fun `Fails when custom LocaleResolver that doesn't respect contract`() {
    val customResolver = BadMapleSyrupLocaleResolver()
    val allLocaleBarbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_CA)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_GB)
      .setLocaleResolver(customResolver)
      .build()

    val recipientReceiptSms =
      allLocaleBarbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()

    // You always get EN_CA response back with [BadMapleSyrupLocaleResolver]
    val specEN_US = recipientReceiptSms.render(sandy50Receipt, EN_US)
    assertEquals("Sandy Winchester sent you \$50 Eh?", specEN_US.sms_body)
    val specEN_CA = recipientReceiptSms.render(sandy50Receipt, EN_CA)
    assertEquals("Sandy Winchester sent you \$50 Eh?", specEN_CA.sms_body)
    val specEN_GB = recipientReceiptSms.render(sandy50Receipt, EN_GB)
    assertEquals("Sandy Winchester sent you \$50 Eh?", specEN_GB.sms_body)

    // ...and if EN_CA is not installed then [BadMapleSyrupLocaleResolver] blows up
    val onlyUsBarbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .setLocaleResolver(customResolver)
      .build()

    val exception = assertFailsWith<BarberException> {
      onlyUsBarbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()
        .render(sandy50Receipt, EN_CA)
    }
    assertEquals("""
      |Problems
      |1) Resolved entry is not valid key in Map.
      |LocaleResolver: class app.cash.barber.examples.BadMapleSyrupLocaleResolver
      |Locale: Locale(locale=en-CA)
      |Resolved Locale: Locale(locale=en-CA)
      |
    """.trimMargin(), exception.toString())
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

  @Disabled @Test
  fun copyUsesFieldThatDoesntExist() {
    val recipientReceiptDocumentData = DocumentTemplate(
      fields = mapOf(
        "subject" to "{{sender}} sent you {{totally_invalid_field}}",
        "headline" to "",
        "short_description" to "",
        "primary_button" to "",
        "primary_button_url" to ""
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalEmailDocument::class),
      locale = EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptDocumentData)
    }
    assertThat(exception.problems).containsExactly("""
        |output field 'subject' uses 'totally_invalid_field' but 'RecipientReceipt' has no such field
        |  {{sender}} sent you {{totally_invalid_field}}
        |  valid fields are [sender, amount, cancelUrl, deposit_expected_at]
        |""".trimMargin()
    )
  }

  // TODO: name the input fields ('sender', 'amount' etc.) that the DocumentData defines
  // TODO: name the output fields ('subject', 'headline' etc.) that the DocuemntSpec has

  @Disabled @Test
  fun copyDoesntOutputFieldThatIsRequired() {
    val recipientReceiptDocumentData = DocumentTemplate(
      fields = mapOf(
        "subject" to "",
        "headline" to "",
        "primary_button" to "",
        "primary_button_url" to ""
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalEmailDocument::class),
      locale = EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptDocumentData)
    }
    assertThat(exception.problems).containsExactly("""
        |output field 'short_description' is required but was not found
        |""".trimMargin()
    )
  }

  @Disabled @Test
  fun copyOutputsFieldThatIsUnknown() {
    val recipientReceiptDocumentData = DocumentTemplate(
      fields = mapOf(
        "subject" to "",
        "headline" to "",
        "primary_button" to "",
        "primary_button_url" to "",
        "tertiary_button_url" to ""
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalEmailDocument::class),
      locale = EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptDocumentData)
    }
    assertThat(exception.problems).containsExactly("""
        |output field 'tertiary_button_url' is not used
        |""".trimMargin()
    )
  }

  @Disabled @Test
  fun renderUnknownDocumentData() {
    val recipientReceiptDocumentData = DocumentTemplate(
      fields = mapOf(
        "subject" to "",
        "headline" to "",
        "short_description" to "",
        "primary_button" to "",
        "primary_button_url" to ""
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalEmailDocument::class),
      locale = EN_US
    )

    BarbershopBuilder()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptDocumentData)

    val exception = assertFailsWith<BarberException> {
      // TODO confirm failure when render (SenderReceipt::class, TransactionalEmailDocument::class)
    }

    assertThat(exception.problems).containsExactly("""
        |unknown copy model: SenderReceipt
        |""".trimMargin()
    )
  }

  @Disabled @Test
  fun renderUnknownDocument() {
    val recipientReceiptDocumentData = DocumentTemplate(
      fields = mapOf(
        "subject" to "",
        "headline" to "",
        "short_description" to "",
        "primary_button" to "",
        "primary_button_url" to ""
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalEmailDocument::class),
      locale = EN_US
    )

    BarbershopBuilder()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptDocumentData)

    val exception = assertFailsWith<BarberException> {
      // TODO confirm failure when render (RecipientReceipt::class, SmsDocument::class)
    }

    assertThat(exception.problems).containsExactly("""
        |unknown document: SmsDocument
        |""".trimMargin()
    )
  }

  @Disabled @Test
  fun singleDocumentDataHasMultipleTargets() {
    TODO()
  }

  @Disabled @Test
  fun failOnSingleUnit() {
    data class StrangeUnitDocumentData(
      val strange: Unit
    ) : DocumentData

    TODO()
  }
}
