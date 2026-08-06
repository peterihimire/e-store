package com.benkih.estore.audit.service;

import com.benkih.estore.audit.entity.ApiLog;
import com.benkih.estore.audit.repository.ApiLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiLogService implements IApiLogService{
  private final ApiLogRepository apiLogRepository;
  private final ObjectMapper objectMapper;


  public ApiLog start(String method, String endpoint, Object request) {
    ApiLog log = new ApiLog();

    log.setMethod(method);
    log.setEndpoint(endpoint);
    try {
      log.setRequestBody(objectMapper.writeValueAsString(request));
    } catch (JsonProcessingException e) {
      log.setRequestBody("Unable to serialize request");
    }

    return apiLogRepository.save(log);
  }


  public void success(ApiLog log, int statusCode, Object response) {
    log.setStatusCode(statusCode);
    try {
      log.setResponseBody(objectMapper.writeValueAsString(response));
    } catch (JsonProcessingException e) {
      log.setResponseBody("Unable to serialize response");
    }

    apiLogRepository.save(log);
  }


  public void failure(ApiLog log, int statusCode, String responseBody, Exception e) {
    log.setStatusCode(statusCode);
    log.setResponseBody(responseBody);
    log.setExceptionMessage(e.getMessage());

    apiLogRepository.save(log);
  }

  public void saveOutboundLog(
      String method,
      String endpoint,
      Object requestBody,
      Integer statusCode,
      Object responseBody,
      Exception exception
  ) {
    ApiLog log = new ApiLog();

    log.setMethod(method);
    log.setEndpoint(endpoint);
    log.setStatusCode(statusCode);

    log.setRequestBody(writeJson(requestBody));
    log.setResponseBody(writeJson(responseBody));

    if (exception != null) {
      log.setExceptionMessage(exception.getMessage());
    }
    apiLogRepository.save(log);
  }


  @Override
  public void saveInboundLog(
      String method,
      String endpoint,
      Object requestBody,
      Integer statusCode,
      Object responseBody,
      Exception exception
  ) {
    ApiLog log = new ApiLog();

    log.setMethod(method);
    log.setEndpoint(endpoint);
    log.setStatusCode(statusCode);

    log.setRequestBody(writeJson(requestBody));
    log.setResponseBody(writeJson(responseBody));

    if (exception != null) {
      log.setExceptionMessage(exception.getMessage());
    }
    apiLogRepository.save(log);
  }


  private String writeJson(Object value) {
    if (value == null) {
      return null;
    }

    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      return value.toString();
    }
  }
}
