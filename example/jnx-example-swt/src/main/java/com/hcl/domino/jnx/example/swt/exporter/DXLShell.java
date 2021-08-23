package com.hcl.domino.jnx.example.swt.exporter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.hcl.domino.jnx.example.swt.App;

public class DXLShell extends Shell {

  public DXLShell(Display display) {
    super(display, SWT.SHELL_TRIM);

    setText(App.APP_NAME);
    setSize(500, 550);
    setLayout(new FillLayout());
    

    new DXLExporterPane(this, SWT.NONE);
  }

  @Override
  protected void checkSubclass() {
    // Disable the check that prevents subclassing of SWT components
  }
}