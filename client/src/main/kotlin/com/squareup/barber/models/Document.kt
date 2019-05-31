package com.squareup.barber.models

/**
 * This is a UI object that has the user-presented strings of a document.
 *
 * It is the output from a DocumentTemplate being rendered with the DocumentTemplate's corresponding input DocumentData.
 *
 * A Document is medium specific (email, SMS, article...) and can either be the final rendered product
 * (as in for SMS), or as the input object for a final rendering and theming step (HTML Mustache emails).
 *
 * Examples
 * barber/test com.squareup.barber.examples.TransactionalEmailDocument
 * barber/test com.squareup.barber.examples.TransactionalSmsDocument
 */
interface Document