package com.squareup.cash.barber.client.testing

import com.squareup.cash.barber.client.BarberClient
import misk.inject.KAbstractModule

/** Module to install to bind BarberClient to a fake instance */
class FakeBarberClientModule : KAbstractModule() {
  override fun configure() {
    bind<BarberClient>().to<FakeBarberClient>()
  }
}
