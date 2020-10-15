package app.cash.barber.examples

import app.cash.barber.models.DocumentData

data class NestedLoginCode(
    val code: String,
    val button: EmailButton
) : DocumentData {
    data class EmailButton(
        val color: String,
        val text: String,
        val link: String,
        val size: String
    )
}