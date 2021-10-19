package com.hcl.domino.jnx.example.swt.console;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.hcl.domino.admin.IConsoleLine;
import com.hcl.domino.admin.ServerAdmin.ConsoleHandler;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.jnx.example.swt.bean.DominoContextBean;

public class ConsolePane extends Composite {

  private Text serverNameInput;
  private Button connectButton;
  private Text output;
  private Text input;
  private OutputConsoleHandler handler;
  
  public ConsolePane(Composite parent) {
    super(parent, SWT.NONE);
  
    setLayout(new GridLayout(3, false));

    createControls();
    connectActions();
  }
  
  private void createControls() {
    {
      Label serverNameLabel = new Label(this, SWT.NONE);
      serverNameLabel.setText("Server");
      serverNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
      
      serverNameInput = new Text(this, SWT.BORDER);
      serverNameInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      
      connectButton = new Button(this, SWT.PUSH);
      connectButton.setText("Connect");
      connectButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    }
    {
      output = new Text(this, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
      output.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
      output.setEditable(false);
      // TODO pick a non-named font -
      // https://stackoverflow.com/questions/221568/swt-os-agnostic-way-to-get-monospaced-font
      output.setFont(new Font(getDisplay(), "Consolas", 12, SWT.NORMAL)); //$NON-NLS-1$
    }
    {
      input = new Text(this, SWT.BORDER);
      input.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
      input.setEnabled(false);
    }
  }
  
  private void connectActions() {
    serverNameInput.addTraverseListener(e -> {
      if(e.detail == SWT.TRAVERSE_RETURN) {
        connectToServer();
      }
    });
    connectButton.addListener(SWT.Selection, e -> connectToServer());
    input.addTraverseListener(e -> {
      if(e.detail == SWT.TRAVERSE_RETURN) {
        String serverName = serverNameInput.getText();
        String command = input.getText();
        getDisplay().asyncExec(() -> input.setText("")); //$NON-NLS-1$
        
        DominoContextBean.submit(client -> {
          String reply = client.getServerAdmin().sendConsoleCommand(serverName, command);
          if(StringUtil.isNotEmpty(reply)) {
            getDisplay().asyncExec(() -> output.append(reply));
          }
        });
      }
    });
  }
  
  private void connectToServer() {
    String serverName = serverNameInput.getText();
    if(StringUtil.isNotEmpty(serverName)) {
      DominoContextBean.submit(client -> {
        if(this.handler != null) {
          handler.stop();
        }
        handler = new OutputConsoleHandler();
        client.getServerAdmin().openServerConsole(serverName, handler);
      });
      getDisplay().asyncExec(() -> input.setEnabled(true));
    }
  }

  @Override
  protected void checkSubclass() {
    
  }
  
  private class OutputConsoleHandler implements ConsoleHandler {
    private boolean stop;
    
    private List<String> dataBuf = new ArrayList<>();

    @Override
    public void messageReceived(IConsoleLine line) {
      String dataLine = StringUtil.toString(line.getData());
      // Chop off the trailing \n
      dataLine = dataLine.substring(0, dataLine.length()-1);
      if(dataLine.startsWith("<ct") && !dataBuf.isEmpty()) { //$NON-NLS-1$
        // Then the buffer is the result of a previous dropped stream and should be truncated
        dataBuf.clear();
      }
      String xmlFragment = String.join("", dataBuf) + dataLine; //$NON-NLS-1$
      dataBuf.clear();
      
      // Read the <ct> pseudo-XML elements
      String[] nodes = xmlFragment.split(">\\n[\\r\\n]*"); //$NON-NLS-1$
      for(String fragment : nodes) {
        if(!fragment.endsWith("</ct")) { //$NON-NLS-1$
          dataBuf.add(fragment);
        } else {
          int textIndex = fragment.indexOf(">"); //$NON-NLS-1$
          int endIndex = fragment.lastIndexOf("</ct"); //$NON-NLS-1$
          String text = fragment.substring(textIndex+1, endIndex);
          getDisplay().asyncExec(() -> output.append(text + "\n")); //$NON-NLS-1$
        }
      }
    }
    
    public void stop() {
      this.stop = true;
    }

    @Override
    public boolean shouldStop() {
      return stop || isDisposed();
    }
    
  }
}
