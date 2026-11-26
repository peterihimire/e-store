package com.benkih.estore.vendor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.benkih.estore.common.exceptions.EmailException;
import com.benkih.estore.email.dto.EmailAttachment;
import com.benkih.estore.email.dto.EmailRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;
import org.springframework.mock.http.client.reactive.MockClientHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class MailgunApiProviderTest {

    @Test
    void sendsMultipartRequestWithBasicAuthAndAttachments() {
        StubConnector connector =
                new StubConnector(HttpStatus.OK, "{\"id\":\"msg-1\",\"message\":\"Queued\"}");

        byte[] pdf = {1, 2, 3, 4};
        EmailRequest request = request();
        request.setAttachments(List.of(new EmailAttachment("receipt.pdf", "application/pdf", pdf)));

        MailgunApiProvider provider = provider(connector);
        assertEquals("msg-1", provider.send(request));

        MockClientHttpRequest sent = connector.captured.get();
        assertEquals(HttpMethod.POST, sent.getMethod());
        assertEquals(
                URI.create("https://api.mailgun.net/v3/mg.example.com/messages"), sent.getURI());
        assertEquals(
                MailgunApiProvider.authHeader("secret-key"),
                sent.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertTrue(sent.getHeaders().getContentType().toString().startsWith("multipart/form-data"));

        MultiValueMap<String, HttpEntity<?>> body =
                provider.buildBody(request, "Estore <hello@example.com>");

        assertEquals("Estore <hello@example.com>", body.getFirst("from").getBody());
        assertEquals("buyer@example.com", body.getFirst("to").getBody());
        assertEquals("Your receipt", body.getFirst("subject").getBody());
        assertEquals("<p>Thank you</p>", body.getFirst("html").getBody());
        assertEquals("Thank you", body.getFirst("text").getBody());

        HttpEntity<?> attachment = body.getFirst("attachment");
        ByteArrayResource resource =
                assertInstanceOf(ByteArrayResource.class, attachment.getBody());
        assertEquals("receipt.pdf", resource.getFilename());
        assertArrayEquals(pdf, resource.getByteArray());
        assertEquals(MediaType.APPLICATION_PDF, attachment.getHeaders().getContentType());
    }

    @Test
    void mapsApiErrorsToEmailExceptionWithoutLeakingKey() {
        StubConnector connector =
                new StubConnector(HttpStatus.UNAUTHORIZED, "{\"message\":\"Unauthorized\"}");

        EmailException error =
                assertThrows(EmailException.class, () -> provider(connector).send(request()));

        assertTrue(error.getMessage().contains("HTTP 401"));
        assertTrue(error.getMessage().contains("Unauthorized"));
        assertFalse(error.getMessage().contains("secret-key"));
    }

    @Test
    void failsFastWhenApiKeyIsMissing() {
        MailgunApiProvider provider =
                new MailgunApiProvider(
                        WebClient.builder().build(),
                        "",
                        "mg.example.com",
                        "hello@example.com",
                        "Estore");

        EmailException error = assertThrows(EmailException.class, provider::init);

        assertTrue(error.getMessage().contains("app.mailgun.api-key"));
    }

    @Test
    void selectsEuRegionBaseUrl() {
        assertEquals("https://api.mailgun.net", MailgunApiProvider.resolveBaseUrl("us"));
        assertEquals("https://api.eu.mailgun.net", MailgunApiProvider.resolveBaseUrl("eu"));
    }

    private static EmailRequest request() {
        EmailRequest request = new EmailRequest();
        request.setTo("buyer@example.com");
        request.setSubject("Your receipt");
        request.setHtml("<p>Thank you</p>");
        request.setText("Thank you");
        return request;
    }

    private static MailgunApiProvider provider(StubConnector connector) {
        return new MailgunApiProvider(
                WebClient.builder(),
                "secret-key",
                "mg.example.com",
                "us",
                "hello@example.com",
                "Estore",
                connector);
    }

    private static final class StubConnector implements ClientHttpConnector {

        private final AtomicReference<MockClientHttpRequest> captured = new AtomicReference<>();
        private final HttpStatusCode status;
        private final String responseBody;

        private StubConnector(HttpStatusCode status, String responseBody) {
            this.status = status;
            this.responseBody = responseBody;
        }

        @Override
        public Mono<ClientHttpResponse> connect(
                HttpMethod method,
                URI uri,
                Function<? super ClientHttpRequest, Mono<Void>> requestCallback) {

            MockClientHttpRequest request = new MockClientHttpRequest(method, uri);

            return requestCallback
                    .apply(request)
                    .then(
                            Mono.fromSupplier(
                                    () -> {
                                        captured.set(request);
                                        MockClientHttpResponse response =
                                                new MockClientHttpResponse(status);
                                        response.getHeaders()
                                                .setContentType(MediaType.APPLICATION_JSON);
                                        response.setBody(responseBody, StandardCharsets.UTF_8);
                                        return response;
                                    }));
        }
    }
}
