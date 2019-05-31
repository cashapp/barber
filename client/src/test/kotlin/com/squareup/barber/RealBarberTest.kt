package com.squareup.barber

import com.squareup.barber.examples.RecipientReceipt
import com.squareup.barber.examples.SenderReceipt
import com.squareup.barber.examples.TransactionalEmailDocumentSpec
import com.squareup.barber.examples.TransactionalSmsDocumentSpec
import com.squareup.barber.examples.recipientReceiptSmsDocumentCopy
import com.squareup.barber.examples.sandy50Receipt
import com.squareup.barber.models.CopyModel
import com.squareup.barber.models.DocumentCopy
import com.squareup.barber.models.Locale
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertFailsWith

class RealBarberTest {
  @Test
  fun happyPathSms() {
    val barber = Barber.Builder()
      .installDocumentSpec<TransactionalSmsDocumentSpec>()
      .installCopy<RecipientReceipt>(recipientReceiptSmsDocumentCopy)
      .build()

    val spec = barber.newRenderer<RecipientReceipt, TransactionalSmsDocumentSpec>()
      .render(sandy50Receipt)

    assertThat(spec).isEqualTo(
      TransactionalSmsDocumentSpec(
        sms_body = "Sandy Winchester sent you $50"
      )
    )
  }

  @Test
  fun renderedSpecIsTypeSafeAndSpecific() {
    val barber = Barber.Builder()
      .installDocumentSpec<TransactionalSmsDocumentSpec>()
      .installCopy<RecipientReceipt>(recipientReceiptSmsDocumentCopy)
      .build()

    val spec = barber.newRenderer<RecipientReceipt, TransactionalSmsDocumentSpec>()
      .render(sandy50Receipt)

    // Spec matches
    assertThat(spec).isEqualTo(
      TransactionalSmsDocumentSpec(
        sms_body = "Sandy Winchester sent you $50"
      )
    )

    // Returned rendered spec is type safely accessible
    assertThat(spec.sms_body).isEqualTo("Sandy Winchester sent you $50")
  }

  @Test
  fun happyPathEmail() {
    val recipientReceiptDocumentCopy = DocumentCopy(
      fields = mapOf(
        "subject" to "{{sender}} sent you {{amount}}",
        "headline" to "You received {{amount}}",
        "short_description" to "You’ve received a payment from {{sender}}! The money will be in your bank account " +
          "{{deposit_expected_at}}.",
        "primary_button" to "Cancel this payment",
        "primary_button_url" to "{{cancelUrl}}"
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalEmailDocumentSpec::class),
      locale = Locale.EN_US
    )

    val barber = Barber.Builder()
      .installDocumentSpec<TransactionalEmailDocumentSpec>()
      .installCopy<RecipientReceipt>(recipientReceiptDocumentCopy)
      .build()

    val spec = barber.newRenderer<RecipientReceipt, TransactionalEmailDocumentSpec>()
      .render(sandy50Receipt)

    assertThat(spec).isEqualTo(
      TransactionalEmailDocumentSpec(
        subject = "Sandy Winchester sent you $50",
        headline = "You received $50",
        short_description = "You’ve received a payment from Sandy Winchester! The money will be in your bank account 2019-05-21T16:02:00Z.",
        primary_button = "Cancel this payment",
        primary_button_url = "https://cash.app/cancel/123",
        secondary_button = null,
        secondary_button_url = null
      )
    )
  }

  @Disabled @Test
  fun fieldStemming() {
    val barber = Barber.Builder()
      .installDocumentSpec<TransactionalEmailDocumentSpec>()
      .installCopy<RecipientReceipt>(recipientReceiptSmsDocumentCopy)
      .build()

    val spec = barber.newRenderer<RecipientReceipt, TransactionalEmailDocumentSpec>()
      .render(sandy50Receipt)

    assertThat(spec).isEqualTo(
      TransactionalEmailDocumentSpec(
        subject = "Sandy Winchester sent you $50",
        headline = "You received $50",
        short_description = "You’ve received a payment from Sandy Winchester! The money will be in your bank account " +
          "in 2 days.",
        primary_button = "Cancel this payment",
        primary_button_url = "https://cash.app/cancel/123",
        secondary_button = null,
        secondary_button_url = null
      )
    )
  }

