package app.cash.barber.models

fun <T, K, V> Iterable<T>.mapToMaps(entryFactory: (T) -> Map<K, V>): Map<K, V> {
  val result = mutableMapOf<K, V>()
  for (item in this) {
    for (entry in entryFactory(item)) {
      result[entry.key] = entry.value
    }
  }
  return result
}