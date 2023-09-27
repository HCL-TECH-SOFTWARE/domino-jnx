package com.hcl.domino.jna.security;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.NameNotFoundException;
import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoException;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.INotesCAPI1400;
import com.hcl.domino.jna.internal.capi.NotesCAPI1400;
import com.hcl.domino.jna.internal.structs.JwtValidateOptionalParamsStruct;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.security.CredentialValidationTokenHandler;
import com.hcl.domino.security.DominoOIDCTokenValidation;
import com.sun.jna.Memory;
import com.sun.jna.ptr.LongByReference;

/**
 * Implementation of {@link CredentialValidationTokenHandler} that uses
 * {@link INotesCAPI1400#SECValidateAccessToken} to validate a String
 * beginning with {@code "Bearer "} against the server's token
 * settings.
 * 
 * <p>This implementation will attempt to map the email address returned
 * by the underlying method to a DN using the directory API, returning
 * the original email address when not found.</p>
 * 
 * @since 1.29.0
 */
public class JNAOIDCTokenHandler implements CredentialValidationTokenHandler<DominoOIDCTokenValidation> {
  private static final Logger log = Logger.getLogger(JNAOIDCTokenHandler.class.getName());
  
  @Override
  public boolean canProcess(Object token) {
    return token instanceof DominoOIDCTokenValidation;
  }

  @Override
  public Optional<String> getUserDn(DominoOIDCTokenValidation token, String serverName, DominoClient contextDominoClient)
      throws NameNotFoundException, AuthenticationException, AuthenticationNotSupportedException {
    Objects.requireNonNull(token, "token cannot be null");
    
    INotesCAPI1400 api = NotesCAPI1400.get();
    
    Memory memJwt = NotesStringUtils.toLMBCS(token.getToken(), true);
    Memory memProvider = NotesStringUtils.toLMBCS(token.getProviderUrl(), true);
    Memory memScope = NotesStringUtils.toLMBCS(token.getRequiredScope(), true);
    Memory memAud = NotesStringUtils.toLMBCS(token.getAud(), true);
    int dwFlags = DominoEnumUtil.toBitField(DominoOIDCTokenValidation.Flag.class, token.getFlags());
    
    // Check optional parameters to adjust flags and fill in a structure
    JwtValidateOptionalParamsStruct params = JwtValidateOptionalParamsStruct.newInstance();
    String customClaim = token.getCustomClaimName();
    if(customClaim != null && !customClaim.isEmpty()) {
      dwFlags |= NotesConstants.fJWT_validate_UseCustomEmailClaim;
      params.pszCustomClaimName = NotesStringUtils.toLMBCS(customClaim, true);
    }
    Predicate<String> resourceValidator = token.getResourceValidator();
    if(resourceValidator != null) {
      dwFlags |= NotesConstants.fJWT_validate_AllowAlternateAud;
      params.allowedResource = pszAudience -> resourceValidator.test(NotesStringUtils.fromLMBCS(pszAudience, -1));
    }
    Predicate<String> clientIdValidator = token.getClientIdValidator();
    if(clientIdValidator != null) {
      dwFlags |= NotesConstants.fJWT_validate_EnforceAllowedClients;
      params.allowedClientID = pAzp -> clientIdValidator.test(NotesStringUtils.fromLMBCS(pAzp, -1));
    }
    params.write();
    
    try(DisposableMemory memEmail = new DisposableMemory(NotesConstants.MAXUSERNAME)) {
      LongByReference duration = new LongByReference();
      
      Optional<DominoException> e = NotesErrorUtils.toNotesError(api.SECValidateAccessToken(
          memJwt,
          memProvider,
          memScope,
          memAud,
          dwFlags,
          params.getPointer(),
          NotesConstants.MAXUSERNAME,
          memEmail,
          duration
      ));
      if(e.isPresent()) {
        if(log.isLoggable(Level.INFO)) {
          log.log(Level.INFO, "Encountered exception validating OIDC token", e.get());
        }
        
        return Optional.empty();
      } else {
        String email = NotesStringUtils.fromLMBCS(memEmail, -1);
        // Map to the DN if available
        try(DominoClient client = DominoClientBuilder.newDominoClient().asIDUser().build()) {
          Optional<Map<String, List<Object>>> fullName = client.openUserDirectory(null)
              .lookupUserValue(email, NotesConstants.MAIL_FULLNAME_ITEM);
          String dn = fullName.map(m -> m.get(NotesConstants.MAIL_FULLNAME_ITEM))
              .flatMap(l ->
                l.stream()
                  .map(String::valueOf)
                  .filter(s -> s != null && !s.isEmpty())
                  .findFirst()
               )
              .orElse(email);
          return Optional.of(dn);
        }
      }
    }
  }

}
