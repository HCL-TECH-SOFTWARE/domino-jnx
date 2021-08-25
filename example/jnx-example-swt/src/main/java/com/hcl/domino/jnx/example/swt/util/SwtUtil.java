package com.hcl.domino.jnx.example.swt.util;

import java.util.prefs.Preferences;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.jnx.example.swt.App;

public enum SwtUtil {
  ;

  public static String loadData(String key) {
    Preferences jPrefs = Preferences.userNodeForPackage(App.class);
    String result = jPrefs.get(key, ""); //$NON-NLS-1$
    return result;
  }
  public static boolean loadDataBoolean(String key) {
    Preferences jPrefs = Preferences.userNodeForPackage(App.class);
    return jPrefs.getBoolean(key, false);
  }
  public static void saveData(String key, String data) {
    Preferences jPrefs = Preferences.userNodeForPackage(App.class);
    jPrefs.put(key, data);
  }
  public static void saveData(String key, boolean data) {
    Preferences jPrefs = Preferences.userNodeForPackage(App.class);
    jPrefs.putBoolean(key, data);
  }

  public static void bindInput(final String key, final Text text) {
    text.setText(loadData(key));
    text.addModifyListener(e -> saveData(key, text.getText()));
    GridData data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.widthHint = 300;
    text.setLayoutData(data);
  }

  public static void bindCheckbox(String key, final Button button) {
    button.setSelection(loadDataBoolean(key));
    button.addSelectionListener(new SelectionAdapter() {
      @Override public void widgetSelected(SelectionEvent e) {
        saveData(key, button.getSelection());
      }
    });
  }

  public static boolean isMacx86() {
    try {
      String os = StringUtil.toString(System.getProperty("os.name")); //$NON-NLS-1$
      if(os.contains("Mac OS X")) { //$NON-NLS-1$
        String arch = StringUtil.toString(System.getProperty("os.arch")); //$NON-NLS-1$
        if(arch.contains("x86") && !arch.contains("x86_64")) { //$NON-NLS-1$ //$NON-NLS-2$
          return true;
        }
      }
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    return false;
  }

  public static boolean isMacx64() {
    try {
      String os = StringUtil.toString(System.getProperty("os.name")); //$NON-NLS-1$
      if(os.contains("Mac OS X")) { //$NON-NLS-1$
        String arch = StringUtil.toString(System.getProperty("os.arch")); //$NON-NLS-1$
        if(arch.contains("x86_64")) { //$NON-NLS-1$
          return true;
        }
      }
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    return false;
  }

  public static boolean isMac() {
    return isMacx86() || isMacx64();
  }
}