package com.benkih.estore.vendor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.benkih.estore.email.dto.EmailAttachment;
import com.benkih.estore.email.dto.EmailRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Opt-in real send. Enable with {@code MAILGUN_LIVE_TEST=true} and set MAILGUN_API_KEY,
 * MAILGUN_DOMAIN, MAILGUN_TEST_TO and optionally MAIL_FROM / MAILGUN_REGION. Never put credentials
 * in source control.
 */
@EnabledIfEnvironmentVariable(named = "MAILGUN_LIVE_TEST", matches = "true")
class MailgunLiveSmokeTest {

    @Test
    void sendsRealMessageThroughMailgun() {
        String apiKey = System.getenv("MAILGUN_API_KEY");
        String domain = System.getenv("MAILGUN_DOMAIN");
        String recipient = System.getenv("MAILGUN_TEST_TO");

        Assumptions.assumeTrue(StringUtils.hasText(apiKey), "MAILGUN_API_KEY is required");
        Assumptions.assumeTrue(StringUtils.hasText(domain), "MAILGUN_DOMAIN is required");
        Assumptions.assumeTrue(StringUtils.hasText(recipient), "MAILGUN_TEST_TO is required");

        String from = System.getenv("MAIL_FROM");
        if (!StringUtils.hasText(from)) {
            from = "hello@" + domain;
        }
        String region = System.getenv("MAILGUN_REGION");

        MailgunApiProvider provider =
                new MailgunApiProvider(
                        WebClient.builder(), apiKey, domain, region, from, "Estore", 10000, 30000);
        provider.init();

        EmailRequest request = new EmailRequest();
        request.setTo(recipient);
        request.setSubject("E-store Mailgun smoke test");
        request.setHtml("<p>Mailgun API delivery is working.</p>");
        request.setText("Mailgun API delivery is working.");
        request.setAttachments(
                List.of(
                        new EmailAttachment(
                                "smoke.txt",
                                "text/plain",
                                "attachment encoding works".getBytes(StandardCharsets.UTF_8))));

        assertDoesNotThrow(() -> provider.send(request));
    }
}
