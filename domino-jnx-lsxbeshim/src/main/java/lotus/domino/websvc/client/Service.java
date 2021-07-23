package lotus.domino.websvc.client;

import java.io.Serializable;

import javax.naming.Referenceable;
import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;

@SuppressWarnings("serial")
public class Service extends lotus.domino.axis.client.Service implements javax.xml.rpc.Service, Serializable, Referenceable {
	protected Service(String paramString) {
		// NOP
	}
	Service(String paramString, byte[] paramArrayOfbyte) {
		// NOP
	}
	
	@Override
	public Call createCall() throws ServiceException {
		return super.createCall();
	}
}
