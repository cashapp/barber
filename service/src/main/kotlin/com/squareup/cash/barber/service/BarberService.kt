package com.squareup.cash.barber.service

import com.squareup.cash.barber.actions.BarberWebActionModule
import com.squareup.skim.ServiceBuilder
import com.squareup.skim.SkimRealModule
import com.squareup.skim.SkimTestingModule
import misk.config.ConfigModule
import misk.environment.Environment
import misk.environment.EnvironmentModule
import misk.inject.KAbstractModule
import misk.web.metadata.AdminDashboardTestingModule

fun main(args: Array<String>) {
  ServiceBuilder.getMiskApplication(::applicationModules).run(args)
}

fun applicationModules(
  serviceBuilder: ServiceBuilder<BarberConfig>
): List<KAbstractModule> {
  val modules = mutableListOf(
      BarberAccessModule(),
      BarberWebActionModule(),
      ConfigModule.create(serviceBuilder.name, serviceBuilder.config),
      EnvironmentModule(serviceBuilder.env)
  )

  when (serviceBuilder.env) {
    Environment.PRODUCTION, Environment.STAGING -> {
      modules.addAll(listOf(
          SkimRealModule(serviceBuilder.env, serviceBuilder.config.skim)
      ))
    }
    Environment.DEVELOPMENT, Environment.TESTING -> {
      modules.addAll(listOf(
          SkimTestingModule(serviceBuilder.env, serviceBuilder.config.skim),
          AdminDashboardTestingModule(serviceBuilder.env)
      ))
    }
  }

  return modules
}
