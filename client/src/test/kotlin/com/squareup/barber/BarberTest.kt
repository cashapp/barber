package com.squareup.barber

import com.squareup.barber.examples.RecipientReceipt
import com.squareup.barber.examples.SenderReceipt
import com.squareup.barber.examples.TransactionalEmailDocumentSpec
import com.squareup.barber.examples.TransactionalSmsDocumentSpec
import com.squareup.barber.examples.recipientReceiptSmsDocumentCopy
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class BarberTest {
  @Test
  fun `Install works`() {
    Barber.Builder()
      .installDocumentSpec<TransactionalSmsDocumentSpec>()
      .installCopy<RecipientReceipt>(recipientReceiptSmsDocumentCopy)
      .build()
  }

  @Test
  fun `Install works regardless of order`() {
    Barber.Builder()
      .installCopy<RecipientReceipt>(recipientReceiptSmsDocumentCopy)
      .installDocumentSpec<TransactionalSmsDocumentSpec>()
      .build()
  }

  @Test
  fun `Fails when DocumentCopy targets are not installed DocumentSpecs`() {
    val exception = assertFailsWith<BarberException> {
      Barber.Builder()
        .installCopy<RecipientReceipt>(recipientReceiptSmsDocumentCopy)
        .build()
    }
    Assertions.assertThat(exception.problems).containsExactly("""
      |Attempted to install DocumentCopy without the corresponding DocumentSpec being installed.
      |Not installed DocumentCopy.targets:
      |[class com.squareup.barber.examples.TransactionalSmsDocumentSpec]""".trimMargin())
  }

  @Test
  fun `Fails when DocumentCopy installed with non-source CopyModel`() {
    val builder = Barber.Builder()
      .installDocumentSpec<TransactionalEmailDocumentSpec>()
    val exception = assertFailsWith<BarberException> {
      builder
        .installCopy<SenderReceipt>(recipientReceiptSmsDocumentCopy)
        .build()
    }
    Assertions.assertThat(exception.problems).containsExactly("""
      |Attempted to install DocumentCopy with a CopyModel not specific in the DocumentCopy source.
      |DocumentCopy.source: class com.squareup.barber.examples.RecipientReceipt
      |CopyModel: class com.squareup.barber.examples.SenderReceipt""".trimMargin())
  }
}