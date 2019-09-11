# Barber💈

See the [project website][barber] for documentation and APIs.

A type safe Kotlin JVM library for building up localized, fillable, themed documents using [Mustache](https://mustache.github.io) templating.

# Why?

An application will often have hundreds of user viewable strings to power UI, notifications, emails, and other text based user interactions.

This leads to many questions about the how to achieve the desired functionality of these user viewable strings:

- These strings may want to be personalizable! Thus the string would now have to be a template that can render with a data blob unique to each user to produce a personalized string.
- How can we check that the fillable openings in templates have corresponding values in the data blob?
- How can templates and data blobs be validated at compile time to protect against runtime exceptions or user visible bugs?
- What if we want to pass around not just a single template string, but a collection of template strings for more complex documents?
  - How could we support emails that have a subject, body, recipient, primary button...?
- What about different languages? 
    - Could the same unique data blob could be used to support templates in different languages?
- What about time or money that differ between countries that even share the same language (ie. 12 vs 24 hour time)? 
  - How could that formatting localization be provided?
 

To answer the above questions, we built Barber💈. 

> A type safe Kotlin JVM library for building up localized, fillable, themed documents using [Mustache](https://mustache.github.io) templating.

# Getting Started

To get started using Barber, skim below to understand the different elements used to safely render your localized, fillable, themed documents using [Mustache](https://mustache.github.io) templating.

### Gradle/Maven Artifact

Barber is not yet published publicly. API is subject to change.

## DocumentData

DocumentData is the data that is used to render a template. In Barber, this is defined as a data class making it easy to use in Kotlin. 

```kotlin
// Define DocumentData
data class RecipientReceipt(
  val sender: String,
  val amount: String,
  val cancelUrl: String,
  val deposit_expected_at: Instant
) : DocumentData
```

To render a template, for example `"{{sender}} sent you {{amount}}"`, an instance of the above DocumentData could be passed in to fill the fillable openings in the template.

## Document

The final fields of the output, rendered document. 

For simple documents, this may be a single field.

```kotlin
// Define Document
data class TransactionalSmsDocument(
  val sms_body: String
) : Document
```

For more complex documents that may be used in further processing, there may be multiple fields, which can be nullable.

```kotlin
data class TransactionalEmailDocument(
  val subject: String,
  val headline: String,
  val short_description: String,
  val primary_button: String?,
  val primary_button_url: String?,
  val secondary_button: String?,
  val secondary_button_url: String?
) : Document
```

## DocumentTemplate

A DocumentTemplate is the glue that connects the DocumentData to the Document. It contains:
- fields: 
    - keys fulfill all the non-nullable keys of the `Document` targets
    - values are [Mustache](https://mustache.github.io) templates that are rendered with the passed in source `DocumentData`
- source: `DocumentData` that can support all fillable templates in fields 
- targets: `Document`s that the DocumentTemplate can render to 
- locale: Locale for the language of the fields templates

```kotlin
val recipientReceiptSmsDocumentTemplateEN_US = DocumentTemplate(
  fields = mapOf(
    "sms_body" to "{{sender}} sent you {{amount}}"
  ),
  source = RecipientReceipt::class,
  targets = setOf(TransactionalSmsDocument::class),
  locale = Locale.EN_US
)
```

## Barber<DocumentData, Document>

A Barber is typed to the `DocumentData -> Document` relationship that it knows how to render.

> Amy knows how to cut blonde hair into a Fu Manchu mustache. Joe knows how to trim brown hair into a soul patch mustache.

```kotlin
// A Barber who knows how to render RecipientReceipt data into a TransactionalSmsDocument
val recipientReceiptSms: Barber<RecipientReceipt, TransactionalSmsDocument>
```  

## Barbershop

A Barbershop contains all possible Barbers based on the installed `DocumentData`, `DocumentTemplate`, and `Document`s. 

Each Barber knows how to handle a different combination of `DocumentData -> Document`.

You can call `barbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()` to get the Barber that can handle rendering a `RecipientReceipt` into a `TransactionalSmsDocument`.

```kotlin
// Get a Barber who knows how to render RecipientReceipt data into a TransactionalSmsDocument
val recipientReceiptSms = barbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()
```

> You want a soul patch and have brown hair, get Joe! You want a Fu Manchu and have blonde hair, get Amy!

You can also get a Map of all Barbers using `barbershop.getAllBarbers()`.

```kotlin
// Get all Barbers
val allBarbers: Map<BarberKey, Barber<DocumentData, Document>> = barbershop.getAllBarbers()
```

Note: `BarberKey` is a data class that let's us lookup by both `DocumentData` and `Document`.

## BarbershopBuilder

A Java style Builder that installs all of the above Barber elements and returns a pre-compiled and validated Barbershop.

- Install `DocumentData` and `DocumentTemplate` pairs with `.installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)`.
- Install `Document` with `.installDocument<TransactionalSmsDocument>()`
- Set a custom `LocaleResolver` with `.setLocaleResolver(MapleSyrupOrFirstLocaleResolver())`
- Return the finished Barbershop with `.build()` as the final method call on BarbershopBuilder.

```kotlin
val barbershop = BarbershopBuilder()
  .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
  .installDocument<TransactionalSmsDocument>()
  .build()
```

## Barber<DocumentData, Document>.render(data: DocumentData, locale: Locale)

To render the final `Document`, a Barber requires a `DocumentData`,  used to fill the `DocumentTemplate`, and an output Locale.

First, the Barber uses a `LocaleResolver` to find the best Locale match from installed `DocumentTemplate`s.

Then, using the Locale resolved `DocumentTemplate`, Barber renders the fields of `DocumentTemplate` using the passed in `DocumentData`.

Returned is the requested `Document` rendered with the personalized values of `DocumentData` in the closest match to the requested Locale.

```kotlin
// Get a Barber who knows how to render RecipientReceipt data into a TransactionalSmsDocument
val recipientReceiptSms = barbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()

// Create the RecipientReceipt instance that holds the personalized data
val sandy50Receipt = RecipientReceipt(
  sender = "Sandy Winchester",
  amount = "$50",
  cancelUrl = "https://cash.app/cancel/123",
  deposit_expected_at = Instant.parse("2019-05-21T16:02:00.00Z")
)

// Render the final document using the personalized DocumentData instance and the output Locale
val renderedSms = recipientReceiptSms.render(sandy50Receipt, EN_US)
```

## Locale

Barber supports installation and resolution of multiple Locales for each `DocumentTemplate`.

All Locale versions of a DocumentTemplate will be installed with the BarbershopBuilder.

The desired output Locale is then provided at render time and the best available option is resolved.

```kotlin
// Define DocumentTemplate in English
val recipientReceiptSmsDocumentTemplateEN_US = DocumentTemplate(
  fields = mapOf("sms_body" to "{{sender}} sent you {{amount}}"),
  source = RecipientReceipt::class,
  targets = setOf(TransactionalSmsDocument::class),
  locale = Locale.EN_US
)

// Define DocumentTemplate in Canadian English
val recipientReceiptSmsDocumentTemplateEN_CA = DocumentTemplate(
  fields = mapOf("sms_body" to "{{sender}} sent you {{amount}}, eh!"),
  // ... same as EN_US
  locale = Locale.EN_CA
)

// Define DocumentTemplate in Spanish
val recipientReceiptSmsDocumentTemplateES_US = DocumentTemplate(
  fields = mapOf("sms_body" to "{{sender}} te envió {{amount}}"),
  // ... same as EN_US
  locale = Locale.ES_US
)

// Use above elements to build a Barbershop 
val barbershop = BarbershopBuilder()
  .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
  .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_CA)
  .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateES_US)
  .installDocument<TransactionalSmsDocument>()
  .build()
  
// Get a Barber who knows how to render RecipientReceipt data into a TransactionalSmsDocument
val recipientReceiptSms = barbershop.getBarber<RecipientReceipt, TransactionalSmsDocument>()
  
// Render in each Locale
val smsEN_US = recipientReceiptSms.render(sandy50Receipt, EN_US) // = Sandy Winchester sent you $50
val smsEN_CA = recipientReceiptSms.render(sandy50Receipt, EN_CA) // = Sandy Winchester sent you $50, eh?
val smsES_US = recipientReceiptSms.render(sandy50Receipt, ES_US) // = Sandy Winchester te envio $50
```

# LocaleResolver

Determining based on a Locale passed in at render which installed Locale to render is done by a LocaleResolver.

It is a simple interface that looks like this:

```kotlin
interface LocaleResolver {
  /**
   * @return a [Locale] from the given [options]
   * @param [options] must be valid keys for a Locale keyed Map
   */
  fun resolve(locale: Locale, options: Set<Locale>): Locale
}
```

Barber comes with a very simple `MatchOrFirstLocaleResolver` that attempts to resolve the requested Locale exactly, and otherwise fallsback to the first installed Locale.

For more complex resolving algorithms, you can set your own custom `LocaleResolver` when building your Barbershop.

```kotlin
val barbershop = BarbershopBuilder()
  // ...
  .setLocaleResolver(MapleSyrupOrFirstLocaleResolver()) // Always tries to resolve EN_CA
  .build()
```

# Integration with Guice

If you use Guice, creating a module that automatically binds all possible typed Barber instances is simple. See the code example below.

```kotlin
package com.your.service.package

import com.google.inject.AbstractModule
import com.google.inject.Key
import com.google.inject.util.Types
import app.cash.barber.Barber
import app.cash.barber.Barbershop
import app.cash.barber.models.BarberKey
import app.cash.barber.models.Document
import app.cash.barber.models.DocumentData

/**
 * Binds all the barbers so they may be injected directly like so:
 *
 * ```
 * @Inject var barber: Barber<RecipientReceipt, TransactionalSmsDocument>
 * ```
 */
class BarberModule(private val barbershop: Barbershop) : AbstractModule() {
  override fun configure() {
    barbershop.getAllBarbers().forEach { (barberKey, barber) ->
      bind(barberKey.asGuiceKey()).toInstance(barber)
    }
  }

  private fun BarberKey.asGuiceKey(): Key<Barber<DocumentData, Document>> {
    val barberType = Types.newParameterizedType(Barber::class.java, documentData.java, document.java)
    @Suppress("UNCHECKED_CAST") // We know this cast is safe dynamically.
    return Key.get(barberType) as Key<Barber<DocumentData, Document>>
  }
}
```

## Coming Soon

### FieldStemming

Automatically replace Money, DateTime, and Instant types with BarberMoney, BarberDateTime, and BarberInstant that let templates call out localized formatted output of each type.

Example

```
BarberInstant(Instant(2019-05-15T15:23:11), EN_US)
= mapOf(
    "date" to "May 15, 2019", 
    "time" to "3:23 pm",
    "casual" to "tomorrow at 3pm"
 )

BarberMoney(Money(50_00), EN_US)
= mapOf(
    "full" to "$50.00"
    "casual" to "$50"
 )
```


Releases
--------

Our [change log][changelog] has release history.

```kotlin
implementation("app.cash.barber:barber:0.1.0")
```

Snapshot builds are [available][snap].


License
--------

    Copyright 2019 Square, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 [barber]: https://cashapp.github.io/barber/
 [changelog]: http://cashapp.github.io/barber/changelog/
 [snap]: https://oss.sonatype.org/content/repositories/snapshots/
