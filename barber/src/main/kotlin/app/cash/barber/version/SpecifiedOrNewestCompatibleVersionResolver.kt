package app.cash.barber.version

/**
 * If version is specified and compatible, use it
 * Otherwise use the newest compatible version
 */
object SpecifiedOrNewestCompatibleVersionResolver: VersionResolver() {
 override fun resolveImpl(
  version: Long?,
  compatibleOptions: Set<Long>,
  templateToken: String
 ): Long = when {
  version != null && compatibleOptions.contains(version) -> version
  else -> compatibleOptions.maxOrNull()!!
 }
}