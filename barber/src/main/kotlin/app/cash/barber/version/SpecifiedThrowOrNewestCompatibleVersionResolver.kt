package app.cash.barber.version

import app.cash.barber.BarberException

/**
 * If version is specified and compatible, use it, else throw
 * If version is not specified, use the newest compatible version
 */
object SpecifiedThrowOrNewestCompatibleVersionResolver : VersionResolver() {
  override fun resolveImpl(
    version: Long?,
    compatibleOptions: Set<Long>,
    templateToken: String
  ): Long = when {
    version != null && compatibleOptions.contains(version) -> version
    version != null && !compatibleOptions.contains(version) ->
      throw BarberException(
        errors = listOf(
          """
          |Unable to resolve compatible DocumentTemplate [version=$version] for [templateToken=$templateToken]
          |[compatibleOptions=$compatibleOptions]
          """.trimMargin()
        )
      )
    version == null -> compatibleOptions.maxOrNull()!!
    else -> throw BarberException(
      errors = listOf(
        """
        |Unable to resolve version for inputs 
        |[version=$version]
        |[compatibleOptions=$compatibleOptions]
        |[templateToken=$templateToken]
        """.trimMargin()
      )
    )
  }
}