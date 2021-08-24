package app.cash.barber.api

import app.cash.protos.barber.api.DocumentTemplate

fun DocumentTemplate.Field.prettyPrint() = "Field: [key=${key}][template=$template]"

fun DocumentTemplate.prettyPrint() = "DocumentTemplate: [templateToken=$template_token][locale=$locale][version=$version]"

