package com.hcl.domino.commons.data;

import com.hcl.domino.data.PreV3Author;
import com.hcl.domino.richtext.structures.LicenseID;

/**
 * Default implementation of {@link PreV3Author}.
 * 
 * @author Jesse Gallagher
 * @since 1.0.42
 */
public class DefaultPreV3Author implements PreV3Author {
  private final String name;
  private final LicenseID licenseId;

  public DefaultPreV3Author(String name, LicenseID licenseId) {
    this.name = name;
    this.licenseId = licenseId;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public LicenseID getLicenseID() {
    return licenseId;
  }

  @Override
  public String toString() {
    return String.valueOf(name);
  }
}
