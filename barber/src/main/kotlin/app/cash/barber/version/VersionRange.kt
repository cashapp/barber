package app.cash.barber.version

data class VersionRange(
  val min: Long,
  val max: Long
) {
  init {
    check(min <= max) {
      "Invalid VersionRange [min=$min] must be <= [max=$max]"
    }
  }

  fun supports(version: Long) = version in min..max

  companion object {
    fun Set<Long>.asVersionRanges(): Set<VersionRange> {
      val versionRanges = mutableSetOf<VersionRange>()
      var localMin = -1L
      var localMax = -1L
      this.sorted().forEach { version ->
        when {
          localMin == -1L && localMax == -1L -> {
            localMin = version
            localMax = version
          }
          localMax == version - 1 -> localMax = version
          localMax < version - 1 -> {
            versionRanges.add(
                VersionRange(localMin, localMax)
            )
            localMin = version
            localMax = version
          }
        }
      }
      if (localMin != -1L && localMax != -1L) {
        versionRanges.add(
            VersionRange(localMin, localMax)
        )
      }
      return versionRanges.toSet()
    }

    fun Set<VersionRange>.supports(version: Long) = any {
      it.supports(version)
    }
  }
}