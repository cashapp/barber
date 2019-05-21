package com.squareup.barber

/**
 * This is a schema that specifies the input values for a DocumentCopy template.
 *
 * Examples:
 * data class RecipientReceipt(
 *  val amount: Money,
 *  val sender: String,
 *  val depositExpectedAt: Instant,
 *  val cancelUrl: HttpUrl
 * ) : CopyModel
 *
 * data class AutoAddCashFailed(
 *  val amount: Money,
 *  val reason: String
 * ) : CopyModel
 *
 * data class PaymentLateHelpArticle(
 *  val amount: Money,
 *  val displayId: String,
 *  val depositExpectedAt: Instant
 * ) : CopyModel
 *
 * Instances of CopyModel are used to fill the templates in a DocumentCopy and produce a DocumentSpec object.
 *
 * A SpecRenderer function consumes the DocumentSpec object and renders the final document, SMS, email, or article.
 *
 * Copy models do not have a locale.
 */
interface CopyModel