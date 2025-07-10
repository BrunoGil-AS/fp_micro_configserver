package com.aspiresys.fp_micro_configserver.config;

/**
 * Constantes para la configuración del config server.
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
