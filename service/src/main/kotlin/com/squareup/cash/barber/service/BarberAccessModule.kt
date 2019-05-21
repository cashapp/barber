package com.squareup.cash.barber.service

import misk.MiskCaller
import misk.inject.KAbstractModule
import misk.security.authz.AccessAnnotationEntry
import misk.security.authz.DevelopmentOnly
import misk.web.metadata.AdminDashboardAccess

/** Configures access to authenticated web actions and the admin dashboard. */
internal class BarberAccessModule : KAbstractModule() {
  override fun configure() {
    // Give engineers access to the admin dashboard for barber
    multibind<AccessAnnotationEntry>().toInstance(AccessAnnotationEntry<AdminDashboardAccess>(roles = listOf("eng")))

    // Setup authentication in the development environment
    bind<MiskCaller>().annotatedWith<DevelopmentOnly>()
      .toInstance(MiskCaller(user = "development", roles = setOf("eng")))
  }
}
