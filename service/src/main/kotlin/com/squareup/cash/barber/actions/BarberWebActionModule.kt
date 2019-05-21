package com.squareup.cash.barber.actions

import misk.inject.KAbstractModule
import misk.web.WebActionModule

/** Configures web actions by binding [WebActionEntry]s. */
internal class BarberWebActionModule : KAbstractModule() {
  override fun configure() {
    install(WebActionModule.create<PingAction>())
  }
}
