package app.cash.barber

import app.cash.barber.examples.RecipientReceipt
import app.cash.barber.examples.SenderReceipt
import app.cash.barber.examples.TransactionalEmailDocument
import app.cash.barber.examples.TransactionalSmsDocument
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_CA
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_GB
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_US
import app.cash.barber.examples.senderReceiptEmailDocumentTemplateEN_US
import app.cash.barber.models.DocumentData
import app.cash.barber.models.DocumentTemplate
import app.cash.barber.models.Locale
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BarbershopBuilderTest {
  @Test
  fun `Install works`() {
    BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .build()
  }

  @Test
  fun `Install works regardless of order`() {
    BarbershopBuilder()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .installDocument<TransactionalSmsDocument>()
      .build()
  }

  @Test
  fun `Install multiple locales`() {
    BarbershopBuilder()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_CA)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_GB)
      .installDocument<TransactionalSmsDocument>()
      .build()
  }

  @Test
  fun `Fails when DocumentTemplate target Documents are not installed`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .installDocumentTemplate<SenderReceipt>(senderReceiptEmailDocumentTemplateEN_US)
        .installDocument<TransactionalEmailDocument>()
        .build()
    }
    assertEquals("""
      |Errors
      |1) Attempted to install DocumentTemplate without the corresponding Document being installed.
      |Not installed DocumentTemplate.targets:
      |[class app.cash.barber.examples.TransactionalSmsDocument]
      |
      """.trimMargin(),
      exception.toString())
  }

  @Test
  fun `Fails when DocumentTemplate installed with non-source DocumentData`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate<SenderReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .installDocument<TransactionalSmsDocument>()
        .build()
    }
    assertEquals("""
      |Errors
      |1) Attempted to install DocumentTemplate with a DocumentData not specified in the DocumentTemplate source.
      |DocumentTemplate.source: class app.cash.barber.examples.RecipientReceipt
      |DocumentData: class app.cash.barber.examples.SenderReceipt
      |
      |2) Missing variable [sender] in DocumentData [class app.cash.barber.examples.SenderReceipt] for DocumentTemplate field [{{sender}} sent you {{amount}}. It will be available at {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}]
      |
      |Warnings
      |1) Unused DocumentData variable [recipient] in [class app.cash.barber.examples.SenderReceipt] with no usage in installed DocumentTemplate Locales:
      |[Locale=en-US]
      |
      """.trimMargin(), exception.toString())
  }

  @Test
  fun `Fails on unused dangling installed Document`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocument<TransactionalEmailDocument>()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .installDocument<TransactionalSmsDocument>()
        .setWarningsAsErrors()
        .build()
    }
    assertEquals("""
      |Warnings
      |1) Document installed that is not used in any installed DocumentTemplates
      |[class app.cash.barber.examples.TransactionalEmailDocument]
      |
      |
      """.trimMargin(), exception.toString())
  }

  @Test
  fun `Fails when variable in field template is not in source DocumentData`() {
    data class TradeReceipt(
      val ticker: String
    ) : DocumentData

    val tradeReceiptEN_US = DocumentTemplate(
      fields = mapOf(
        "sms_body" to "You bought {{ shares }} shares of {{ ticker }}."
      ),
      source = TradeReceipt::class,
      targets = setOf(TransactionalSmsDocument::class),
      locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocument<TransactionalSmsDocument>()
        .installDocumentTemplate<TradeReceipt>(tradeReceiptEN_US)
        .build()
    }
    assertEquals(
      """
        |Errors
        |1) Missing variable [shares] in DocumentData [class app.cash.barber.BarbershopBuilderTest::Fails when variable in field template is not in source DocumentData::TradeReceipt] for DocumentTemplate field [You bought {{ shares }} shares of {{ ticker }}.]
        |
      """.trimMargin(),
      exception.toString())
  }

  @Test
  fun `Fails when variable in data is not used in any field template`() {
    data class TradeReceipt(
      val ticker: String,
      val shares: String
    ) : DocumentData

    val tradeReceiptEN_US = DocumentTemplate(
      fields = mapOf(
        "sms_body" to "Welcome to the {{ ticker }} shareholder family!"
      ),
      source = TradeReceipt::class,
      targets = setOf(TransactionalSmsDocument::class),
      locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocument<TransactionalSmsDocument>()
        .installDocumentTemplate<TradeReceipt>(tradeReceiptEN_US)
        .setWarningsAsErrors()
        .build()
    }
    assertEquals(
      """
        |Warnings
        |1) Unused DocumentData variable [shares] in [class app.cash.barber.BarbershopBuilderTest::Fails when variable in data is not used in any field template::TradeReceipt] with no usage in installed DocumentTemplate Locales:
        |[Locale=en-US]
        |
      """.trimMargin(),
      exception.toString())
  }

  @Test
  fun `Fails when DocumentTemplate does not have sufficient fields for target Document`() {
    val recipientReceiptDocumentData = DocumentTemplate(
      fields = mapOf(
        "subject" to "{{ sender }} sent {{ amount }} on {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}"
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalSmsDocument::class),
      locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptDocumentData)
        .installDocument<TransactionalSmsDocument>()
        .build()
    }
    assertEquals(
      """
        |Errors
        |1) Installed DocumentTemplate missing required fields for Document targets
        |Missing fields:
        |[class app.cash.barber.examples.TransactionalSmsDocument] requires missing fields [sms_body]
        |
        |DocumentTemplate: DocumentTemplate(
        | fields = mapOf(
        |   subject={{ sender }} sent {{ amount }} on {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}
        | ),
        | source = class app.cash.barber.examples.RecipientReceipt,
        | targets = [class app.cash.barber.examples.TransactionalSmsDocument],
        | locale = [Locale=en-US]
        |)
        |
      """.trimMargin(),
      exception.toString())
  }

  @Test
  fun `DocumentTemplate has extra fields unused by target Documents`() {
    val recipientReceiptDocumentData = DocumentTemplate(
      fields = mapOf(
        "sms_body" to "{{ sender }} sent you {{ amount }} on {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}",
        "extra_field" to "This field is unused in any target document"
      ),
    source = RecipientReceipt::class,
    targets = setOf(TransactionalSmsDocument::class),
    locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptDocumentData)
        .installDocument<TransactionalSmsDocument>()
        .build()
    }
    assertEquals(
      """
        |Errors
        |1) Installed DocumentTemplate has additional fields that are not used in any target Document
        |Additional fields:
        |extra_field
        |
      """.trimMargin(),
      exception.toString())
  }

  @Test
  fun `Fails on duplicate install of DocumentTemplate`() {
    val first = DocumentTemplate(
      fields = mapOf(
        "sms_body" to "first {{ sender }} sent you {{ amount }} on {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}"
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalSmsDocument::class),
      locale = Locale.EN_US
    )
    val second = DocumentTemplate(
      fields = mapOf(
        "sms_body" to "second {{ sender }} sent you {{ amount }} on {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}"
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalSmsDocument::class),
      locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(first)
        .installDocumentTemplate<RecipientReceipt>(second)
        .installDocument<TransactionalSmsDocument>()
        .build()
    }
    assertEquals(
      """
        |Errors
        |1) Attempted to install DocumentTemplate that will overwrite an already installed DocumentTemplate with locale
        |[Locale=en-US].
        |Already Installed
        |DocumentData: class app.cash.barber.examples.RecipientReceipt
        |Locales:
        |[Locale=en-US]
        |DocumentTemplates: [
        |DocumentTemplate(
        | fields = mapOf(
        |   sms_body=first {{ sender }} sent you {{ amount }} on {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}
        | ),
        | source = class app.cash.barber.examples.RecipientReceipt,
        | targets = [class app.cash.barber.examples.TransactionalSmsDocument],
        | locale = [Locale=en-US]
        |)]
        |
        |Attempted to Install
        |DocumentTemplate(
        | fields = mapOf(
        |   sms_body=second {{ sender }} sent you {{ amount }} on {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}
        | ),
        | source = class app.cash.barber.examples.RecipientReceipt,
        | targets = [class app.cash.barber.examples.TransactionalSmsDocument],
        | locale = [Locale=en-US]
        |)
        |
      """.trimMargin(),
      exception.toString())
  }
}