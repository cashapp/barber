package com.squareup.barber

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BarberExceptionTest {
  @Test
  fun `Pretty toString Formatting`() {
    val exception = assertFailsWith<BarberException> {
      throw BarberException(problems = listOf("""
        |Alpha is unset
        |Details
        |More Details
      """.trimMargin(), """
        |Bravo is unset
        |Details
        |More Details
      """.trimMargin(), """
        |Charlie is unset
        |Details
        |More Details
      """.trimMargin()))
    }
    assertEquals("""
      |Problems
      |1) Alpha is unset
      |Details
      |More Details
      |
      |2) Bravo is unset
      |Details
      |More Details
      |
      |3) Charlie is unset
      |Details
      |More Details
      |
    """.trimMargin(), exception.toString())
  }
}