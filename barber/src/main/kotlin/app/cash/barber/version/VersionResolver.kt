package app.cash.barber.version

import app.cash.barber.BarberException

abstract class VersionResolver {
  protected abstract fun resolveImpl(
    version: Long?,
    compatibleOptions: Set<Long>,
    templateToken: String
  ): Long

  fun resolve(version: Long?, compatibleOptions: Set<Long>, templateToken: String): Long {
    throwIfEmpty(compatibleOptions)
    return resolveImpl(version, compatibleOptions, templateToken)
  }

  private fun throwIfEmpty(compatibleOptions: Set<Long>) {
    when {
      compatibleOptions.isEmpty() -> throw BarberException(errors = listOf("No compatible versions to resolve from"))
    }
  }
}