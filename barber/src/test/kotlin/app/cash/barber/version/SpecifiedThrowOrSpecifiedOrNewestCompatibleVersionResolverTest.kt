package app.cash.barber.version

import app.cash.barber.BarberException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SpecifiedThrowOrSpecifiedOrNewestCompatibleVersionResolverTest {
  private val resolver = SpecifiedThrowOrNewestCompatibleVersionResolver
  private val compatibleOptions = setOf(3L, 5L, 7L, 9L)
  private val templateToken = "templateToken"

  @Test
  fun `null version resolves newest compatible`() {
    val nullVersion = resolver.resolve(null, compatibleOptions, templateToken)
    assertEquals(9L, nullVersion)
  }

  @Test
  fun `specific compatible is resolved`() {
    val specificCompatible = resolver.resolve(7L, compatibleOptions, templateToken)
    assertEquals(7L, specificCompatible)
  }

  @Test
  fun `specific incompatible throws`() {
    val specificNonCompatibleException = assertFailsWith<BarberException> {
      resolver.resolve(2L, compatibleOptions, templateToken)
    }
    assertEquals(
      """
      |Errors
      |1) Unable to resolve compatible DocumentTemplate [version=2] for [templateToken=templateToken]
      |[compatibleOptions=[3, 5, 7, 9]]
      |
    """.trimMargin(), specificNonCompatibleException.toString()
    )
  }

  @Test
  fun `invalid version throws`() {
    val invalidVersionException = assertFailsWith<BarberException> {
      resolver.resolve(10L, compatibleOptions, templateToken)
    }
    assertEquals(
      """
      |Errors
      |1) Unable to resolve compatible DocumentTemplate [version=10] for [templateToken=templateToken]
      |[compatibleOptions=[3, 5, 7, 9]]
      |
    """.trimMargin(), invalidVersionException.toString()
    )
  }

  @Test
  fun `empty compatible options`() {
    val emptyCompatibleOptionsException = assertFailsWith<BarberException> {
      resolver.resolve(null, setOf(), templateToken)
    }
    assertEquals(
      """
      |Errors
      |1) No compatible versions to resolve from
      |
    """.trimMargin(), emptyCompatibleOptionsException.toString()
    )
  }
}