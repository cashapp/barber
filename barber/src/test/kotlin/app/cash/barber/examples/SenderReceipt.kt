package app.cash.barber.examples

import app.cash.barber.models.DocumentData
import java.time.Instant

data class SenderReceipt(
  val recipient: String,
  val amount: String, // TODO: Money
  val cancelUrl: String, // TODO: HttpUrl
  val deposit_expected_at: Instant
) : DocumentData
