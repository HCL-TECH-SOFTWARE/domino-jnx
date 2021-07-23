package lotus.domino.axis.transport.http;

public class NotesSocket {
	private final boolean m_ssl = false;
	private final String m_host = ""; //$NON-NLS-1$
	private final int m_port = 0;
	private final int m_timeout = 0;
	private final int m_ssloptions = 0;
	private int m_context = 0;
	private boolean m_useFullURL = false;
	private boolean m_useProxyAuth = false;
	private String m_proxyUser;
	private String m_proxyPass;
	
	private native void writeBytes(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) throws NotesSocketException;
	private native int readBytes(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) throws NotesSocketException;
	private native void openConnection() throws NotesSocketException;
	private native void closeConnection() throws NotesSocketException;
}
