package com.benkih.estore.audit.service;

import com.benkih.estore.audit.entity.ApiLog;

public interface ApiLogService {
    ApiLog start(String method, String endpoint, Object request);

    void success(ApiLog log, int statusCode, Object response);

    void failure(ApiLog log, int statusCode, String responseBody, Exception e);

    void saveOutboundLog(
            String method,
            String endpoint,
            Object requestBody,
            Integer statusCode,
            Object responseBody,
            Exception exception);

    void saveInboundLog(
            String method,
            String endpoint,
            Object requestBody,
            Integer statusCode,
            Object responseBody,
            Exception exception);
}
