package com.hcl.domino.jnx.xsp;

import javax.faces.context.FacesContext;

import com.hcl.domino.DominoProcess;
import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.context.RequestCustomizerFactory;
import com.ibm.xsp.context.RequestParameters;
import com.ibm.xsp.event.FacesContextListener;

public class JnxRequestCustomizerFactory extends RequestCustomizerFactory {

  @Override
  public void initializeParameters(FacesContext facesContext, RequestParameters parameters) {
    DominoProcess.get().initializeThread();
    
    if(facesContext instanceof FacesContextEx) {
      ((FacesContextEx)facesContext).addRequestListener(new FacesContextListener() {
        
        @Override
        public void beforeRenderingPhase(FacesContext var1) {
          // NOP
        }
        
        @Override
        public void beforeContextReleased(FacesContext var1) {
          DominoProcess.get().terminateThread();
        }
      });
    }
  }

}
