package com.squareup.cash.barber.client

/** Responsible for building the request and parsing the response from the barber api. */
interface BarberClient {
  fun ping(message: String): String
}