package com.squareup.cash.barber.client.testing

import com.squareup.cash.barber.client.BarberClient
import javax.inject.Singleton

/** A fake client used by tests. The fake client is an open class to allow for use of spies and mocks. */
@Singleton
open class FakeBarberClient : BarberClient {
  override fun ping(message: String): String =  "pong $message"
}