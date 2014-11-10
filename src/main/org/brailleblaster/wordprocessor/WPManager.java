/* BrailleBlaster Braille Transcription Application
 *
 * Copyright (C) 2014
 * American Printing House for the Blind, Inc. www.aph.org
 * and
 * ViewPlus Technologies, Inc. www.viewplus.com
 * and
 * Abilitiessoft, Inc. www.abilitiessoft.com
 * All rights reserved
 *
 * This file may contain code borrowed from files produced by various 
 * Java development teams. These are gratefully acknowledged.
 *
 * This file is free software; you can redistribute it and/or modify it
 * under the terms of the Apache 2.0 License, as given at
 * http://www.apache.org/licenses/
 *
 * This file is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE
 * See the Apache 2.0 License for more details.
 *
 * You should have received a copy of the Apache 2.0 License along with 
 * this program; see the file LICENSE.
 * If not, see
 * http://www.apache.org/licenses/
 *
 * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
 */

package org.brailleblaster.wordprocessor;

import java.util.LinkedList;

import org.brailleblaster.BBIni;
import org.brailleblaster.perspectives.Controller;
import org.brailleblaster.perspectives.Perspective;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.settings.Welcome;
import org.brailleblaster.util.PropertyFileManager;
import org.brailleblaster.util.YesNoChoice;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WPManager {
	/**
	 * This is the controller for the whole word processing operation. It is the
	 * entry point for the word processor, and therefore the only public class.
	 */
	private static Logger logger = LoggerFactory.getLogger(WPManager.class);

	public static Display display;
	private Shell shell;
	private FormLayout layout;
	private TabFolder folder;
	private FormData location;
	private BBMenu bbMenu;
	private BBStatusBar statusBar;
	private Perspective currentPerspective;
	private LinkedList<Controller> managerList;
	private Class<?> lastPerspective;
	private SelectionAdapter folderListener;
	private static final int MAX_NUM_DOCS = 4;// the max limit of total number
												// of docs can have at the same
												// time

	// This constructor is the entry point to the word processor. It gets things
	// set up, handles multiple documents, etc.
	public WPManager(String fileName) {
		
		managerList = new LinkedList<Controller>();
		checkLiblouisutdml();
		display = new Display();
		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText("BrailleBlaster");
		layout = new FormLayout();
		shell.setLayout(this.layout);

		folder = new TabFolder(this.shell, SWT.NONE);
		location = new FormData();
		location.left = new FormAttachment(0);
		location.right = new FormAttachment(100);
		location.top = new FormAttachment(10);
		location.bottom = new FormAttachment(98);
		folder.setLayoutData(location);
		statusBar = new BBStatusBar(shell);

		if (fileName == null)
			currentPerspective = Perspective.getPerspective(this,
					getDefaultPerspective(), null);
		else
			currentPerspective = Perspective.getPerspective(this,
					getDefaultPerspective(), fileName);

		managerList.add(currentPerspective.getController());
		currentPerspective.getController().setStatusBarText(statusBar);
		bbMenu = currentPerspective.getMenu();

		folder.addSelectionListener(folderListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = folder.getSelectionIndex();
				if (managerList.size() > 0) {
					if (bbMenu.getCurrent().getClass()
							.isInstance(managerList.get(index))) {
						bbMenu.setCurrent(managerList.get(index));
						currentPerspective.setController(managerList.get(index));
					} else {
						currentPerspective.dispose();
						currentPerspective = Perspective.restorePerspective(
								WPManager.this, managerList.get(index));
						bbMenu = currentPerspective.getMenu();
						bbMenu.setCurrent(managerList.get(index));
					}

					managerList.get(index).setStatusBarText(statusBar);
				}
			}
		});

		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event) {
				logger.info("Main Shell handling Close event, about to dispose the main Display");

				event.doit = close();
			}
		});

		setShellScreenLocation(display, shell);
		shell.setMaximized(true);

		new Welcome();
		shell.open();
		
		while (!shell.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Throwable e) {
/*				logger.debug("Uncaught exception detected", e);
				MessageBox questionBox = new MessageBox(this.shell,
						SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				questionBox.setMessage(lh.localValue("UnexpectedError.Message"));
				questionBox.setText(lh.localValue("UnexpectedError.Title"));
				int viewLogResult = questionBox.open();
				if (viewLogResult == SWT.YES) {
					LogViewerDialog viewerDialog = new LogViewerDialog(
							this.shell);
					viewerDialog.setText(lh.localValue("LogViewer.Title"));
					viewerDialog.open();
				}
*/
			}
		}
		display.dispose();
		
		if (lastPerspective != null)
			savePerspectiveSetting();
	}

	// Call on close events. Returns true if the whole app should close.
	public boolean close() {
		folder.removeSelectionListener(folderListener);
		int i = 0;
		while(managerList.size() > 0 && i < managerList.size()){
			int size = managerList.size();
			Controller temp = managerList.get(i);
			temp.close();		
			if(size == managerList.size())
				i++;
		}
		if(getList().size() == 0) {
			shell.dispose();
			bbMenu.writeRecentsToFile();
			return true;
		}
			
		folder.addSelectionListener(folderListener);
		return false;
	}
	
	private void setShellScreenLocation(Display display, Shell shell) {
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + ((bounds.width - rect.width) / 2);
		int y = bounds.y + ((bounds.height - rect.height) / 2);
		shell.setLocation(x, y);
	}

	public void addDocumentManager(String fileName) {
		if (managerList.size() == 0) {
			Controller c = Perspective.getNewController(this,
					currentPerspective.getType(), fileName);
			managerList.add(c);
			currentPerspective.setController(c);
			bbMenu.setCurrent(managerList.getLast());
			setSelection();
		} else {
			Controller c = Perspective.getNewController(this,
					currentPerspective.getType(), fileName);
			managerList.add(c);
			bbMenu.setCurrent(managerList.getLast());
			currentPerspective.setController(c);
			setSelection();
		}
	}

	public void setSelection() {
		int index = this.managerList.size() - 1;
		this.folder.setSelection(index);
	}

	void checkLiblouisutdml() {
		if (BBIni.haveLiblouisutdml()) {
			return;
		}
		if (new YesNoChoice("The Braille facility is not usable."
				+ " See the log." + " Do you wish to continue?", false).result == SWT.NO) {
			System.exit(1);
		}
	}

	public void swapPerspectiveController(Class<?> controllerClass) {
		int index = folder.getSelectionIndex();
		if (index != -1) {
			currentPerspective.getController().getArchiver().pauseAutoSave();
			currentPerspective.dispose();
			currentPerspective.getController().dispose();
			currentPerspective = Perspective.getDifferentPerspective(
					currentPerspective, this, controllerClass,
					managerList.get(index).getDoc());
			managerList.set(index, currentPerspective.getController());
			bbMenu = currentPerspective.getMenu();
			managerList.get(index).setStatusBarText(statusBar);
			managerList.get(index).restore(this);
			currentPerspective.getController().getArchiver()
					.resumeAutoSave(null, null);
		} else {
			currentPerspective.getController().getArchiver().pauseAutoSave();
			currentPerspective.dispose();
			currentPerspective.getController().dispose();
			currentPerspective = Perspective.getDifferentPerspective(
					currentPerspective, this, controllerClass, null);
			bbMenu = currentPerspective.getMenu();
			bbMenu.setCurrent(null);
			currentPerspective.getController().getArchiver()
					.resumeAutoSave(null, null);
		}

		lastPerspective = controllerClass;
	}

	private Class<?> getDefaultPerspective() {
		PropertyFileManager prop = BBIni.getPropertyFileManager();
		String defaultPerspective = prop.getProperty("defaultPerspective");

		if (defaultPerspective == null) {
			prop.save("defaultPerspective", Manager.class.getCanonicalName()
					.toString());
			return Manager.class;
		} else {
			try {
				return Class.forName(defaultPerspective);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	private void savePerspectiveSetting() {
		PropertyFileManager prop = BBIni.getPropertyFileManager();
		prop.save("defaultPerspective", lastPerspective.getCanonicalName()
				.toString());
	}

	public void removeController(Controller c) {
		managerList.remove(c);
	}

	static int getMaxNumDocs() {
		return MAX_NUM_DOCS;
	}

	public static Display getDisplay() {
		return display;
	}

	public Shell getShell() {
		return shell;
	}

	public TabFolder getFolder() {
		return this.folder;
	}

	public LinkedList<Controller> getList() {
		return this.managerList;
	}

	public BBStatusBar getStatusBar() {
		return this.statusBar;
	}

	public BBMenu getMainMenu() {
		return bbMenu;
	}
}