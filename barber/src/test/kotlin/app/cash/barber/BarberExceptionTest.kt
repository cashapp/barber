package app.cash.barber

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BarberExceptionTest {
  @Test
  fun `Pretty toString Formatting`() {
    val exception = assertFailsWith<BarberException> {
      throw BarberException(errors = listOf("""
        |Alpha is unset
        |Details
        |More Details
      """.trimMargin(), """
        |Bravo is unset
        |Details
        |More Details
      """.trimMargin()),
        warnings = listOf("""
        |Charlie is unset
        |Details
        |More Details
      """.trimMargin()))
    }
    assertEquals("""
      |Errors
      |1) Alpha is unset
      |Details
      |More Details
      |
      |2) Bravo is unset
      |Details
      |More Details
      |
      |Warnings
      |1) Charlie is unset
      |Details
      |More Details
      |
    """.trimMargin(), exception.toString())
  }
}