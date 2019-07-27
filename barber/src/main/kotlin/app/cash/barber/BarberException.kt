package app.cash.barber

/**
 * @return problems: a list of Strings describing the error
 * Can point to multiple problems
 *
 * Example
 * |output field 'subject' uses 'totally_invalid_field' but 'RecipientReceipt' has no such field
 * |  {sender} sent you {totally_invalid_field}
 * |  valid fields are: sender, amount, cancelUrl, deposit_expected_at
 *
 * TODO make exceptions typed so the above example could be parsed by a client and show rich feedback
 *  for DocumentTemplate writers
 */
class BarberException(
  val problems: List<String>
) : IllegalStateException() {
  override fun toString(): String {
    val sanitizedProblems = problems
      .map { it.replace("$", "::") }
      .mapIndexed { index, s -> "${index + 1}) $s\n" }
      .joinToString("\n")
    return """
      |Problems
      |$sanitizedProblems
    """.trimMargin()
  }
}