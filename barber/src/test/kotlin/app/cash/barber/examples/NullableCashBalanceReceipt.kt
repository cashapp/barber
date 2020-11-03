package app.cash.barber.examples

import app.cash.barber.models.DocumentData

data class NullableCashBalanceReceipt(
  val amount: Long,
  val cashBalance: Long? = null
): DocumentData