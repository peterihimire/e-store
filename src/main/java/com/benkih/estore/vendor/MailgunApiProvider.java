package com.benkih.estore.vendor;

import com.benkih.estore.common.exceptions.EmailException;
import com.benkih.estore.email.dto.EmailAttachment;
import com.benkih.estore.email.dto.EmailRequest;
import com.benkih.estore.email.provider.AbstractEmailProvider;
import io.netty.channel.ChannelOption;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.email.provider", havingValue = "mailgun")
public class MailgunApiProvider extends AbstractEmailProvider {

    private static final String DEFAULT_FROM_NAME = "Estore";

    private final WebClient webClient;
    private final String apiKey;
    private final String domain;
    private final String defaultFrom;
    private final String defaultFromName;

    @Autowired
    public MailgunApiProvider(
            WebClient.Builder webClientBuilder,
            @Value("${app.mailgun.api-key:}") String apiKey,
            @Value("${app.mailgun.domain:}") String domain,
            @Value("${app.mailgun.region:us}") String region,
            @Value("${app.email.from:}") String defaultFrom,
            @Value("${app.email.from-name:Estore}") String defaultFromName,
            @Value("${app.mailgun.connect-timeout-ms:10000}") int connectTimeoutMs,
            @Value("${app.mailgun.response-timeout-ms:30000}") int responseTimeoutMs) {
        this(
                webClientBuilder,
                apiKey,
                domain,
                region,
                defaultFrom,
                defaultFromName,
                new ReactorClientHttpConnector(
                        HttpClient.create()
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                                .responseTimeout(Duration.ofMillis(responseTimeoutMs))));
    }

    MailgunApiProvider(
            WebClient.Builder webClientBuilder,
            String apiKey,
            String domain,
            String region,
            String defaultFrom,
            String defaultFromName,
            ClientHttpConnector connector) {
        this(
                webClientBuilder
                        .clone()
                        .baseUrl(resolveBaseUrl(region))
                        .defaultHeader(HttpHeaders.AUTHORIZATION, authHeader(apiKey))
                        .clientConnector(connector)
                        .build(),
                apiKey,
                domain,
                defaultFrom,
                defaultFromName);
    }

    MailgunApiProvider(
            WebClient webClient,
            String apiKey,
            String domain,
            String defaultFrom,
            String defaultFromName) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.domain = domain;
        this.defaultFrom = defaultFrom;
        this.defaultFromName =
                StringUtils.hasText(defaultFromName) ? defaultFromName : DEFAULT_FROM_NAME;
    }

    static String authHeader(String apiKey) {
        return HttpHeaders.encodeBasicAuth("api", apiKey, StandardCharsets.UTF_8);
    }

    static String resolveBaseUrl(String region) {
        return "eu".equalsIgnoreCase(region)
                ? "https://api.eu.mailgun.net"
                : "https://api.mailgun.net";
    }

    @PostConstruct
    void init() {
        if (!StringUtils.hasText(apiKey)) {
            throw new EmailException(
                    "app.mailgun.api-key is required when app.email.provider=mailgun");
        }
        if (!StringUtils.hasText(domain)) {
            throw new EmailException(
                    "app.mailgun.domain is required when app.email.provider=mailgun");
        }
        if (!StringUtils.hasText(defaultFrom)) {
            throw new EmailException("app.email.from is required when app.email.provider=mailgun");
        }

        log.info("Mailgun API provider initialized for domain {}", domain);
    }

    @Override
    protected void validate(EmailRequest request) {
        super.validate(request);

        if (!StringUtils.hasText(request.getSubject())) {
            throw new EmailException("Subject is required");
        }
        if (!StringUtils.hasText(request.getHtml()) && !StringUtils.hasText(request.getText())) {
            throw new EmailException("Email body is required");
        }
    }

    @Override
    public String send(EmailRequest request) {
        validate(request);

        String from = StringUtils.hasText(request.getFrom()) ? request.getFrom() : defaultFrom;
        String fromName =
                StringUtils.hasText(request.getFromName())
                        ? request.getFromName()
                        : defaultFromName;
        String fromHeader = StringUtils.hasText(fromName) ? fromName + " <" + from + ">" : from;

        try {
            MailgunResponse response =
                    webClient
                            .post()
                            .uri("/v3/{domain}/messages", domain)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .body(BodyInserters.fromMultipartData(buildBody(request, fromHeader)))
                            .retrieve()
                            .onStatus(
                                    HttpStatusCode::isError,
                                    clientResponse ->
                                            clientResponse
                                                    .bodyToMono(String.class)
                                                    .defaultIfEmpty("")
                                                    .map(
                                                            body ->
                                                                    new EmailException(
                                                                            "Mailgun rejected email: HTTP "
                                                                                    + clientResponse
                                                                                            .statusCode()
                                                                                            .value()
                                                                                    + " - "
                                                                                    + abbreviate(
                                                                                            body,
                                                                                            500))))
                            .bodyToMono(MailgunResponse.class)
                            .block();

            if (response == null || !StringUtils.hasText(response.id())) {
                throw new EmailException("Mailgun response did not contain a message id");
            }

            log.info("Mailgun accepted message for {} — id={}", request.getTo(), response.id());
            return response.id();
        } catch (EmailException e) {
            throw e;
        } catch (Exception e) {
            throw new EmailException("Mailgun API send failed: " + e.getMessage(), e);
        }
    }

    private static String abbreviate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength) + "...";
    }

    MultiValueMap<String, HttpEntity<?>> buildBody(EmailRequest request, String fromHeader) {
        MultipartBodyBuilder parts = new MultipartBodyBuilder();

        parts.part("from", fromHeader);
        parts.part("to", request.getTo());
        parts.part("subject", request.getSubject());

        if (StringUtils.hasText(request.getHtml())) {
            parts.part("html", request.getHtml());
        }
        if (StringUtils.hasText(request.getText())) {
            parts.part("text", request.getText());
        }

        if (request.getAttachments() != null) {
            for (EmailAttachment attachment : request.getAttachments()) {
                byte[] content =
                        attachment.getContent() == null ? new byte[0] : attachment.getContent();

                ByteArrayResource resource =
                        new ByteArrayResource(content) {
                            @Override
                            public String getFilename() {
                                return attachment.getFilename();
                            }
                        };

                MediaType mediaType =
                        StringUtils.hasText(attachment.getContentType())
                                ? MediaType.parseMediaType(attachment.getContentType())
                                : MediaType.APPLICATION_OCTET_STREAM;

                parts.part("attachment", resource, mediaType);
            }
        }

        return parts.build();
    }

    record MailgunResponse(String id, String message) {}
}
