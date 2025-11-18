package com.hcl.domino.jnx.jep454.misc;

import java.nio.file.Path;
import java.util.List;
import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.jnx.jep454.JEPDominoClient;

public class JEPDominoClientBuilder extends DominoClientBuilder {

  @Override
  public DominoClient build() {
      return new JEPDominoClient(this);
  }

  @Override
  public boolean isFullAccess() {
      return super.isFullAccess();
  }
  
  @Override
  public Path getIDFilePath() {
      return super.getIDFilePath();
  }
  
  @Override
  public String getIDPassword() {
      return super.getIDPassword();
  }
  
  @Override
  public boolean isMaxInternetAccess() {
      return super.isMaxInternetAccess();
  }
  
  @Override
  public String getUserName() {
      return super.getUserName();
  }
  
  @Override
  public List<String> getUserNamesList() {
      return super.getUserNamesList();
  }

  @Override
  public boolean isAsIDUser() {
      return super.isAsIDUser();
  }
  
  @Override
  public String getCredServer() {
      return super.getCredServer();
  }
  @Override
  public String getCredUser() {
      return super.getCredUser();
  }
  @Override
  public String getCredPassword() {
      return super.getCredPassword();
  }
  @Override
  public Object getCredToken() {
      return super.getCredToken();
  }

}
