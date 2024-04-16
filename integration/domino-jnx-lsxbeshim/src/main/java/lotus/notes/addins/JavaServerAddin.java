package lotus.notes.addins;

import lotus.domino.NotesThread;

public class JavaServerAddin extends NotesThread {
  public static final String MSG_Q_PREFIX = "MQ$";
  
  protected int AddInCreateStatusLine(String str) {
    return 0;
  }
  
  protected void AddInDeleteStatusLine(int statusLine) {
    
  }
  
  public native void AddInLogErrorText(String text, int status);
  
  protected void AddInLogMessageText(String text) {
    
  }
  
  protected synchronized boolean addInRunning() {
    return false;
  }
  
  public synchronized void stopAddin() {
  }
  
  protected native void AddInSetStatusLine(int statusLine, String text);
  
  protected native void OSPreemptOccasionally();
}
