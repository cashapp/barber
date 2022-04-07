package app.cash.barber.locale

import app.cash.barber.BarberException
import app.cash.barber.BarbershopBuilder
import app.cash.barber.examples.RecipientReceipt
import app.cash.barber.examples.TransactionalSmsDocument
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_CA
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_GB
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_US
import app.cash.barber.examples.sandy50Receipt
import app.cash.barber.getBarber
import app.cash.barber.locale.Locale.Companion.EN_CA
import app.cash.barber.locale.Locale.Companion.EN_US
import app.cash.protos.barber.api.DocumentTemplate
import com.google.common.collect.HashBasedTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LocaleResolverTest {
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
    val expectedEN_CA =
        "Sandy Winchester sent you $50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123 Eh?"
    val specEN_US = recipientReceiptSms.render(sandy50Receipt, Locale.EN_US)
    assertEquals(expectedEN_CA, specEN_US.sms_body)
    val specEN_CA = recipientReceiptSms.render(sandy50Receipt, EN_CA)
    assertEquals(expectedEN_CA, specEN_CA.sms_body)
    val specEN_GB = recipientReceiptSms.render(sandy50Receipt, Locale.EN_GB)
    assertEquals(expectedEN_CA, specEN_GB.sms_body)

    // ...and if EN_CA is not installed then [MapleSyrupOrFirstLocaleResolver] returns the first option
    val onlyUsBarbershop = BarbershopBuilder()
        .installDocument<TransactionalSmsDocument>()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .setLocaleResolver(customResolver)
        .build()

    val specEN_US2 = onlyUsBarbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()
        .render(sandy50Receipt, EN_CA)
    assertEquals(
        "Sandy Winchester sent you \$50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123",
        specEN_US2.sms_body)
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
    val expectedEN_CA =
        "Sandy Winchester sent you $50. It will be available at 2019-05-21T16:02:00Z. Cancel here: https://cash.app/cancel/123 Eh?"
    val specEN_US = recipientReceiptSms.render(sandy50Receipt, Locale.EN_US)
    assertEquals(expectedEN_CA, specEN_US.sms_body)
    val specEN_CA = recipientReceiptSms.render(sandy50Receipt, EN_CA)
    assertEquals(expectedEN_CA, specEN_CA.sms_body)
    val specEN_GB = recipientReceiptSms.render(sandy50Receipt, Locale.EN_GB)
    assertEquals(expectedEN_CA, specEN_GB.sms_body)

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
    assertEquals(
        """
        |Errors
        |1) Resolved entry is not valid key in Map.
        |LocaleResolver: class app.cash.barber.locale.BadMapleSyrupLocaleResolver
        |Locale: [Locale=en-CA]
        |Resolved Locale: [Locale=en-CA]
        |
      """.trimMargin(),
        exception.toString())
  }

  @Test
  fun `LocaleResolver works with a Guava Table`() {
    val resolver = MapleSyrupOrFirstLocaleResolver()
    val table = HashBasedTable.create<Locale, Long, DocumentTemplate>()
    val templateToken = "t1"
    val alpha = DocumentTemplate(template_token = templateToken)
    val bravo = DocumentTemplate(template_token = templateToken)
    val charlie = DocumentTemplate(template_token = templateToken)
    val delta = DocumentTemplate(template_token = templateToken)

    table.put(EN_US, 1, alpha)
    table.put(EN_US, 2, bravo)
    table.put(EN_CA, 1, charlie)
    table.put(EN_CA, 2, delta)

    val expectedMap = mapOf(
        1L to charlie,
        2L to delta
    )
    assertThat(resolver.resolve(EN_US, table, templateToken)).containsAllEntriesOf(expectedMap)
  }

  @Test
  fun `LocaleResolver fails with empty Guava Table`() {
    val resolver = MapleSyrupOrFirstLocaleResolver()
    val table = HashBasedTable.create<Locale, Long, DocumentTemplate>()
    val exception = assertFailsWith<BarberException> {
      resolver.resolve(EN_US, table, "alpha")
    }
    assertEquals(exception.toString(), """
      |Errors
      |1) Can not resolve entry of an empty Table [templateToken=alpha][locale=en-US].
      |
    """.trimMargin())
  }
}
