package app.cash.barber.models

import app.cash.barber.examples.SenderReceipt
import app.cash.barber.models.TemplateToken.Companion.getTemplateToken
import app.cash.protos.barber.api.DocumentData
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TemplateTokenTest {
  @Test
  fun `happy path Kotlin DocumentData`() {
    val actual = SenderReceipt::class.getTemplateToken().token
    val expected = "senderReceipt"
    assertEquals(expected, actual)
  }

  @Test
  fun `happy path Proto DocumentData`() {
    val actual = DocumentData(template_token = "alphaBravo").getTemplateToken()
    val expected = TemplateToken("alphaBravo")
    assertEquals(expected, actual)
  }
}