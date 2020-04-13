package app.cash.barber

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

class BarberExceptionTest {
  @Test
  fun `Pretty toString Formatting`() {
    val exception = assertFailsWith<BarberException> {
      throw BarberException(
          errors = listOf("""
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
          """.trimMargin()
          )
      )
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

  @Test
  fun `null exception`() {
    val exception = assertFailsWith<BarberException> { throw BarberException() }
    assertEquals("""
      |Unknown BarberException
    """.trimMargin(), exception.toString())
  }

  @Test
  fun `exception with message`() {
    val exception = assertFailsWith<BarberException> { throw BarberException(listOf(
        "Failed to get Barber<documentDataClass, documentClass>, unknown error"
    )) }
    assertEquals("""
      |Errors
      |1) Failed to get Barber<documentDataClass, documentClass>, unknown error
      |
    """.trimMargin(), exception.toString())
  }
}
