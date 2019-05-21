package com.squareup.barber

import com.squareup.barber.examples.TransactionalEmailDocumentSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertFailsWith

class BarberTest {
  @Disabled @Test
  fun happyPath() {
    val recipientReceipt = RecipientReceipt(
      sender = "Sandy Winchester",
      amount = "$50",
      cancelUrl = "https://cash.app/cancel/123",
      deposit_expected_at = Instant.parse("2019-05-21T16:02:00.00Z")
    )

    val recipientReceiptDocumentCopy = DocumentCopy(
      fields = mapOf(
        "subject" to "{sender} sent you {amount}",
        "headline" to "You received {amount}",
        "short_description" to "You’ve received a payment from {sender}! The money will be in your bank account " +
          "{deposit_expected_at_casual}.",
        "primary_button" to "Cancel this payment",
        "primary_button_url" to "{cancelUrl}"
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalEmailDocumentSpec::class),
      locale = Locale.EN_US
    )

    val barber = Barber()
    barber.installCopy(recipientReceiptDocumentCopy)

    val specRenderer = barber.newSpecRenderer(RecipientReceipt::class, TransactionalEmailDocumentSpec::class)
    val spec = specRenderer.render(recipientReceipt)

    assertThat(spec).isEqualTo(
      TransactionalEmailDocumentSpec(
        subject = "Sandy Winchester sent you $50",
        headline = HtmlString("You received $50"),
        short_description = "You’ve received a payment from Sandy Winchester! The money will be in your bank account ",
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
        "subject" to "{sender} sent you {totally_invalid_field}",
        "headline" to "",
        "short_description" to "",
        "primary_button" to "",
        "primary_button_url" to ""
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalEmailDocumentSpec::class),
      locale = Locale.EN_US
    )

    val barber = Barber()
    val exception = assertFailsWith<BarberException> {
      barber.installCopy(recipientReceiptDocumentCopy)
    }
    assertThat(exception.problems).containsExactly("""
        |output field 'subject' uses 'totally_invalid_field' but 'RecipientReceipt' has no such field
        |  {sender} sent you {totally_invalid_field}
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

    val barber = Barber()
    val exception = assertFailsWith<BarberException> {
      barber.installCopy(recipientReceiptDocumentCopy)
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

    val barber = Barber()
    val exception = assertFailsWith<BarberException> {
      barber.installCopy(recipientReceiptDocumentCopy)
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

    val barber = Barber()
    barber.installCopy(recipientReceiptDocumentCopy)

    val exception = assertFailsWith<BarberException> {
      barber.newSpecRenderer(SenderReceipt::class, TransactionalEmailDocumentSpec::class)
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

    val barber = Barber()
    barber.installCopy(recipientReceiptDocumentCopy)

    val exception = assertFailsWith<BarberException> {
      barber.newSpecRenderer(RecipientReceipt::class, SmsDocumentSpec::class)
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
}

data class RecipientReceipt(
  val sender: String,
  val amount: String, // TODO: Money
  val cancelUrl: String, // TODO: HttpUrl
  val deposit_expected_at: Instant
) : CopyModel

data class SenderReceipt(
  val recipient: String,
  val amount: String, // TODO: Money
  val cancelUrl: String, // TODO: HttpUrl
  val deposit_expected_at: Instant
) : CopyModel

data class SmsDocumentSpec(
  val text: String
) : DocumentSpec