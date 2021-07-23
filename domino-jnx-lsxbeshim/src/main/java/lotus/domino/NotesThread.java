package lotus.domino;

import com.hcl.domino.commons.util.PlatformUtils;

public class NotesThread {
	static {
		String libName;
		if(PlatformUtils.isWindows()) {
			libName = "nlsxbe"; //$NON-NLS-1$
		} else {
			libName = "lsxbe"; //$NON-NLS-1$
		}
		System.loadLibrary(libName);
	}
	
	private static native void NnotesInitThread();

	private static native void NnotesTermThread();
	
	public static void sinitThread() {
		NnotesInitThread();
	}
	public static void stermThread() {
		NnotesTermThread();
	}
}
