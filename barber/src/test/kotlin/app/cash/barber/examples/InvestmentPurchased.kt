package app.cash.barber.examples

import app.cash.barber.models.DocumentData

data class InvestmentPurchase(
  val shares: String,
  val ticker: String
) : DocumentData

val mcDonaldsInvestmentPurchase = InvestmentPurchase(
    shares = "100",
    ticker = "McDonald's"
)
