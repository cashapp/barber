package app.cash.barber

import app.cash.barber.examples.RecipientReceipt
import app.cash.barber.examples.TransactionalEmailDocument
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_US
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

class WarningsAsErrorsTest {
  @Test
  fun `Install fails on no DocumentTemplate and DocumentData`() {
    // Does not fail but returns warnings
    val barbershop = BarbershopBuilder()
        .installDocument<TransactionalEmailDocument>()
        .build()
    assertEquals(listOf("No DocumentData or DocumentTemplates installed"), barbershop.getWarnings())
  }

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
  fun `Install fails on no Documents`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
          .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
          .build()
    }
    assertEquals(
        """
          |Errors
          |1) Attempted to install DocumentTemplate without the corresponding Document being installed.
          |Not installed DocumentTemplate.target_signatures:
          |[BarberSignature(signature=sms_body,1, fields={sms_body=STRING})]
          |
          |Warnings
          |1) No Documents installed
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
}
