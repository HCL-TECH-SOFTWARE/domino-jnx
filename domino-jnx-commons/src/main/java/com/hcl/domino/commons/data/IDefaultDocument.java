package com.hcl.domino.commons.data;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import com.hcl.domino.data.Document;

/**
 * Contains implementation-neutral versions of some {@link Document}
 * methods.
 *  
 * @since 1.12.0
 */
public interface IDefaultDocument extends Document {
  @Override
  default List<X509Certificate> getCertificates() {
    List<X509Certificate> result = new ArrayList<>();
    forEachCertificate((cert, loop) -> result.add(cert));
    return result;
  }
  
  @Override
  default Document computeWithForm(boolean continueOnError, ComputeWithFormCallback callback) {
    return computeWithForm(continueOnError, null, callback);
  }
}
