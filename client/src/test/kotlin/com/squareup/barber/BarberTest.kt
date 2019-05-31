package com.squareup.barber

import com.squareup.barber.examples.RecipientReceipt
import com.squareup.barber.examples.SenderReceipt
import com.squareup.barber.examples.TransactionalEmailDocument
import com.squareup.barber.examples.TransactionalSmsDocument
import com.squareup.barber.examples.recipientReceiptSmsDocumentTemplate
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class BarberTest {
  @Test
  fun `Install works`() {
    Barber.Builder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplate)
      .build()
  }

  @Test
  fun `Install works regardless of order`() {
    Barber.Builder()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplate)
      .installDocument<TransactionalSmsDocument>()
      .build()
  }

  @Test
  fun `Fails when DocumentTemplate targets are not installed Documents`() {
    val exception = assertFailsWith<BarberException> {
      Barber.Builder()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplate)
        .build()
    }
    Assertions.assertThat(exception.problems).containsExactly("""
      |Attempted to install DocumentTemplate without the corresponding Document being installed.
      |Not installed DocumentTemplate.targets:
      |[class com.squareup.barber.examples.TransactionalSmsDocument]""".trimMargin())
  }

  @Test
  fun `Fails when DocumentTemplate installed with non-source DocumentData`() {
    val builder = Barber.Builder()
      .installDocument<TransactionalEmailDocument>()
    val exception = assertFailsWith<BarberException> {
      builder
        .installDocumentTemplate<SenderReceipt>(recipientReceiptSmsDocumentTemplate)
        .build()
    }
    Assertions.assertThat(exception.problems).containsExactly("""
      |Attempted to install DocumentTemplate with a DocumentData not specific in the DocumentTemplate source.
      |DocumentTemplate.source: class com.squareup.barber.examples.RecipientReceipt
      |DocumentData: class com.squareup.barber.examples.SenderReceipt""".trimMargin())
  }
}