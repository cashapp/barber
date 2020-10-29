package app.cash.barber

import app.cash.barber.examples.RecipientReceipt
import app.cash.barber.examples.TransactionalEmailDocument
import app.cash.barber.examples.TransactionalSmsDocument
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_US
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

class WarningsAsErrorsTest {
  @Test
  fun `Throws when Warnings present if Barbershop Builder is configured to setWarningsAsErrors`() {
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
      |[app.cash.barber.examples.TransactionalEmailDocument]
      |
      """.trimMargin(), exception.toString())
  }
}