  @Disabled @Test
  fun copyUsesFieldThatDoesntExist() {
    val recipientReceiptDocumentCopy = DocumentCopy(
      fields = mapOf(
        "subject" to "{{sender}} sent you {{totally_invalid_field}}",
        "headline" to "",
        "short_description" to "",
        "primary_button" to "",
        "primary_button_url" to ""
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalEmailDocumentSpec::class),
      locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      Barber.Builder()
        .installCopy<RecipientReceipt>(recipientReceiptDocumentCopy)
    }
    assertThat(exception.problems).containsExactly("""
        |output field 'subject' uses 'totally_invalid_field' but 'RecipientReceipt' has no such field
        |  {{sender}} sent you {{totally_invalid_field}}
        |  valid fields are [sender, amount, cancelUrl, deposit_expected_at]
        |""".trimMargin()
    )
  }

  // TODO: name the input fields ('sender', 'amount' etc.) that the CopyModel defines
  // TODO: name the output fields ('subject', 'headline' etc.) that the DocuemntSpec has

  @Disabled @Test
  fun copyDoesntOutputFieldThatIsRequired() {
    val recipientReceiptDocumentCopy = DocumentCopy(
      fields = mapOf(
        "subject" to "",
        "headline" to "",
        "primary_button" to "",
        "primary_button_url" to ""
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalEmailDocumentSpec::class),
      locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      Barber.Builder()
        .installCopy<RecipientReceipt>(recipientReceiptDocumentCopy)
    }
    assertThat(exception.problems).containsExactly("""
        |output field 'short_description' is required but was not found
        |""".trimMargin()
    )
  }

  @Disabled @Test
  fun copyOutputsFieldThatIsUnknown() {
    val recipientReceiptDocumentCopy = DocumentCopy(
      fields = mapOf(
        "subject" to "",
        "headline" to "",
        "primary_button" to "",
        "primary_button_url" to "",
        "tertiary_button_url" to ""
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalEmailDocumentSpec::class),
      locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      Barber.Builder()
        .installCopy<RecipientReceipt>(recipientReceiptDocumentCopy)
    }
    assertThat(exception.problems).containsExactly("""
        |output field 'tertiary_button_url' is not used
        |""".trimMargin()
    )
  }

  @Disabled @Test
  fun renderUnknownCopyModel() {
    val recipientReceiptDocumentCopy = DocumentCopy(
      fields = mapOf(
        "subject" to "",
        "headline" to "",
        "short_description" to "",
        "primary_button" to "",
        "primary_button_url" to ""
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalEmailDocumentSpec::class),
      locale = Locale.EN_US
    )

    Barber.Builder()
      .installCopy<RecipientReceipt>(recipientReceiptDocumentCopy)

    val exception = assertFailsWith<BarberException> {
      // TODO confirm failure when render (SenderReceipt::class, TransactionalEmailDocumentSpec::class)
    }

    assertThat(exception.problems).containsExactly("""
        |unknown copy model: SenderReceipt
        |""".trimMargin()
    )
  }

  @Disabled @Test
  fun renderUnknownDocumentSpec() {
    val recipientReceiptDocumentCopy = DocumentCopy(
      fields = mapOf(
        "subject" to "",
        "headline" to "",
        "short_description" to "",
        "primary_button" to "",
        "primary_button_url" to ""
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalEmailDocumentSpec::class),
      locale = Locale.EN_US
    )

    Barber.Builder()
      .installCopy<RecipientReceipt>(recipientReceiptDocumentCopy)

    val exception = assertFailsWith<BarberException> {
      // TODO confirm failure when render (RecipientReceipt::class, SmsDocumentSpec::class)
    }

    assertThat(exception.problems).containsExactly("""
        |unknown document spec: SmsDocumentSpec
        |""".trimMargin()
    )
  }

  @Disabled @Test
  fun singleDocumentCopyHasMultipleTargets() {
    TODO()
  }

  @Disabled @Test
  fun failOnSingleUnit() {
    data class StrangeUnitCopyModel(
      val strange: Unit
    ) : CopyModel

    TODO()
  }
}
