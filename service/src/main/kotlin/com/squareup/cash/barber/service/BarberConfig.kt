package com.squareup.cash.barber.service

import com.squareup.skim.config.SkimServiceConfig
import misk.client.HttpClientsConfig
import misk.config.Config
import misk.web.WebConfig

/** Configuration for barber. */
data class BarberConfig(
  val skim: SkimServiceConfig
) : Config