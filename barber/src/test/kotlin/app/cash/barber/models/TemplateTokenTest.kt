package app.cash.barber.models

import app.cash.barber.examples.SenderReceipt
import app.cash.barber.models.TemplateToken.Companion.getTemplateToken
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TemplateTokenTest {
  @Test
  fun `happy path Kotlin DocumentData`() {
    val actual = SenderReceipt::class.getTemplateToken().token
    val expected = "senderReceipt"
    assertEquals(expected, actual)
  }
}