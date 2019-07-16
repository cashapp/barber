package app.cash.barber.examples

import app.cash.barber.models.DocumentData
import java.time.Instant

data class RecipientReceipt(
  val sender: String,
  val amount: String, // TODO: Money
  val cancelUrl: String, // TODO: HttpUrl
  val deposit_expected_at: Instant
) : DocumentData

val sandy50Receipt = RecipientReceipt(
  sender = "Sandy Winchester",
  amount = "$50",
  cancelUrl = "https://cash.app/cancel/123",
  deposit_expected_at = Instant.parse("2019-05-21T16:02:00.00Z")
)