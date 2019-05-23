package com.squareup.barber

import com.squareup.barber.examples.TransactionalEmailDocumentSpec
import com.squareup.barber.examples.TransactionalSmsDocumentSpec
import com.squareup.barber.models.CopyModel
import com.squareup.barber.models.DocumentCopy
import com.squareup.barber.models.Locale
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertFailsWith

class BarberTest {
  lateinit var barber: Barber

  @BeforeEach
  fun before() {
    barber = BarberImpl()
  }

  @Test
  fun installCopy() {
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
    barber.installDocumentSpec<TransactionalEmailDocumentSpec>()
    barber.installCopy<RecipientReceipt>(recipientReceiptDocumentCopy)
  }

  @Test
  fun installCopyFailsOnMissingDocumentSpec() {
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
    val exception = assertFailsWith<BarberException> {
      barber.installCopy<RecipientReceipt>(recipientReceiptDocumentCopy)
    }
    assertThat(exception.problems).containsExactly("""
      |Attempted to install DocumentCopy without the corresponding DocumentSpec being installed.
      |Not installed DocumentCopy.targets:
      |[class com.squareup.barber.examples.TransactionalEmailDocumentSpec]""".trimMargin())
  }

  @Test
  fun installCopyFailsOnMismatchCopyModel() {
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
    barber.installDocumentSpec<TransactionalEmailDocumentSpec>()
    val exception = assertFailsWith<BarberException> {
      barber.installCopy<SenderReceipt>(recipientReceiptDocumentCopy)
    }
    assertThat(exception.problems).containsExactly("""
      |Attempted to install DocumentCopy with a CopyModel not specific in the DocumentCopy source.
      |DocumentCopy.source: class com.squareup.barber.RecipientReceipt
      |CopyModel: class com.squareup.barber.SenderReceipt""".trimMargin())
  }

  @Test
  fun happyPathSms() {
    val recipientReceipt = RecipientReceipt(
      sender = "Sandy Winchester",
      amount = "$50",
      cancelUrl = "https://cash.app/cancel/123",
      deposit_expected_at = Instant.parse("2019-05-21T16:02:00.00Z")
    )

    val recipientReceiptDocumentCopy = DocumentCopy(
      fields = mapOf(
        "subject" to "{{sender}} sent you {{amount}}",
        "headline" to "You received {{amount}}",
        "short_description" to "You’ve received a payment from {{sender}}! The money will be in your bank account " +
          "{{deposit_expected_at_casual}}.",
        "primary_button" to "Cancel this payment",
        "primary_button_url" to "{{cancelUrl}}",
        "sms_body" to "{{sender}} sent you {{amount}}"
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalSmsDocumentSpec::class),
      locale = Locale.EN_US
    )

    barber.installDocumentSpec<TransactionalSmsDocumentSpec>()
    barber.installCopy<RecipientReceipt>(recipientReceiptDocumentCopy)

    val spec = barber.render<TransactionalSmsDocumentSpec>(recipientReceipt)

    assertThat(spec).isEqualTo(
      TransactionalSmsDocumentSpec(
        sms_body = "Sandy Winchester sent you $50"
      )
    )
  }

  @Test
  fun happyPathEmail() {
    val recipientReceipt = RecipientReceipt(
      sender = "Sandy Winchester",
      amount = "$50",
      cancelUrl = "https://cash.app/cancel/123",
      deposit_expected_at = Instant.parse("2019-05-21T16:02:00.00Z")
    )

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

    barber.installDocumentSpec<TransactionalEmailDocumentSpec>()
    barber.installCopy<RecipientReceipt>(recipientReceiptDocumentCopy)

    val spec = barber.render<TransactionalEmailDocumentSpec>(recipientReceipt)

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
    val recipientReceipt = RecipientReceipt(
      sender = "Sandy Winchester",
      amount = "$50",
      cancelUrl = "https://cash.app/cancel/123",
      deposit_expected_at = Instant.parse("2019-05-21T16:02:00.00Z")
    )

    val recipientReceiptDocumentCopy = DocumentCopy(
      fields = mapOf(
        "subject" to "{{sender}} sent you {{amount}}",
        "headline" to "You received {{amount}}",
        "short_description" to "You’ve received a payment from {{sender}}! The money will be in your bank account " +
          "{{deposit_expected_at_casual}}.",
        "primary_button" to "Cancel this payment",
        "primary_button_url" to "{{cancelUrl}}"
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalEmailDocumentSpec::class),
      locale = Locale.EN_US
    )

    barber.installDocumentSpec<TransactionalEmailDocumentSpec>()
    barber.installCopy<RecipientReceipt>(recipientReceiptDocumentCopy)

    val spec = barber.render<TransactionalEmailDocumentSpec>(recipientReceipt)

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
      barber.installCopy<RecipientReceipt>(recipientReceiptDocumentCopy)
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
      barber.installCopy<RecipientReceipt>(recipientReceiptDocumentCopy)
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
      barber.installCopy<RecipientReceipt>(recipientReceiptDocumentCopy)
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

    barber.installCopy<RecipientReceipt>(recipientReceiptDocumentCopy)

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

    barber.installCopy<RecipientReceipt>(recipientReceiptDocumentCopy)

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
