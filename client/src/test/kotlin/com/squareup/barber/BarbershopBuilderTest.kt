package com.squareup.barber

import com.squareup.barber.examples.RecipientReceipt
import com.squareup.barber.examples.SenderReceipt
import com.squareup.barber.examples.TransactionalEmailDocument
import com.squareup.barber.examples.TransactionalSmsDocument
import com.squareup.barber.examples.recipientReceiptSmsDocumentTemplateEN_CA
import com.squareup.barber.examples.recipientReceiptSmsDocumentTemplateEN_GB
import com.squareup.barber.examples.recipientReceiptSmsDocumentTemplateEN_US
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
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
    Assertions.assertThat(exception.problems).containsExactly("""
      |Attempted to install DocumentTemplate without the corresponding Document being installed.
      |Not installed DocumentTemplate.targets:
      |[class com.squareup.barber.examples.TransactionalSmsDocument]""".trimMargin())
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
    Assertions.assertThat(exception.problems).containsExactly("""
      |Attempted to install DocumentTemplate with a DocumentData not specific in the DocumentTemplate source.
      |DocumentTemplate.source: class com.squareup.barber.examples.RecipientReceipt
      |DocumentData: class com.squareup.barber.examples.SenderReceipt""".trimMargin())
  }
}