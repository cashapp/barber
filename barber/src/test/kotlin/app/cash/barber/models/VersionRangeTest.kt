package app.cash.barber.models

import app.cash.barber.models.VersionRange.Companion.supports
import app.cash.barber.models.VersionRange.Companion.asVersionRanges
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VersionRangeTest {
  @Test
  fun `happy path`() {
    val versions1 = setOf(1L, 2L, 3L, 4L, 8L, 10L, 11L, 12L, 16L, 17L)
    val expected1 = setOf(
        VersionRange(1L, 4L),
        VersionRange(8L, 8L),
        VersionRange(10L, 12L),
        VersionRange(16L, 17L),
    )
    val actual1 = versions1.asVersionRanges()
    assertEquals(expected1, actual1)

    val versions2 = setOf(1L, 2L, 3L)
    val expected2 = setOf(
        VersionRange(1L, 3L),
    )
    val actual2 = versions2.asVersionRanges()
    assertEquals(expected2, actual2)

    val versions3 = setOf(1L)
    val expected3 = setOf(
        VersionRange(1L, 1L),
    )
    val actual3 = versions3.asVersionRanges()
    assertEquals(expected3, actual3)

    val versions4 = setOf(1L, 4L, 7L)
    val expected4 = setOf(
        VersionRange(1L, 1L),
        VersionRange(4L, 4L),
        VersionRange(7L, 7L),
    )
    val actual4 = versions4.asVersionRanges()
    assertEquals(expected4, actual4)
  }

  @Test
  fun `supports happy path`() {
    val ranges1 = setOf(
        VersionRange(1L, 4L),
        VersionRange(8L, 8L),
        VersionRange(10L, 12L),
        VersionRange(16L, 17L),
    )
    assertTrue(ranges1.supports(1L))
    assertTrue(ranges1.supports(3L))
    assertTrue(ranges1.supports(4L))
    assertTrue(ranges1.supports(8L))
    assertTrue(ranges1.supports(10L))
    assertTrue(ranges1.supports(12L))
    assertTrue(ranges1.supports(16L))
    assertTrue(ranges1.supports(17L))
    assertFalse(ranges1.supports(5L))
    assertFalse(ranges1.supports(7L))
    assertFalse(ranges1.supports(9L))
    assertFalse(ranges1.supports(13L))
    assertFalse(ranges1.supports(15L))
    assertFalse(ranges1.supports(18L))
    assertFalse(ranges1.supports(20L))

    val ranges2 = setOf(
        VersionRange(1L, 1L),
    )
    assertTrue(ranges2.supports(1L))
    assertFalse(ranges2.supports(2L))
  }

  @Test
  fun `min gt max`() {
    val exception = assertFailsWith<IllegalStateException> {
      VersionRange(3L, 1L)
    }
    assertEquals("java.lang.IllegalStateException: Invalid VersionRange [min=3] must be <= [max=1]", exception.toString())
  }
}