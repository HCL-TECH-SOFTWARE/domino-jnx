/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.domino.jnx.example.domino.webapp.admin;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;

@PreMatching
public class BasicCorsFilter implements ContainerRequestFilter, ContainerResponseFilter {
  public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin"; //$NON-NLS-1$
  public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials"; //$NON-NLS-1$
  public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods"; //$NON-NLS-1$
  public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers"; //$NON-NLS-1$
  public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age"; //$NON-NLS-1$
  public static final String ORIGIN = "Origin"; //$NON-NLS-1$
  public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method"; //$NON-NLS-1$
  public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers"; //$NON-NLS-1$
  public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers"; //$NON-NLS-1$
  public static final String VARY = "Vary"; //$NON-NLS-1$

  protected boolean allowCredentials = true;
  protected String allowedMethods;
  protected String allowedHeaders;
  protected String exposedHeaders;
  protected int corsMaxAge = -1;
  protected Set<String> allowedOrigins = new HashSet<>();

  protected void checkOrigin(final ContainerRequestContext requestContext, final String origin) {
    if (!this.allowedOrigins.contains("*") && !this.allowedOrigins.contains(origin)) { //$NON-NLS-1$
      requestContext.setProperty("cors.failure", true); //$NON-NLS-1$
      throw new ForbiddenException("Forbidden"); //$NON-NLS-1$
    }
  }

  @Override
  public void filter(final ContainerRequestContext requestContext) throws IOException {
    final String origin = requestContext.getHeaderString(BasicCorsFilter.ORIGIN);
    if (origin == null) {
      return;
    }
    if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) { //$NON-NLS-1$
      this.preflight(origin, requestContext);
    } else {
      this.checkOrigin(requestContext, origin);
    }
  }

  @Override
  public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
      throws IOException {
    final String origin = requestContext.getHeaderString(BasicCorsFilter.ORIGIN);
    if (origin == null || requestContext.getMethod().equalsIgnoreCase("OPTIONS") //$NON-NLS-1$
        || requestContext.getProperty("cors.failure") != null) { //$NON-NLS-1$
      // don't do anything if origin is null, its an OPTIONS request, or cors.failure
      // is set
      return;
    }
    responseContext.getHeaders().putSingle(BasicCorsFilter.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
    responseContext.getHeaders().putSingle(BasicCorsFilter.VARY, BasicCorsFilter.ORIGIN);
    if (this.allowCredentials) {
      responseContext.getHeaders().putSingle(BasicCorsFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"); //$NON-NLS-1$
    }

    if (this.exposedHeaders != null) {
      responseContext.getHeaders().putSingle(BasicCorsFilter.ACCESS_CONTROL_EXPOSE_HEADERS, this.exposedHeaders);
    }
  }

  public String getAllowedHeaders() {
    return this.allowedHeaders;
  }

  /**
   * Will allow all by default.
   *
   * @return allowed methods
   */
  public String getAllowedMethods() {
    return this.allowedMethods;
  }

  /**
   * Put "*" if you want to accept all origins.
   *
   * @return allowed origins
   */
  public Set<String> getAllowedOrigins() {
    return this.allowedOrigins;
  }

  public int getCorsMaxAge() {
    return this.corsMaxAge;
  }

  public String getExposedHeaders() {
    return this.exposedHeaders;
  }

  /**
   * Defaults to true.
   *
   * @return allow credentials
   */
  public boolean isAllowCredentials() {
    return this.allowCredentials;
  }

  protected void preflight(final String origin, final ContainerRequestContext requestContext) throws IOException {
    this.checkOrigin(requestContext, origin);

    final Response.ResponseBuilder builder = Response.ok();
    builder.header(BasicCorsFilter.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
    builder.header(BasicCorsFilter.VARY, BasicCorsFilter.ORIGIN);
    if (this.allowCredentials) {
      builder.header(BasicCorsFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"); //$NON-NLS-1$
    }
    String requestMethods = requestContext.getHeaderString(BasicCorsFilter.ACCESS_CONTROL_REQUEST_METHOD);
    if (requestMethods != null) {
      if (this.allowedMethods != null) {
        requestMethods = this.allowedMethods;
      }
      builder.header(BasicCorsFilter.ACCESS_CONTROL_ALLOW_METHODS, requestMethods);
    }
    String allowHeaders = requestContext.getHeaderString(BasicCorsFilter.ACCESS_CONTROL_REQUEST_HEADERS);
    if (allowHeaders != null) {
      if (this.allowedHeaders != null) {
        allowHeaders = this.allowedHeaders;
      }
      builder.header(BasicCorsFilter.ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
    }
    if (this.corsMaxAge > -1) {
      builder.header(BasicCorsFilter.ACCESS_CONTROL_MAX_AGE, this.corsMaxAge);
    }
    requestContext.abortWith(builder.build());

  }

  public void setAllowCredentials(final boolean allowCredentials) {
    this.allowCredentials = allowCredentials;
  }

  /**
   * Will allow all by default comma delimited string for
   * Access-Control-Allow-Headers.
   *
   * @param allowedHeaders allowed headers
   */
  public void setAllowedHeaders(final String allowedHeaders) {
    this.allowedHeaders = allowedHeaders;
  }

  /**
   * Will allow all by default comma delimited string for
   * Access-Control-Allow-Methods.
   *
   * @param allowedMethods allowed methods
   */
  public void setAllowedMethods(final String allowedMethods) {
    this.allowedMethods = allowedMethods;
  }

  public void setCorsMaxAge(final int corsMaxAge) {
    this.corsMaxAge = corsMaxAge;
  }

  /**
   * Comma delimited list.
   *
   * @param exposedHeaders exposed headers
   */
  public void setExposedHeaders(final String exposedHeaders) {
    this.exposedHeaders = exposedHeaders;
  }
}
