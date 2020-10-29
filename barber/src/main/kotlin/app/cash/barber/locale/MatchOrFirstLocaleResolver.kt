package app.cash.barber.locale

internal object MatchOrFirstLocaleResolver : LocaleResolver {
  override fun resolve(locale: Locale, options: Set<Locale>): Locale =
      if (options.contains(locale)) {
        locale
      } else {
        options.first()
      }
}
