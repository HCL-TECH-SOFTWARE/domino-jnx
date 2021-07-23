package lotus.domino.websvc.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Iterator;

import javax.xml.rpc.Service;

public class Stub extends lotus.domino.axis.client.Stub implements javax.xml.rpc.Stub/*, lotus.domino.PortTypeBase*/ {
	protected Stub(URL paramURL, Service paramService) /*throws lotus.domino.types.Fault*/ {
		
	}
	public Stub(Service paramService, String paramString) {
		
	}
	Object[] getOperationInfo(String paramString) throws Exception {
		// NOP
		return null;
	}
	Object invoke(String paramString, int paramInt1, int paramInt2) throws RemoteException {
		// NOP
		return null;
	}
	public String getEndpoint() {
		// NOP
		return null;
	}
	public void setEndpoint(String paramString) throws MalformedURLException {
		// NOP
	}
	@Override
	public void _setProperty(String name, Object value) {
		// NOP
	}
	@Override
	public Object _getProperty(String name) {
		// NOP
		return null;
	}
	@SuppressWarnings("rawtypes")
	@Override
	public Iterator _getPropertyNames() {
		// NOP
		return null;
	}
}
