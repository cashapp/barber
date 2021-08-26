package app.cash.barber.models

import java.util.Optional

fun <T> Optional<T>.getNullable(): T? {
  return if (this.isEmpty) {
    null
  } else {
    this.get()
  }
}
