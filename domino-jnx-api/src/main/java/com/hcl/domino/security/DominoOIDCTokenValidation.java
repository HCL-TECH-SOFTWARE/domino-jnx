package com.hcl.domino.security;

/**
 * Representation of an OIDC token that can be used to validate
 * credentials via a default {@link CredentialValidationTokenHandler}
 * on compatible versions of Domino (14 and above).
 * 
 * @since 1.29.0
 */
public class DominoOIDCTokenValidation {
  private final String token;
  private final String providerUrl;
  private final String requiredScope;
  private final String resourceUrl;
  
  public DominoOIDCTokenValidation(String token, String providerUrl, String requiredScope, String resourceUrl) {
    if(token == null || token.isEmpty()) {
      throw new IllegalArgumentException("token cannot be empty");
    }
    if(providerUrl == null || providerUrl.isEmpty()) {
      throw new IllegalArgumentException("providerUrl cannot be empty");
    }
    if(requiredScope == null || requiredScope.isEmpty()) {
      throw new IllegalArgumentException("requiredScope cannot be empty");
    }
    if(resourceUrl == null || resourceUrl.isEmpty()) {
      throw new IllegalArgumentException("resourceUrl cannot be empty");
    }
    
    this.token = token;
    this.providerUrl = providerUrl;
    this.requiredScope = requiredScope;
    this.resourceUrl = resourceUrl;
  }
  
  public String getToken() {
    return token;
  }
  public String getProviderUrl() {
    return providerUrl;
  }
  public String getRequiredScope() {
    return requiredScope;
  }
  public String getResourceUrl() {
    return resourceUrl;
  }

  @Override
  public String toString() {
    return String.format(
        "DominoOIDCTokenValidation [token=%s, providerUrl=%s, requiredScope=%s, resourceUrl=%s]",
        token, providerUrl, requiredScope, resourceUrl);
  }
}
