package com.squareup.cash.barber

import com.squareup.cash.barber.actions.BarberTestingModule.Companion.SERVICE_NAME
import com.squareup.cash.barber.service.applicationModules
import com.squareup.skim.ServiceBuilder
import misk.environment.Environment
import org.junit.jupiter.api.Test

/** Tests if guice bindings are setup properly in all environments. */
internal class InjectorTest {
  @Test fun development() {
    test(Environment.DEVELOPMENT)
  }

  @Test fun staging() {
    test(Environment.STAGING)
  }

  @Test fun production() {
    test(Environment.PRODUCTION)
  }

  private fun test(env : Environment) {
    ServiceBuilder.getTestVerifier(SERVICE_NAME, env, ::applicationModules).verify()
  }
}
