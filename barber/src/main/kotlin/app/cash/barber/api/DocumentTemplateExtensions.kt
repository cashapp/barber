package app.cash.barber.api

import app.cash.protos.barber.api.DocumentTemplate

fun DocumentTemplate.prettyPrint() = "DocumentTemplate: [templateToken=$template_token][locale=$locale][version=$version]"

