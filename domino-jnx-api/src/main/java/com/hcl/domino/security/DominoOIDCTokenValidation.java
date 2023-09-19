package com.hcl.domino.security;

import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Predicate;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;

/**
 * Representation of an OIDC token that can be used to validate
 * credentials via a default {@link CredentialValidationTokenHandler}
 * on compatible versions of Domino (14 and above).
 * 
 * @since 1.29.0
 */
public class DominoOIDCTokenValidation {
  /**
   * Flags that can be passed with the token validation request to
   * control internal validation behavior.
   */
  public enum Flag implements INumberEnum<Integer> {
    AllowExpired(NotesConstants.fJWT_validate_AllowExpired),
    AllowMSWorkarounds(NotesConstants.fJWT_validate_AllowMSWorkarounds);
    
    private final int value;
    
    private Flag(int value) {
      this.value = value;
    }
    
    @Override
    public long getLongValue() {
      return value;
    }
    
    @Override
    public Integer getValue() {
      return value;
    }
  }
  
  private final String token;
  private final String providerUrl;
  private final String requiredScope;
  private final String aud;
  private final Collection<Flag> flags;
  private final String customClaimName;
  private final Predicate<String> resourceValidator;
  private final Predicate<String> clientIdValidator;
  
  /**
   * Constructs a new token validation request using the required parameters.
   * 
   * @param token the token to validate, without any leading "Bearer " from a header
   * @param providerUrl the expected provider URL, which must match the Base URL field
   *                    from idpcat.nsf
   * @param requiredScope scope required to be in the token to validate, such as
   *                      {@code "Domino.user.all"}
   * @param aud the expected audience claim, such as {@code "https://www.example.com"}
   */
  public DominoOIDCTokenValidation(String token, String providerUrl, String requiredScope, String aud) {
    this(token, providerUrl, requiredScope, aud, null, null, null, null);
  }
  
  /**
   * Constructs a new token validation request using the required parameters as well as
   * extended parameters for validation flags and handling of audience and client IDs.
   * 
   * @param token the token to validate, without any leading "Bearer " from a header
   * @param providerUrl the expected provider URL, which must match the Base URL field
   *                    from idpcat.nsf
   * @param requiredScope scope required to be in the token to validate, such as
   *                      {@code "Domino.user.all"}
   * @param aud the expected audience claim, such as {@code "https://www.example.com"}
   * @param flags a {@link Collection} of {@link Flag} values to control validation;
   *              may be {@code null}
   * @param customClaimName the name of the expected claim in the JWT containing the
   *                        email address; defaults to {@code "email"} when blank
   * @param resourceValidator a callback to validate additional {@code "aud"} claim
   *                          values, called once per audience; may be {@code null}
   * @param clientIdValidator a callback to validate the contents of the {@code "azp"}
   *                          claim; may be {@code null}
   */
  public DominoOIDCTokenValidation(String token, String providerUrl, String requiredScope, String aud, Collection<Flag> flags, String customClaimName, Predicate<String> resourceValidator, Predicate<String> clientIdValidator) {
    if(token == null || token.isEmpty()) {
      throw new IllegalArgumentException("token cannot be empty");
    }
    if(providerUrl == null || providerUrl.isEmpty()) {
      throw new IllegalArgumentException("providerUrl cannot be empty");
    }
    if(requiredScope == null || requiredScope.isEmpty()) {
      throw new IllegalArgumentException("requiredScope cannot be empty");
    }
    if(aud == null || aud.isEmpty()) {
      throw new IllegalArgumentException("aud cannot be empty");
    }
    
    this.token = token;
    this.providerUrl = providerUrl;
    this.requiredScope = requiredScope;
    this.aud = aud;
    this.flags = flags == null ? EnumSet.noneOf(Flag.class) : EnumSet.copyOf(flags);
    this.customClaimName = customClaimName;
    this.resourceValidator = resourceValidator;
    this.clientIdValidator = clientIdValidator;
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
  public String getAud() {
    return aud;
  }
  public Collection<Flag> getFlags() {
    return EnumSet.copyOf(flags);
  }
  public String getCustomClaimName() {
    return customClaimName;
  }
  public Predicate<String> getClientIdValidator() {
    return clientIdValidator;
  }
  public Predicate<String> getResourceValidator() {
    return resourceValidator;
  }

  @Override
  public String toString() {
    return String.format(
        "DominoOIDCTokenValidation [token=%s, providerUrl=%s, requiredScope=%s, aud=%s, flags=%s]",
        token, providerUrl, requiredScope, aud, flags
    );
  }
}
