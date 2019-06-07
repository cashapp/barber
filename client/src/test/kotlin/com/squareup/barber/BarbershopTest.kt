package com.squareup.barber

import com.squareup.barber.examples.RecipientReceipt
import com.squareup.barber.examples.TransactionalSmsDocument
import com.squareup.barber.examples.recipientReceiptSmsDocumentTemplateEN_CA
import com.squareup.barber.examples.recipientReceiptSmsDocumentTemplateEN_GB
import com.squareup.barber.examples.recipientReceiptSmsDocumentTemplateEN_US
import com.squareup.barber.models.BarberKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BarbershopTest {
  @Test
  fun `getBarber`() {
    val barbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .build()
    val barber = barbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()
    assertEquals("class com.squareup.barber.RealBarber", barber::class.toString())
    //TODO confirm that the returned Barber is strictly of the requested DocumentData and Document types
  }

  @Test
  fun `getAllBarbers`() {
    val barbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_CA)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_GB)
      .build()
    val barbers = barbershop.getAllBarbers()
    assertEquals(1, barbers.size)
    assertThat(barbers.keys).contains(
      BarberKey(RecipientReceipt::class, TransactionalSmsDocument::class))
  }
}