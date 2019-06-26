package app.cash.barber.models

/**
 * A wrapper around a String that contains HTML content that should not be escaped when rendered
 * By default and for safety, all Strings are escaped.
 */
data class HtmlString(val html: String)