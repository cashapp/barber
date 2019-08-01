package app.cash.barber

import app.cash.barber.examples.RecipientReceipt
import app.cash.barber.examples.TransactionalEmailDocument
import app.cash.barber.examples.TransactionalSmsDocument
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_US
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * For test purposes, all of these are run with setWarningsAsErrors
 */
class BarbershopBuilderWarningTest {
  @Test
  fun `setWarningsAsErrors throws for warnings for install with no DocumentTemplate and DocumentData`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocument<TransactionalEmailDocument>()
        .setWarningsAsErrors()
        .build()
    }

    assertEquals(
      """
        |Warnings
        |1) No DocumentData or DocumentTemplates installed
        |
      """.trimMargin(),
      exception.toString())
  }

  @Test
  fun `setWarningsAsErrors fails out early for install with no Documents`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .setWarningsAsErrors()
        .build()
    }
    assertEquals(
      """
        |Warnings
        |1) No Documents installed
        |
      """.trimMargin(),
      exception.toString())
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
}