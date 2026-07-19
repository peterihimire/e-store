package com.benkih.estore.audit.service;

import com.benkih.estore.audit.entity.ApiLog;

public interface IApiLogService {
  ApiLog start(String method, String endpoint, Object requestBody);

  void success(ApiLog log, int statusCode, Object responseBody);

  void failure(ApiLog log, int statusCode, String responseBody, Exception exception);

  void saveOutboundLog(String method, String endpoint, Object request,
                       Integer statusCode, Object response, Exception exception);

  void saveInboundLog(String method, String endpoint, Object request,
                       Integer statusCode, Object response, Exception exception);
}
