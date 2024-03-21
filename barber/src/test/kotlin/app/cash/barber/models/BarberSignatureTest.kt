package app.cash.barber.models

import app.cash.barber.examples.EmptyDocumentData
import app.cash.barber.examples.NestedLoginCode
import app.cash.barber.examples.NullableCashBalanceReceipt
import app.cash.barber.examples.SenderReceipt
import app.cash.barber.examples.TransactionalEmailDocument
import app.cash.barber.examples.TransactionalSmsDocument
import app.cash.barber.examples.TransactionalSmsDocumentWithOptionalMedia
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_US
import app.cash.barber.models.BarberSignature.Companion.getBarberSignature
import app.cash.barber.models.BarberSignature.Companion.getMinimumRequiredSignatureToSatisfy
import app.cash.barber.models.BarberSignature.Companion.getNaiveSourceBarberSignature
import app.cash.protos.barber.api.BarberSignature.Type
import app.cash.protos.barber.api.DocumentData
import com.github.mustachejava.DefaultMustacheFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BarberSignatureTest {
  private val mustacheFactory = DefaultMustacheFactory()

  @Test
  fun `encode happy path`() {
    val fields = mapOf(
      "name1" to Type.STRING,
      "name2" to Type.LONG,
      "name3" to Type.DURATION,
      "name4" to Type.INSTANT
    )
    val actual = BarberSignature(fields)
    assertEquals("name1,1;name2,2;name3,3;name4,4", actual.signature)
  }

  @Test
  fun `decode happy path`() {
    val actual = BarberSignature("name1,1;name2,2;name3,3;name4,4")
    val expected = mapOf(
      "name1" to Type.STRING,
      "name2" to Type.LONG,
      "name3" to Type.DURATION,
      "name4" to Type.INSTANT
    )
    assertEquals(expected, actual.fields)
  }

  @Test
  fun `encode empty path`() {
    val actual = BarberSignature(mapOf<String, Type>())
    assertEquals("", actual.signature)
  }

  @Test
  fun `decode empty path`() {
    val actual = BarberSignature("")
    assertEquals(mapOf(), actual.fields)
  }

  @Test
  fun `no parameter document data signature`() {
    val noParam = EmptyDocumentData()
    val actual = noParam.getBarberSignature()
    assertEquals("", actual.signature)
  }

  @Test
  fun `canSatisfy happy path`() {
    val supersetSource = BarberSignature(
      mapOf(
        "name1" to Type.STRING,
        "name2" to Type.LONG,
        "name3" to Type.DURATION,
        "name4" to Type.INSTANT
      )
    )
    val target = BarberSignature(
      mapOf(
        "name1" to Type.STRING,
        "name2" to Type.LONG,
      )
    )
    val actual = supersetSource.canSatisfy(target)
    assertTrue(actual)
  }

  @Test
  fun `canSatisfy failure path`() {
    val supersetSource = BarberSignature(
      mapOf(
        "name1" to Type.STRING,
        "name2" to Type.LONG,

        )
    )
    val target = BarberSignature(
      mapOf(
        "name1" to Type.STRING,
        "name2" to Type.LONG,
        "name3" to Type.DURATION,
        "name4" to Type.INSTANT
      )
    )
    val actual = supersetSource.canSatisfy(target)
    assertFalse(actual)
  }

  @Test
  fun `canSatisfyNaively happy path`() {
    val supersetSource = BarberSignature(
      mapOf(
        "name1" to Type.STRING,
        "name2" to Type.LONG,
        "name3" to Type.DURATION,
        "name4" to Type.INSTANT
      )
    )
    val target = BarberSignature(
      mapOf(
        "name1" to Type.STRING,
        "name2" to Type.STRING,
      )
    )
    assertTrue(supersetSource.canSatisfyNaively(target))
  }

  @Test
  fun `canSatisfyNaively failure path`() {
    val supersetSource = BarberSignature(
      mapOf(
        "name1" to Type.STRING,
        "name2" to Type.STRING,

        )
    )
    val target = BarberSignature(
      mapOf(
        "name1" to Type.STRING,
        "name2" to Type.LONG,
        "name3" to Type.DURATION,
        "name4" to Type.INSTANT
      )
    )
    assertFalse(supersetSource.canSatisfyNaively(target))
  }

  @Test
  fun `signature from Proto DocumentData`() {
    val actual = DocumentData(
      template_token = "T_123",
      fields = listOf(
        DocumentData.Field(
          key = "recipient",
          value_string = "recipient"
        ),
        DocumentData.Field(
          key = "score",
          value_long = 420
        ),
        DocumentData.Field(
          key = "waiting_time",
          value_duration = Duration.ZERO
        ),
        DocumentData.Field(
          key = "expected_at",
          value_instant = Instant.EPOCH
        )
      )
    ).getBarberSignature()
    val expected = BarberSignature(
      mapOf(
        "recipient" to Type.STRING,
        "score" to Type.LONG,
        "waiting_time" to Type.DURATION,
        "expected_at" to Type.INSTANT,
      )
    )
    assertEquals(expected, actual)
  }

  @Test
  fun `signature from Kotlin DocumentData class`() {
    val actual = SenderReceipt::class.getBarberSignature()
    val expected = BarberSignature(
      mapOf(
        "recipient" to Type.STRING,
        "amount" to Type.STRING,
        "cancelUrl" to Type.STRING,
        "deposit_expected_at" to Type.INSTANT,
      )
    )
    assertEquals(expected, actual)
  }

  @Test
  fun `signature from Kotlin DocumentData`() {
    val actual = SenderReceipt(
      recipient = "test",
      amount = "test",
      cancelUrl = "test",
      deposit_expected_at = Instant.EPOCH
    ).getBarberSignature()
    val expected = BarberSignature(
      mapOf(
        "recipient" to Type.STRING,
        "amount" to Type.STRING,
        "cancelUrl" to Type.STRING,
        "deposit_expected_at" to Type.INSTANT,
      )
    )
    assertEquals(expected, actual)
  }

  @Test
  fun `signature from Document class`() {
    val actual = TransactionalSmsDocument::class.getBarberSignature()
    val expected = BarberSignature(
      mapOf(
        "sms_body" to Type.STRING,
      )
    )
    assertEquals(expected, actual)
  }

  @Test
  fun `signature from Document`() {
    val actual = TransactionalSmsDocument(
      sms_body = "test"
    ).getBarberSignature()
    val expected = BarberSignature(
      mapOf(
        "sms_body" to Type.STRING,
      )
    )
    assertEquals(expected, actual)
  }

  @Test
  fun `signature from Document class with nullable fields`() {
    val actual = TransactionalEmailDocument::class.getBarberSignature()
    val expected = BarberSignature(
      mapOf(
        "subject" to Type.STRING,
        "headline" to Type.STRING,
        "short_description" to Type.STRING,
        "primary_button" to Type.STRING,
        "primary_button_url" to Type.STRING,
        "secondary_button" to Type.STRING,
        "secondary_button_url" to Type.STRING,
      )
    )
    assertEquals(expected, actual)
  }

  @Test
  fun `signature for nested Kotlin DocumentData data class`() {
    val nestedLoginCode = NestedLoginCode(
      code = "123-456",
      button = NestedLoginCode.EmailButton(
        color = "#B3B3B3",
        text = "Login",
        link = "https://cash.app/login",
        size = "regular"
      )
    )
    val actual = nestedLoginCode.getBarberSignature()
    val expected = BarberSignature(
      mapOf(
        "code" to Type.STRING,
        "button.color" to Type.STRING,
        "button.text" to Type.STRING,
        "button.link" to Type.STRING,
        "button.size" to Type.STRING,
      )
    )
    assertEquals(expected, actual)
  }

  @Test
  fun `signature from nested Proto DocumentData`() {
    val actual = DocumentData(
      template_token = "T_123",
      fields = listOf(
        DocumentData.Field(
          key = "recipient",
          value_string = "recipient"
        ),
        DocumentData.Field(
          key = "score.metric1",
          value_long = 420
        ),
        DocumentData.Field(
          key = "score.metric2",
          value_long = 69
        ),
      )
    ).getBarberSignature()
    val expected = BarberSignature(
      mapOf(
        "recipient" to Type.STRING,
        "score.metric1" to Type.LONG,
        "score.metric2" to Type.LONG,
      )
    )
    assertEquals(expected, actual)
  }

  @Test
  fun `signature for nested Kotlin DocumentData data class converted to Proto`() {
    val nestedLoginCode = NestedLoginCode(
      code = "123-456",
      button = NestedLoginCode.EmailButton(
        color = "#B3B3B3",
        text = "Login",
        link = "https://cash.app/login",
        size = "regular"
      )
    )
    val nestedProto = nestedLoginCode.toProto()
    val actual = nestedProto.getBarberSignature()
    val expected = BarberSignature(
      mapOf(
        "code" to Type.STRING,
        "button.color" to Type.STRING,
        "button.text" to Type.STRING,
        "button.link" to Type.STRING,
        "button.size" to Type.STRING,
      )
    )
    assertEquals(expected, actual)
  }

  @Test
  fun `implicit source signature for DocumentTemplate proto`() {
    val actual = recipientReceiptSmsDocumentTemplateEN_US
      .toProto()
      .getNaiveSourceBarberSignature(mustacheFactory)
    val expected = BarberSignature(
      mapOf(
        "sender" to Type.STRING,
        "amount" to Type.STRING,
        "cancelUrl" to Type.STRING,
        "deposit_expected_at" to Type.STRING,
      )
    )
    assertEquals(expected, actual)
  }

  @Test
  fun `Use value_null_type or default to String for null DocumentData Fields when Signature calculation is ambiguous`() {
    val actual = DocumentData(
      template_token = "T_123",
      fields = listOf(
        DocumentData.Field(
          key = "alpha",
          value_string = "non null"
        ),
        DocumentData.Field(
          key = "bravo",
          value_null_type = Type.DURATION
        ),
        DocumentData.Field(
          key = "charlie"
        )
      )
    ).getBarberSignature()
    val expected = BarberSignature(
      mapOf(
        "alpha" to Type.STRING,
        "bravo" to Type.DURATION,
        "charlie" to Type.STRING,
      )
    )
    assertEquals(expected, actual)
  }

  @Test
  fun `Kotlin DocumentData signature fills in value_null_type for null value`() {
    val documentData = NullableCashBalanceReceipt(
      amount = 50
    )

    val documentDataProto = documentData.toProto()
    assertEquals(Type.LONG, documentDataProto.fields.last().value_null_type)

    val kotlinSignature = documentData.getBarberSignature()
    val protoSignature = documentDataProto.getBarberSignature()
    val expected = BarberSignature(
      mapOf(
        "amount" to Type.LONG,
        "cashBalance" to Type.LONG,
      )
    )
    assertEquals(expected, kotlinSignature)
    assertEquals(expected, protoSignature)
  }

  @Test
  fun `getMinimumRequiredSignatureToSatisfy does not include optional fields`() {
    assertThat(TransactionalSmsDocumentWithOptionalMedia::class.getMinimumRequiredSignatureToSatisfy())
      .isEqualTo(BarberSignature(mapOf("sms_body" to Type.STRING)))
  }
}
