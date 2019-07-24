package app.cash.barber

import app.cash.barber.examples.RecipientReceipt
import app.cash.barber.examples.SenderReceipt
import app.cash.barber.examples.TransactionalEmailDocument
import app.cash.barber.examples.TransactionalSmsDocument
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_CA
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_GB
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_US
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
  fun `Fails when DocumentTemplate targets are not installed Documents`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .build()
    }
    assertEquals("""
      |Problems
      |1) Attempted to install DocumentTemplate without the corresponding Document being installed.
      |Not installed DocumentTemplate.targets:
      |[class app.cash.barber.examples.TransactionalSmsDocument]
      |
      """.trimMargin(),
      exception.toString())
  }

  @Test
  fun `Fails when DocumentTemplate installed with non-source DocumentData`() {
    val builder = BarbershopBuilder()
      .installDocument<TransactionalEmailDocument>()
    val exception = assertFailsWith<BarberException> {
      builder
        .installDocumentTemplate<SenderReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .build()
    }
    assertEquals("""
      |Problems
      |1) Attempted to install DocumentTemplate with a DocumentData not specified in the DocumentTemplate source.
      |DocumentTemplate.source: class app.cash.barber.examples.RecipientReceipt
      |DocumentData: class app.cash.barber.examples.SenderReceipt
      |
      |2) Attempted to install DocumentTemplate without the corresponding Document being installed.
      |Not installed DocumentTemplate.targets:
      |[class app.cash.barber.examples.TransactionalSmsDocument]
      |
      |3) Missing variable [sender] in DocumentData [class app.cash.barber.examples.SenderReceipt] for DocumentTemplate field [{{sender}} sent you {{amount}}]
      |
      |4) Missing variable [sender] in DocumentData [class app.cash.barber.examples.SenderReceipt] for DocumentTemplate field [You’ve received a payment from {{sender}}! The money will be in your bank account {{deposit_expected_at.casual}}.]
      |
      |5) Missing variable [sender] in DocumentData [class app.cash.barber.examples.SenderReceipt] for DocumentTemplate field [{{sender}} sent you {{amount}}]
      |
      |6) Unused DocumentData variable [recipient] in [class app.cash.barber.examples.SenderReceipt] with no usage in installed DocumentTemplate Locales:
      |Locale(locale=en-US)
      |
      """.trimMargin(), exception.toString())
  }
}