/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.domino.jnx.example.swt.info;

import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.hcl.domino.DominoClient;
import com.hcl.domino.admin.IConsoleLine;
import com.hcl.domino.admin.ServerAdmin.ConsoleHandler;

import jakarta.enterprise.inject.spi.CDI;

public class ServerInfoPane extends AbstractInfoPane {
  private static class ConsolePaneHandler implements ConsoleHandler, AutoCloseable {
    private final long opened = System.currentTimeMillis();
    private final Display display;
    private final Text target;
    private boolean shouldStop;

    public ConsolePaneHandler(final Display display, final Text target) {
      this.display = display;
      this.target = target;
    }

    @Override
    public void close() {
      this.shouldStop = true;
    }

    @Override
    public void messageReceived(final IConsoleLine line) {
      final String newLine = line.getData();

      this.display.asyncExec(() -> {
        final String current = this.target.getText();
        if (current == null || current.isEmpty()) {
          this.target.setText(newLine);
        } else {
          this.target.setText(current + "\n" + newLine); //$NON-NLS-1$
        }
      });
    }

    @Override
    public boolean shouldStop() {
      if (this.target.isDisposed() || this.shouldStop || Thread.currentThread().isInterrupted()) {
        return true;
      }
      final boolean timedOut = System.currentTimeMillis() > this.opened + TimeUnit.HOURS.toMillis(1);
      if (timedOut) {
        return true;
      }
      return false;
    }

  }

  private final String serverName;

  public ServerInfoPane(final Composite parent, final String serverName) {
    super(parent, MessageFormat.format("Server: {0}", serverName));
    this.serverName = serverName;

    this.createChildren();
  }

  protected void createChildren() {
    try {
      final Composite actions = new Composite(this, SWT.NONE);
      final GridData actionBarData = new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1);
      actions.setLayoutData(actionBarData);
      actions.setLayout(new RowLayout());

      final Button connect = new Button(actions, SWT.PUSH);
      connect.setText("Connect Console");
      final RowData connectData = new RowData();
      connectData.width = 150;
      connect.setLayoutData(connectData);
      connect.setSize(100, connect.getSize().y);

      final Text console = new Text(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP | SWT.BORDER);
      console.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
      // TODO pick a non-named font -
      // https://stackoverflow.com/questions/221568/swt-os-agnostic-way-to-get-monospaced-font
      console.setFont(new Font(this.getDisplay(), "Consolas", 12, SWT.NORMAL)); //$NON-NLS-1$
      console.setEditable(false);

      final Text commandBox = new Text(this, SWT.BORDER | SWT.SEARCH);
      commandBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
      commandBox.setMessage("Console Command");
      commandBox.setEnabled(false);

      connect.addListener(SWT.Selection, evt -> {
        CDI.current().select(ExecutorService.class).get().submit(() -> {
          final DominoClient client = CDI.current().select(DominoClient.class).get();
          client.getServerAdmin().openServerConsole(this.serverName, new ConsolePaneHandler(this.getDisplay(), console));
        });

        this.getDisplay().asyncExec(() -> {
          commandBox.setEnabled(true);
        });
      });
      commandBox.addKeyListener(new KeyListener() {

        @Override
        public void keyPressed(final KeyEvent e) {
          if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
            final String command = commandBox.getText();

            ServerInfoPane.this.getDisplay().asyncExec(() -> {
              commandBox.setText(""); //$NON-NLS-1$
            });

            CDI.current().select(ExecutorService.class).get().submit(() -> {
              final DominoClient client = CDI.current().select(DominoClient.class).get();
              final String result = client.getServerAdmin().sendConsoleCommand(ServerInfoPane.this.serverName, command);
              ServerInfoPane.this.getDisplay().asyncExec(() -> {
                final String current = console.getText();
                if (current == null || current.isEmpty()) {
                  console.setText(result);
                } else {
                  console.setText(current + "\n" + result); //$NON-NLS-1$
                }
              });
            });

          }
        }

        @Override
        public void keyReleased(final KeyEvent e) {
        }
      });
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
