package com.squareup.cash.barber.client

import com.google.inject.name.Names
import misk.client.TypedHttpClientModule
import misk.inject.KAbstractModule

/**
 * Module to install to bind a real client to your app.
 * Your app's config must include the http client details
 * to communicate to barber.
 *
 * Note that this is meant for other Misk apps to install.
 * Square DC Java apps have their own style of binding up
 * clients, and can use the RealBarberClient.buildClient API.
 */
class BarberClientModule : KAbstractModule() {
  override fun configure() {
    install(TypedHttpClientModule(
        BarberApi::class, "barber", Names.named("barber")))
    bind<BarberClient>().to<RealBarberClient>()
  }
}
