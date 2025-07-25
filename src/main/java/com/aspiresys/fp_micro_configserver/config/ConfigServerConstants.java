package com.aspiresys.fp_micro_configserver.config;

/**
 * Constant values used in the configuration server.
 * <p>
 * This class defines constants for CORS headers, HTTP headers, request matcher patterns,
 * and other configuration-related values.
 * </p>
 * <h2>Usage</h2>
 * <ul>
 *  <li>Use CORS constants to configure cross-origin resource sharing in the application.</li>
 * <li>HTTP header constants can be used to set or retrieve specific headers in HTTP requests.</li>
 * <li>Request matcher patterns can be used to define security rules or routing configurations.</li>
 * <li>Origin delimiter is used to split multiple origins in CORS configurations.</li>
 * </ul>
 * <p> * This class is not intended to be instantiated.
 * </p> 
 */
public final class ConfigServerConstants {
    private ConfigServerConstants() {}

    // CORS Headers
    public static final String CORS_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String CORS_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String CORS_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String CORS_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

    // CORS Values
    public static final String CORS_METHODS_VALUE = "GET,POST,PUT,DELETE,OPTIONS";
    public static final String CORS_HEADERS_VALUE = "*";
    public static final String CORS_CREDENTIALS_VALUE = "true";

    // HTTP Headers
    public static final String HEADER_ORIGIN = "Origin";

    // Request matcher patterns
    public static final String PERMIT_ALL_PATTERN = "/**";

    // Origin delimiter
    public static final String ORIGINS_DELIMITER = ",";
}
