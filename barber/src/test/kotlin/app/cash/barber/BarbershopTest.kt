package app.cash.barber

import app.cash.barber.examples.MultiVersionDocumentTargetChangeDocumentData
import app.cash.barber.examples.RecipientReceipt
import app.cash.barber.examples.SenderReceipt
import app.cash.barber.examples.TransactionalEmailDocument
import app.cash.barber.examples.TransactionalSmsDocument
import app.cash.barber.examples.multiVersionDocumentTarget_v4_email
import app.cash.barber.examples.multiVersionDocumentTarget_v3_emailSms
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_CA
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_GB
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_US
import app.cash.barber.examples.recipientReceiptSmsEmailDocumentTemplateEN_US
import app.cash.barber.examples.senderReceiptEmailDocumentTemplateEN_US
import app.cash.barber.models.BarberKey
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BarbershopTest {
  @Test
  fun `getBarber happy path`() {
    val barbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .build()
    val barber = barbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()
    assertEquals("app.cash.barber.RealBarber", barber::class.qualifiedName)
  }

  @Test
  fun `getAllBarbers happy path`() {
    val barbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_CA)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_GB)
      .build()
    val barbers = barbershop.getAllBarbers()
    assertEquals(1, barbers.size)
    assertThat(barbers.keys).contains(BarberKey(RecipientReceipt::class, TransactionalSmsDocument::class))
  }

  @Test
  fun `getBarber fail on non-target Document, with Document installed`() {
    val barber = BarbershopBuilder()
      .installDocumentTemplate<SenderReceipt>(senderReceiptEmailDocumentTemplateEN_US)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .installDocument<TransactionalSmsDocument>()
      .installDocument<TransactionalEmailDocument>()
      .build()

    val exception = assertFailsWith<BarberException> {
      barber.getBarber<RecipientReceipt, TransactionalEmailDocument>()
    }
    assertEquals(
      """
        |Errors
        |1) Failed to get Barber<app.cash.barber.examples.TransactionalEmailDocument>(templateToken=recipientReceipt)
        |Requested Document [class app.cash.barber.examples.TransactionalEmailDocument] is installed
        |Requested DocumentData [templateToken=recipientReceipt] is installed
        |DocumentTemplate with [templateToken=recipientReceipt] does not have target=[app.cash.barber.examples.TransactionalEmailDocument]
        |
      """.trimMargin(),
      exception.toString())
  }

  @Test
  fun `getBarber fail on non-target Document, with Document not installed`() {
    val barber = BarbershopBuilder()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .installDocument<TransactionalSmsDocument>()
      .build()

    val exception = assertFailsWith<BarberException> {
      barber.getBarber<RecipientReceipt, TransactionalEmailDocument>()
    }
    assertEquals(
      """
        |Errors
        |1) Failed to get Barber<app.cash.barber.examples.TransactionalEmailDocument>(templateToken=recipientReceipt)
        |Document [class app.cash.barber.examples.TransactionalEmailDocument] is not installed in Barbershop
        |
      """.trimMargin(),
      exception.toString())
  }

  @Test
  fun `getTargetDocuments happy path`() {
    val barbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .build()
    val supportedDocuments = barbershop.getTargetDocuments<RecipientReceipt>()
    assertThat(supportedDocuments).containsOnly(TransactionalSmsDocument::class)
  }

  @Test
  fun `getTargetDocuments multiple supported documents`() {
    val barbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocument<TransactionalEmailDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsEmailDocumentTemplateEN_US)
      .build()
    val supportedDocuments = barbershop.getTargetDocuments<RecipientReceipt>()
    assertThat(supportedDocuments).containsOnly(
      TransactionalSmsDocument::class,
      TransactionalEmailDocument::class
    )
  }

  @Test
  fun `getTargetDocuments no supported documents`() {
    val barbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .build()
    val supportedDocuments = barbershop.getTargetDocuments<SenderReceipt>()
    assertThat(supportedDocuments).isEmpty()
  }

  @Test
  fun `getTargetDocuments returns version aware targets`() {
    val barbershop = BarbershopBuilder()
      .installDocument<TransactionalEmailDocument>()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<MultiVersionDocumentTargetChangeDocumentData>(multiVersionDocumentTarget_v3_emailSms)
      .installDocumentTemplate<MultiVersionDocumentTargetChangeDocumentData>(multiVersionDocumentTarget_v4_email)
      .build()

    val targetDocumentsLatestVersion = barbershop.getTargetDocuments<MultiVersionDocumentTargetChangeDocumentData>()
    assertEquals(setOf(TransactionalEmailDocument::class), targetDocumentsLatestVersion)

    val targetDocumentsVersion1 = barbershop.getTargetDocuments<MultiVersionDocumentTargetChangeDocumentData>(multiVersionDocumentTarget_v3_emailSms.version)
    assertEquals(setOf(TransactionalEmailDocument::class, TransactionalSmsDocument::class), targetDocumentsVersion1)
    val targetDocumentsVersion2 = barbershop.getTargetDocuments<MultiVersionDocumentTargetChangeDocumentData>(multiVersionDocumentTarget_v4_email.version)
    assertEquals(setOf(TransactionalEmailDocument::class), targetDocumentsVersion2)
  }

  @Test
  fun `toBuilder happy path`() {
    val barbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_CA)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_GB)
      .build()
    assertThat(barbershop.newBuilder().build().getAllBarbers().keys)
      .containsExactlyElementsOf(barbershop.getAllBarbers().keys)
  }
}
