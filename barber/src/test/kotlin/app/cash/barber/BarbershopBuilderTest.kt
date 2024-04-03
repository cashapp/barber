package app.cash.barber

import app.cash.barber.examples.EmptyDocumentData
import app.cash.barber.examples.EncodingTestDocument
import app.cash.barber.examples.InvestmentPurchase
import app.cash.barber.examples.NoParametersDocument
import app.cash.barber.examples.RecipientReceipt
import app.cash.barber.examples.SenderReceipt
import app.cash.barber.examples.ShadowEncodingEverythingPlaintextTestDocument
import app.cash.barber.examples.ShadowPlaintextDocument
import app.cash.barber.examples.TransactionalEmailDocument
import app.cash.barber.examples.TransactionalSmsDocument
import app.cash.barber.examples.TransactionalSmsDocumentWithOptionalMedia
import app.cash.barber.examples.investmentPurchaseEncodingDocumentTemplateEN_US
import app.cash.barber.examples.investmentPurchaseShadowEncodingDocumentTemplateEN_US
import app.cash.barber.examples.mcDonaldsInvestmentPurchase
import app.cash.barber.examples.mcDonaldsInvestmentPurchaseUrl
import app.cash.barber.examples.noParametersDocumentTemplate
import app.cash.barber.examples.plaintextDocumentTemplateEN_US
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_CA
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_GB
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_US
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_USV2
import app.cash.barber.examples.recipientReceiptSmsEmailDocumentTemplateEN_US
import app.cash.barber.examples.senderReceiptEmailDocumentTemplateEN_US
import app.cash.barber.locale.Locale
import app.cash.barber.models.BarberFieldEncoding
import app.cash.barber.models.BarberSignature
import app.cash.barber.models.Document
import app.cash.barber.models.DocumentData
import app.cash.barber.models.DocumentTemplate
import app.cash.barber.models.TemplateToken.Companion.getTemplateToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BarbershopBuilderTest {
  @Test
  fun `Install works`() {
    BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .setWarningsAsErrors()
      .build()
  }

  @Test
  fun `Install works regardless of order`() {
    BarbershopBuilder()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .installDocument<TransactionalSmsDocument>()
      .setWarningsAsErrors()
      .build()
  }

  @Test
  fun `Install multiple locales`() {
    BarbershopBuilder()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_CA)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_GB)
      .installDocument<TransactionalSmsDocument>()
      .setWarningsAsErrors()
      .build()
  }

  @Test
  fun `Install multiple documents`() {
    BarbershopBuilder()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsEmailDocumentTemplateEN_US)
      .installDocumentTemplate<SenderReceipt>(senderReceiptEmailDocumentTemplateEN_US)
      .installDocumentTemplate<InvestmentPurchase>(
        investmentPurchaseEncodingDocumentTemplateEN_US
      )
      .installDocument<EncodingTestDocument>()
      .installDocument<TransactionalEmailDocument>()
      .installDocument<TransactionalSmsDocument>()
      .setWarningsAsErrors()
      .build()
  }

  @Test
  fun `Fails when DocumentTemplate has no target Documents`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate(
          recipientReceiptSmsEmailDocumentTemplateEN_US.toProto().copy(
            target_signatures = listOf()
          )
        )
        .installDocumentTemplate<InvestmentPurchase>(
          investmentPurchaseEncodingDocumentTemplateEN_US
        )
        .installDocument<EncodingTestDocument>()
        .build()
    }
    assertEquals(
      """
      |Errors
      |1) DocumentTemplate must have one or more target Documents, target_signatures is empty. 
      |DocumentTemplate: [templateToken=recipientReceipt][locale=en-US][version=1] 
      |
      """.trimMargin(),
      exception.toString()
    )
  }

  @Test
  fun `Fails when DocumentTemplate target Documents are not installed`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .installDocumentTemplate<SenderReceipt>(senderReceiptEmailDocumentTemplateEN_US)
        .installDocument<TransactionalEmailDocument>()
        .build()
    }
    assertEquals(
      """
      |Errors
      |1) Attempted to install DocumentTemplate without the corresponding Document being installed.
      |Not installed DocumentTemplate.target_signatures:
      |[BarberSignature(signature=sms_body,1, fields={sms_body=STRING})]
      |
      """.trimMargin(),
      exception.toString()
    )
  }

  @Test
  fun `Fails when DocumentTemplate installed with non-source DocumentData`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate<SenderReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .installDocument<TransactionalSmsDocument>()
        .build()
    }
    assertEquals(
      """
      |Errors
      |1) Attempted to install DocumentTemplate with a DocumentData not specified in the DocumentTemplate source
      |DocumentTemplate.source: app.cash.barber.examples.RecipientReceipt
      |DocumentData: app.cash.barber.examples.SenderReceipt
      |
      """.trimMargin(), exception.toString()
    )
  }

  @Test
  fun `Passes when DocumentTemplate naively matches source signature since types all resolve to String`() {
    BarbershopBuilder()
      .installDocumentTemplate(
        recipientReceiptSmsEmailDocumentTemplateEN_US.toProto().copy(
          source_signature = BarberSignature(
            mapOf(
              "sender" to app.cash.protos.barber.api.BarberSignature.Type.STRING,
              "amount" to app.cash.protos.barber.api.BarberSignature.Type.STRING,
              "cancelUrl" to app.cash.protos.barber.api.BarberSignature.Type.STRING,
              "deposit_expected_at" to app.cash.protos.barber.api.BarberSignature.Type.STRING
            )
          ).signature
        )
      )
      .installDocument<TransactionalEmailDocument>()
      .installDocument<TransactionalSmsDocument>()
      .setWarningsAsErrors()
      .build()
  }

  @Test
  fun `Fails when DocumentTemplate installed where explicit source_signature does not match computed implicit source signature`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate(
          recipientReceiptSmsEmailDocumentTemplateEN_US.toProto().copy(
            source_signature = BarberSignature(
              mapOf(
                "sender" to app.cash.protos.barber.api.BarberSignature.Type.STRING,
                "amount" to app.cash.protos.barber.api.BarberSignature.Type.STRING,
                "deposit_expected_at" to app.cash.protos.barber.api.BarberSignature.Type.STRING
              )
            ).signature
          )
        )
        .installDocument<TransactionalEmailDocument>()
        .installDocument<TransactionalSmsDocument>()
        .build()
    }
    assertEquals(
      """
      |Errors
      |1) Missing variable [cancelUrl] for DocumentData with [templateToken=recipientReceipt] for DocumentTemplate field [Field{key=primary_button_url, template=\{\{cancelUrl\}\}}]
      |
      """.trimMargin(), exception.toString()
    )
  }

  @Test
  fun `Fails when Document has no parameters`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocument<NoParametersDocument>()
        .installDocumentTemplate<EmptyDocumentData>(noParametersDocumentTemplate)
        .build()
    }
    assertEquals(
      """
      |Errors
      |1) No fields included for Document [class app.cash.barber.examples.NoParametersDocument]
      |
      """.trimMargin(), exception.toString()
    )
  }

  @Test
  fun `Install fails on no DocumentTemplate and DocumentData`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocument<TransactionalEmailDocument>()
        .build()
    }

    assertEquals(
      """
          |Errors
          |1) No DocumentData or DocumentTemplates installed
          |
        """.trimMargin(),
      exception.toString()
    )
  }

  @Test
  fun `Install warns on missing Document for an installed DocumentTemplate`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(
          investmentPurchaseShadowEncodingDocumentTemplateEN_US
        )
        .installDocument<EncodingTestDocument>()
        .setWarningsAsErrors()
        .build()
    }
    assertEquals(
      """
          |Errors
          |1) Attempted to install DocumentTemplate with a DocumentData not specified in the DocumentTemplate source
          |DocumentTemplate.source: app.cash.barber.examples.InvestmentPurchase
          |DocumentData: app.cash.barber.examples.RecipientReceipt
          |
        """.trimMargin(),
      exception.toString()
    )
  }

  @Test
  fun `setWarningsAsErrors fails out early for install with no Documents`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .build()
    }
    assertEquals(
      """
          |Errors
          |1) No Documents installed
          |
        """.trimMargin(),
      exception.toString()
    )
  }

  @Test
  fun `Fails on unused dangling installed Document`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocument<TransactionalEmailDocument>()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .installDocument<TransactionalSmsDocument>()
        .setWarningsAsErrors()
        .build()
    }
    assertEquals(
      """
      |Warnings
      |1) Document installed that is not used in any installed DocumentTemplates
      |[app.cash.barber.examples.TransactionalEmailDocument]
      |
      """.trimMargin(), exception.toString()
    )
  }

  @Test
  fun `Renders correctly Documents with shadow fields with different encoding`() {
    val barber = BarbershopBuilder()
      .installDocumentTemplate<InvestmentPurchase>(
        investmentPurchaseShadowEncodingDocumentTemplateEN_US
      )
      .installDocument<EncodingTestDocument>()
      .installDocument<ShadowEncodingEverythingPlaintextTestDocument>()
      .setWarningsAsErrors()
      .build()

    val specRegular = barber.getBarber<InvestmentPurchase, EncodingTestDocument>()
      .render(mcDonaldsInvestmentPurchase, Locale.EN_US)

    assertThat(specRegular).isEqualTo(
      EncodingTestDocument(
        no_annotation_field = "You purchased 100 shares of McDonald&#39;s.",
        default_field = "You purchased 100 shares of McDonald&#39;s.",
        html_field = "You purchased 100 shares of McDonald&#39;s.",
        plaintext_field = "You purchased 100 shares of McDonald's.",
        url_field = mcDonaldsInvestmentPurchaseUrl,
      )
    )

    val specShadow =
      barber.getBarber<InvestmentPurchase, ShadowEncodingEverythingPlaintextTestDocument>()
        .render(mcDonaldsInvestmentPurchase, Locale.EN_US)

    assertThat(specShadow).isEqualTo(
      ShadowEncodingEverythingPlaintextTestDocument(
        // Notice all shadowed fields are still rendered with the correct Plaintext encoding
        // even though they conflict in name with the above target document that has HTML encoding
        no_annotation_field = "You purchased 100 shares of McDonald's.",
        default_field = "You purchased 100 shares of McDonald's.",
        html_field = "You purchased 100 shares of McDonald's.",
        plaintext_field = "You purchased 100 shares of McDonald's.",
        non_shadow_field = "You purchased 100 shares of McDonald's."
      )
    )
  }

  @Test
  fun `Barbershop does not include documents as targets where template does not satisfy all fields`() {
    val barber = BarbershopBuilder()
      .installDocumentTemplate<InvestmentPurchase>(
        investmentPurchaseEncodingDocumentTemplateEN_US
      )
      .installDocumentTemplate<SenderReceipt>(plaintextDocumentTemplateEN_US)
      .installDocument<EncodingTestDocument>()
      .installDocument<ShadowPlaintextDocument>()
      .setWarningsAsErrors()
      .build()

    // investmentPurchaseEncodingDocumentTemplateEN_US only specifies EncodingTestDocument
    // but can support ShadowPlaintextDocument, so it is also included
    assertEquals(
      setOf(EncodingTestDocument::class, ShadowPlaintextDocument::class),
      barber.getTargetDocuments(InvestmentPurchase::class.getTemplateToken())
    )
    assertEquals(
      setOf(ShadowPlaintextDocument::class),
      barber.getTargetDocuments(SenderReceipt::class.getTemplateToken())
    )
  }

  @Test
  fun `A Document with all optional fields is not a target of all Barbers`() {
    data class PushDocumentWithAllFieldsOptional(
      val push_androidTitle: String?,
      val push_androidBody: String?,
      val push_iOSTitle: String?,
      val push_iOSBody: String?,
    ) : Document

    data class GreetingPushDocumentData(val name: String) : DocumentData

    val greetingPushTemplateEnUs = DocumentTemplate(
      fields = mapOf(
        "push_iOSBody" to "Hi, {{name}}!",
      ),
      source = GreetingPushDocumentData::class,
      targets = setOf(PushDocumentWithAllFieldsOptional::class),
      locale = Locale.EN_US
    )

    val barber = BarbershopBuilder()
      .installDocumentTemplate<InvestmentPurchase>(
        investmentPurchaseEncodingDocumentTemplateEN_US
      )
      .installDocumentTemplate<GreetingPushDocumentData>(greetingPushTemplateEnUs)
      .installDocument<EncodingTestDocument>()
      .installDocument<PushDocumentWithAllFieldsOptional>()
      .setWarningsAsErrors()
      .build()

    // Just because all fields of [PushDocumentWithAllFieldsOptional] are optional,
    // it should not be targeted by every installed Document Template
    assertThat(barber.getTargetDocuments(InvestmentPurchase::class.getTemplateToken()))
      .isEqualTo(setOf(EncodingTestDocument::class))

    assertThat(barber.getTargetDocuments(GreetingPushDocumentData::class.getTemplateToken()))
      .isEqualTo(setOf(PushDocumentWithAllFieldsOptional::class))
  }

  @Test
  fun `Barbershop respects version parameter`() {
    val barber = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocument<ShadowPlaintextDocument>()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_USV2)
      .setWarningsAsErrors()
      .build()

    val templateToken = RecipientReceipt::class.getTemplateToken()
    val documentData = app.cash.protos.barber.api.DocumentData(
      template_token = templateToken.token
    )

    val v1ExpectedDocs = setOf(TransactionalSmsDocument::class)
    assertEquals(v1ExpectedDocs, barber.getTargetDocuments(documentData, 1))
    assertEquals(v1ExpectedDocs, barber.getTargetDocuments(templateToken, 1))

    val v2ExpectedDocs = setOf(TransactionalSmsDocument::class, ShadowPlaintextDocument::class)
    assertEquals(v2ExpectedDocs, barber.getTargetDocuments(documentData, 2))
    assertEquals(v2ExpectedDocs, barber.getTargetDocuments(templateToken, 2))
  }

  @Test
  fun `setDefaultBarberFieldEncoding changes non-annotated field render encoding`() {
    val barber = BarbershopBuilder()
      .installDocument<EncodingTestDocument>()
      .installDocumentTemplate<InvestmentPurchase>(
        investmentPurchaseEncodingDocumentTemplateEN_US
      )
      .setDefaultBarberFieldEncoding(BarberFieldEncoding.STRING_PLAINTEXT)
      .build()

    val spec = barber.getBarber<InvestmentPurchase, EncodingTestDocument>()
      .render(mcDonaldsInvestmentPurchase, Locale.EN_US)

    assertThat(spec).isEqualTo(
      EncodingTestDocument(
        // Using the default HTML encoding, the apostrophe in no_annotation_field have been escaped.
        no_annotation_field = "You purchased 100 shares of McDonald's.",
        default_field = "You purchased 100 shares of McDonald&#39;s.",
        html_field = "You purchased 100 shares of McDonald&#39;s.",
        plaintext_field = "You purchased 100 shares of McDonald's.",
        url_field = "https://cash.app/go/InvestConfirmAction_input?strOrigTrackNum=9400111899561379412019",
      )
    )
  }

  @Test
  fun `HttpUrl field encoding escapes non-URL characters to prevent code injection`() {
    val iframeInjection = "<iframe>test</iframe>"
    val cssKeyframeInjection = "<style>@keyframes x{}</style><b style=\"animation-n"
    val htmlCode = "<h1>test2</h1>"
    val javascriptAlertInjection = "javascript:alert`1`\t"

    val barber = BarbershopBuilder()
      .installDocument<EncodingTestDocument>()
      .installDocumentTemplate<InvestmentPurchase>(
        investmentPurchaseEncodingDocumentTemplateEN_US
      )
      .setDefaultBarberFieldEncoding(BarberFieldEncoding.STRING_PLAINTEXT)
      .build()

    val spec = barber.getBarber<InvestmentPurchase, EncodingTestDocument>()
      .render(
        mcDonaldsInvestmentPurchase.copy(
          url = "https://test.com/$iframeInjection$cssKeyframeInjection$htmlCode$javascriptAlertInjection",
        ), Locale.EN_US
      )

    assertThat(spec).isEqualTo(
      EncodingTestDocument(
        // Using the default HTML encoding, the apostrophe in no_annotation_field have been escaped.
        no_annotation_field = "You purchased 100 shares of McDonald's.",
        default_field = "You purchased 100 shares of McDonald&#39;s.",
        html_field = "You purchased 100 shares of McDonald&#39;s.",
        plaintext_field = "You purchased 100 shares of McDonald's.",
        url_field = "https://test.com/%3Ciframe%3Etest%3C/iframe%3E%3Cstyle%3E@keyframes%20x%7B%7D%3C/style%3E%3Cb%20style=%22animation-n%3Ch1%3Etest2%3C/h1%3Ejavascript:alert%601%60",
      )
    )
    assertThat(spec.url_field).doesNotContain(iframeInjection)
    assertThat(spec.url_field).doesNotContain(cssKeyframeInjection)
    assertThat(spec.url_field).doesNotContain(htmlCode)
    assertThat(spec.url_field).doesNotContain(javascriptAlertInjection)
  }

  @Test
  fun `Fails when variable in field template is not in source DocumentData`() {
    data class TradeReceipt(
      val ticker: String
    ) : DocumentData

    val tradeReceiptEN_US = DocumentTemplate(
      fields = mapOf(
        "sms_body" to "You bought {{ shares }} shares of {{ ticker }}."
      ),
      source = TradeReceipt::class,
      targets = setOf(TransactionalSmsDocument::class),
      locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocument<TransactionalSmsDocument>()
        .installDocumentTemplate<TradeReceipt>(tradeReceiptEN_US)
        .build()
    }
    assertEquals(
      """
        |Errors
        |1) Missing variable [shares] for DocumentData with [templateToken=tradeReceipt] for DocumentTemplate field [Field{key=sms_body, template=You bought \{\{ shares \}\} shares of \{\{ ticker \}\}.}]
        |
      """.trimMargin(),
      exception.toString()
    )
  }

  @Test
  fun `Builds a backwards-compatible Barbershop when a nullable Document field is not present in a DocumentTemplate`() {
    data class TradeReceipt(
      val shares: String,
      val ticker: String
    ) : DocumentData

    // "Old" version of the DocumentTemplate, created before the TransactionalSmsDocument had an optional field added
    val oldTradeReceiptWithNoMediaEnUS = DocumentTemplate(
      fields = mapOf(
        "sms_body" to "(v1) You bought {{ shares }} shares of {{ ticker }}."
      ),
      source = TradeReceipt::class,
      targets = setOf(TransactionalSmsDocument::class),
      locale = Locale.EN_US,
      version = 1
    )

    // "New" version of the DocumentTemplate, with the optional field included
    val newTradeReceiptWithMediaEnUS = DocumentTemplate(
      fields = mapOf(
        "sms_body" to "(v2) You bought {{ shares }} shares of {{ ticker }}.",
        "sms_mediaUrl" to "https://cdn.com/images/test.png"
      ),
      source = TradeReceipt::class,
      targets = setOf(TransactionalSmsDocumentWithOptionalMedia::class),
      locale = Locale.EN_US,
      version = 2
    )

    val barbershop = BarbershopBuilder()
      .installDocument<TransactionalSmsDocumentWithOptionalMedia>()
      .installDocumentTemplate(oldTradeReceiptWithNoMediaEnUS.toProto())
      .installDocumentTemplate(newTradeReceiptWithMediaEnUS.toProto())
      .build()

    val tradeReceiptDocumentData = TradeReceipt("3", "SQ")

    val barber = barbershop.getBarber<TradeReceipt, TransactionalSmsDocumentWithOptionalMedia>()

    val renderNoMedia = barber.render(tradeReceiptDocumentData, Locale.EN_US, 1)
    assertThat(renderNoMedia.sms_body).isEqualTo("(v1) You bought 3 shares of SQ.")
    assertThat(renderNoMedia.sms_mediaUrl).isNull()

    val renderWithMedia = barber.render(tradeReceiptDocumentData, Locale.EN_US, 2)
    assertThat(renderWithMedia.sms_body).isEqualTo("(v2) You bought 3 shares of SQ.")
    assertThat(renderWithMedia.sms_mediaUrl).isEqualTo("https://cdn.com/images/test.png")
  }

  @Test
  fun `Builds and warns when an extra Document field is present in a DocumentTemplate`() {

    data class TradeReceipt(
      val shares: String,
      val ticker: String
    ) : DocumentData

    val tradeReceiptWithNoPrefixEnUS = DocumentTemplate(
      fields = mapOf(
        "sms_body" to "You bought {{ shares }} shares of {{ ticker }}."
      ),
      source = TradeReceipt::class,
      targets = setOf(TransactionalSmsDocument::class),
      locale = Locale.EN_US,
      version = 1
    )

    val tradeReceiptWithPrefixEnUS = DocumentTemplate(
      fields = mapOf(
        "sms_body" to "You bought {{ shares }} shares of {{ ticker }}.",
        "sms_mediaUrl" to "https://cdn.com/images/test.png"
      ),
      source = TradeReceipt::class,
      targets = setOf(TransactionalSmsDocument::class),
      locale = Locale.EN_US,
      version = 2
    )

    val builder = BarbershopBuilder()
      .installDocument<TransactionalSmsDocument>()
      .installDocumentTemplate<TradeReceipt>(tradeReceiptWithNoPrefixEnUS)
      .installDocumentTemplate<TradeReceipt>(tradeReceiptWithPrefixEnUS)

    val barbershop = builder.build()

    val tradeReceiptDocumentData = TradeReceipt("3", "SQ")

    val barber = barbershop.getBarber<TradeReceipt, TransactionalSmsDocument>()

    val renderNoPrefix = barber.render(tradeReceiptDocumentData, Locale.EN_US, 1)
    assertThat(renderNoPrefix.sms_body).isEqualTo("You bought 3 shares of SQ.")

    val renderWithPrefix = barber.render(tradeReceiptDocumentData, Locale.EN_US, 2)
    assertThat(renderWithPrefix.sms_body).isEqualTo("You bought 3 shares of SQ.")

    val exception = assertFailsWith<BarberException> {
      builder
        .setWarningsAsErrors()
        .build()
    }
    assertThat(exception.toString()).isEqualTo(
      """Warnings
          |1) Installed DocumentTemplate has additional fields that are not used in any target Document
          |Additional fields:
          |sms_mediaUrl
          |""".trimMargin()
    )
  }

  @Test
  fun `Fails when variable in data is not used in any field template`() {
    data class TradeReceipt(
      val ticker: String,
      val shares: String
    ) : DocumentData

    val tradeReceiptEN_US = DocumentTemplate(
      fields = mapOf(
        "sms_body" to "Welcome to the {{ ticker }} shareholder family!"
      ),
      source = TradeReceipt::class,
      targets = setOf(TransactionalSmsDocument::class),
      locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocument<TransactionalSmsDocument>()
        .installDocumentTemplate<TradeReceipt>(tradeReceiptEN_US)
        .setWarningsAsErrors()
        .build()
    }
    assertEquals(
      """
          |Warnings
          |1) Unused DocumentData variable [shares] in Source signature [shares,1;ticker,1] with no
          |usage in DocumentTemplate: [templateToken=tradeReceipt][locale=en-US][version=1] 
          |
        """.trimMargin(),
      exception.toString()
    )
  }

  @Test
  fun `Fails when DocumentTemplate does not have sufficient fields for target Document`() {
    val recipientReceiptDocumentTemplate = DocumentTemplate(
      fields = mapOf(
        "subject" to "{{ sender }} sent {{ amount }} on {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}"
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalSmsDocument::class),
      locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptDocumentTemplate)
        .installDocument<TransactionalSmsDocument>()
        .build()
    }
    assertEquals(
      """
          |Errors
          |1) Installed DocumentTemplate: [templateToken=recipientReceipt][locale=en-US][version=1]
          |missing required fields for Document targets:
          |[document=app.cash.barber.examples.TransactionalSmsDocument] requires missing [field=sms_body]
          |
        """.trimMargin(),
      exception.toString()
    )
  }

  @Test
  fun `DocumentTemplate has extra fields unused by target Documents is a warning`() {
    val recipientReceiptDocumentData = DocumentTemplate(
      fields = mapOf(
        "sms_body" to "{{ sender }} sent you {{ amount }} on {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}",
        "extra_field" to "This field is unused in any target document"
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalSmsDocument::class),
      locale = Locale.EN_US
    )

    val builder = BarbershopBuilder()
      .installDocumentTemplate<RecipientReceipt>(recipientReceiptDocumentData)
      .installDocument<TransactionalSmsDocument>()

    assertThat(builder.build()).isNotNull

    val exception = assertFailsWith<BarberException> {
      builder.setWarningsAsErrors().build()
    }
    assertThat(exception.toString()).isEqualTo(
      """
          |Warnings
          |1) Installed DocumentTemplate has additional fields that are not used in any target Document
          |Additional fields:
          |extra_field
          |
        """.trimMargin()
    )
  }

  @Test
  fun `Fails on duplicate install of DocumentTemplate`() {
    val first = DocumentTemplate(
      fields = mapOf(
        "sms_body" to "first {{ sender }} sent you {{ amount }} on {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}"
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalSmsDocument::class),
      locale = Locale.EN_US
    )
    val second = DocumentTemplate(
      fields = mapOf(
        "sms_body" to "second {{ sender }} sent you {{ amount }} on {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}"
      ),
      source = RecipientReceipt::class,
      targets = setOf(TransactionalSmsDocument::class),
      locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(first)
        .installDocumentTemplate<RecipientReceipt>(second)
        .installDocument<TransactionalSmsDocument>()
        .build()
    }
    assertEquals(
      """
          |Errors
          |1) Attempted to install DocumentTemplate that will overwrite an already installed locale version
          |DocumentTemplate: [templateToken=recipientReceipt][locale=en-US][version=1]
          |
          |Already Installed DocumentTemplates
          |TemplateToken: recipientReceipt
          |Locales: [Locale=en-US]
          |Installed Versions: [1]
          |
        """.trimMargin(),
      exception.toString()
    )
  }
}
