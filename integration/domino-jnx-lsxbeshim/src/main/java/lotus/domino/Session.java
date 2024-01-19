package lotus.domino;

public interface Session {
  String getEnvironmentString(String varName, boolean isSystem) throws NotesException;
}
