package app.cash.barber.examples

import app.cash.barber.models.DocumentData

data class InvestmentPurchase(
  val shares: String,
  val ticker: String,
  val url: String
) : DocumentData

val mcDonaldsInvestmentPurchaseUrl = "https://cash.app/go/InvestConfirmAction_input?strOrigTrackNum=9400111899561379412019"
val mcDonaldsInvestmentPurchase = InvestmentPurchase(
  shares = "100",
  ticker = "McDonald's",
  url = mcDonaldsInvestmentPurchaseUrl
)
