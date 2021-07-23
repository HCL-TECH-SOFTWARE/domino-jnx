package lotus.domino.local;

import java.lang.ref.ReferenceQueue;

@SuppressWarnings("rawtypes")
public class NotesReferenceQueue extends ReferenceQueue implements Runnable {

	static native void NRelease50Sessions();
	
	public NotesReferenceQueue(boolean isNotes) {
		// NOP
	}
	
	@Override
	public void run() {

	}

}
