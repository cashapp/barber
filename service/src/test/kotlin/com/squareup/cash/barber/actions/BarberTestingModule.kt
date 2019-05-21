package com.squareup.cash.barber.actions

import com.squareup.cash.barber.service.BarberConfig
import com.squareup.skim.config.SkimConfig
import misk.MiskTestingServiceModule
import misk.config.ConfigModule
import misk.environment.Environment
import misk.environment.EnvironmentModule
import misk.inject.KAbstractModule
import misk.logging.LogCollectorModule

/** Configures testing modules. */
internal class BarberTestingModule : KAbstractModule() {
  override fun configure() {
    val config: BarberConfig = SkimConfig.load(SERVICE_NAME, Environment.TESTING)

    install(ConfigModule.create(SERVICE_NAME, config))
    install(EnvironmentModule(Environment.TESTING))
    install(LogCollectorModule())
    install(BarberWebActionModule())
    install(MiskTestingServiceModule())
  }

  companion object {
    internal const val SERVICE_NAME = "barber"
  }
}
