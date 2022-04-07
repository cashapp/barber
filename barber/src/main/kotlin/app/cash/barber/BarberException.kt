package app.cash.barber

/**
 * An error, typically presented at application startup, that describes an inconsistency between the
 * input document data types, the templates, and the target document types.
 *
 * For example this exception is raised if a document data lacks an input field that a template
 * expects, or if a template lacks an output field that a document expects.
 *
 * Example:
 *
 * ```
 * Problems
 * 1) Attempted to install DocumentTemplate with a DocumentData not specified in the DocumentTemplate source.
 * DocumentTemplate.source: class app.cash.barber.examples.RecipientReceipt
 * DocumentData: class app.cash.barber.examples.SenderReceipt
 *
 * 2) Missing variable [sender] in DocumentData [class app.cash.barber.examples.SenderReceipt] for DocumentTemplate field [{{sender}} sent you {{amount}}. It will be available at {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}]
 *
 * 3) Unused DocumentData variable [recipient] in [class app.cash.barber.examples.SenderReceipt] with no usage in installed DocumentTemplate Locales:
 * [Locale=en-US]
 * ```
 */
// TODO make exceptions typed so the above example could be parsed by a client and show rich
//  feedback for DocumentTemplate writers
class BarberException(
  val errors: List<String> = listOf(),
  val warnings: List<String> = listOf()
) : IllegalStateException(generateMessage(errors, warnings)) {

  override fun toString(): String {
    return generateMessage(errors, warnings)
  }

  companion object {
    /**
     * Throwing early makes debugging simpler for Barber developers as the above simple warnings
     * can be raised before a flood of other errors below fail as a result of the above
     */
    fun maybeThrowBarberException(errors: List<String>, warnings: List<String>, warningsAsErrors: Boolean) {
      if (errors.isNotEmpty() || (warnings.isNotEmpty() && warningsAsErrors)) {
        throw BarberException(errors = errors, warnings = warnings)
      }
    }

    private fun generateMessage(errors: List<String>, warnings: List<String>): String {
      val exceptionSections = listOf(
          formatExceptionSection("Errors", errors),
          formatExceptionSection("Warnings", warnings)
      )

      val exceptionMessage = exceptionSections
          .filter { it.isNotEmpty() }
          .joinToString("\n")

      return exceptionMessage.ifEmpty {
        "Unknown BarberException"
      }
    }

    private fun formatExceptionSection(sectionTitle: String, entries: List<String>): String {
      return if (entries.isEmpty()) {
        ""
      } else {
        """
        |$sectionTitle
        |${entries.sanitize()}
        """.trimMargin()
      }
    }

    private fun List<String>.sanitize(): String = map { it.replace("$", "::") }
        .mapIndexed { index, s -> "${index + 1}) $s\n" }
        .joinToString("\n")
  }
}
