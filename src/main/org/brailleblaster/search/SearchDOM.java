package org.brailleblaster.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
import org.brailleblaster.perspectives.braille.viewInitializer.ViewInitializer;
import org.brailleblaster.perspectives.braille.views.wp.TextView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

public class SearchDOM extends Dialog {

	// Imports for DOM
	MapList maplist;
	TextMapElement textMap;
	TextView tv;

	// Variables for the search dialog
	protected Object result;
	protected Shell shlFindreplace;
	private Shell errorMessageShell;
	private Shell replaceAllShell;
	Display display = null;
	private Manager man = null;
	Combo searchCombo = null;
	Combo replaceCombo = null;
	private final int SCH_FORWARD = 0;
	private final int SCH_BACKWARD = 1;
	private int searchDirection = SCH_FORWARD;
	private final int SCH_SCOPE_ALL = 0;
	private final int SCH_SELECT_LINES = 1;
	private int searchScope = SCH_SCOPE_ALL;
	private final int SCH_CASE_ON = 0;
	private final int SCH_CASE_OFF = 1;
	private int searchCaseSensitive = SCH_CASE_OFF;
	private final int SCH_WRAP_ON = 0;
	private final int SCH_WRAP_OFF = 1;
	private int searchWrap = SCH_WRAP_OFF;
	private final int SCH_WHOLE_ON = 0;
	private final int SCH_WHOLE_OFF = 1;
	private int searchWholeWord = SCH_WHOLE_OFF;
	private String[] searchList = new String[50];
	private String[] replaceList = new String[50];
	Map<String, String> searchSettings = new HashMap<String, String>();
	int searchArraySize;
	Map<String, String> searchMap = new HashMap<String, String>();
	int replaceArraySize;
	Map<String, String> replaceMap = new HashMap<String, String>();

	// Variables for searching
	private int startCharIndex = 0;
	private int endCharIndex = 0;
	int numberOfLoops;
	int oldCursorPos;
	int oldTopIndex;
	int numberReplaceAlls;

	public SearchDOM(Shell parent, int style, Manager brailleViewController,
			MapList list) {
		super(parent, style);
		setText("SWT Dialog");
		man = brailleViewController;
		// maplist = new MapList(man);
		this.maplist = list;
	}

	public Object open() {

		createContents();
		shlFindreplace.open();
		shlFindreplace.layout();

		display = getParent().getDisplay();
		display.addFilter(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(Event e) {

				// If user presses F3 key, do find/replace.
				if (e.keyCode == SWT.F3) {
					if (find() == true && replaceCombo.getText().length() > 0)
						man.getText().copyAndPaste(replaceCombo.getText(),
								startCharIndex, endCharIndex);
				} // if(e.keyCode == SWT.F3)

			} // handleEvent()

		}); // addFilter()

		while (!shlFindreplace.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		// }
		return result;

	}

	private void createContents() {
		shlFindreplace = new Shell(getParent(), SWT.DIALOG_TRIM);
		setPanelSize();

		shlFindreplace.setText("Find/Replace");
		shlFindreplace.setVisible(true);

		GridLayout gl_shlFindreplace = new GridLayout(1, true);
		gl_shlFindreplace.marginTop = 10;
		gl_shlFindreplace.marginLeft = 5;
		shlFindreplace.setLayout(gl_shlFindreplace);

		Label lblFind = new Label(shlFindreplace, SWT.NONE);
		lblFind.setText("Find:");

		searchCombo = new Combo(shlFindreplace, SWT.NONE);
		searchCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false));
		if (searchList != null) {
			for (int i = 0; i < searchArraySize; i++) {
				searchCombo.add(searchList[i]);
				searchCombo.setText(searchList[i].toString());
			}// for
		}// if

		searchCombo.getData();
		searchCombo.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {

				String newText = searchCombo.getText();
				if (!searchMap.containsValue(newText)) {
					searchCombo.add(newText);
					searchList[searchArraySize] = newText;
					searchMap.put(newText, newText);
					searchArraySize++;
				}// if

			}// key traversed
		});// addTraverseListener

		searchCombo.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				String newText = searchCombo.getText();
				if (!searchMap.containsValue(newText)) {
					searchCombo.add(newText);
					searchList[searchArraySize] = newText;
					searchMap.put(newText, newText);
					searchArraySize++;
				}// if
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				// do nothing for now
			}
		});

		Label lblReplaceWith = new Label(shlFindreplace, SWT.NONE);
		lblReplaceWith.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));
		lblReplaceWith.setText("Replace with:");

		replaceCombo = new Combo(shlFindreplace, SWT.NONE);
		replaceCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 2, 1));
		if (replaceList != null) {
			for (int i = 0; i < replaceArraySize; i++) {
				replaceCombo.add(replaceList[i]);
				replaceCombo.setText(replaceList[i].toString());
			}// for
		}// if
		replaceCombo.getData();
		replaceCombo.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {

				String newText = replaceCombo.getText();
				if (!replaceMap.containsValue((newText))) {
					replaceCombo.add(newText);
					replaceList[replaceArraySize] = newText;
					replaceMap.put(newText, newText);
					replaceArraySize++;
				}// if
			}// key traversed
		});// addTraverseListener
		replaceCombo.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				String newText = replaceCombo.getText();
				if (!replaceMap.containsValue(newText)) {
					replaceCombo.add(newText);
					replaceList[replaceArraySize] = newText;
					replaceMap.put(newText, newText);
					replaceArraySize++;
				}// if
			}

			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});

		Group grpDirection = new Group(shlFindreplace, SWT.NONE);
		grpDirection.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		grpDirection.setLayout(new RowLayout(SWT.VERTICAL));
		grpDirection.setText("Direction");

		Button forwardRadioBtn = new Button(grpDirection, SWT.RADIO);
		if (searchSettings.containsValue("forward")) {
			forwardRadioBtn.setSelection(true);
		}// if
		forwardRadioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchDirection = SCH_FORWARD;
				searchSettings.put("searchDirectionString", "forward");
			}// event
		});// listener
		forwardRadioBtn.setBounds(10, 21, 90, 16);
		forwardRadioBtn.setText("Forward");

		Button backwardRadioBtn = new Button(grpDirection, SWT.RADIO);
		if (searchSettings.containsValue("backward")) {
			backwardRadioBtn.setSelection(true);
		}// if
		else {
			forwardRadioBtn.setSelection(true);
		}
		backwardRadioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchDirection = SCH_BACKWARD;
				searchSettings.put("searchDirectionString", "backward");
			}// event
		});// listener
		backwardRadioBtn.setBounds(10, 43, 90, 16);
		backwardRadioBtn.setText("Backward");

		Group grpScope = new Group(shlFindreplace, SWT.NONE);
		grpScope.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false,
				1, 1));
		grpScope.setLayout(new RowLayout(SWT.VERTICAL));
		grpScope.setText("Scope");

		Button allRadioBtn = new Button(grpScope, SWT.RADIO);
		allRadioBtn.setSelection(true);
		allRadioBtn.setText("All");
		allRadioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchScope = SCH_SCOPE_ALL;
			}
		});
		allRadioBtn.setEnabled(false);

		Button selectedLinesBtn = new Button(grpScope, SWT.RADIO);
		selectedLinesBtn.setText("Selected Lines");
		selectedLinesBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchScope = SCH_SELECT_LINES;
			}
		});
		selectedLinesBtn.setEnabled(false);

		Group grpOptions = new Group(shlFindreplace, SWT.NONE);
		grpOptions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		grpOptions.setLayout(new GridLayout(2, false));
		grpOptions.setText("Options");

		Button caseSensitiveCheck = new Button(grpOptions, SWT.CHECK);
		caseSensitiveCheck.setText("Case sensitive");

		if (searchSettings.containsValue("caseSensitive")) {
			caseSensitiveCheck.setSelection(true);
		}// previous search

		caseSensitiveCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (!searchSettings.containsValue("caseSensitive")) {
					searchSettings.put("caseSensitive", "caseSensitive");
				} else {
					searchSettings.put("caseSensitive", "notCaseSensitive");
				}
				if (searchCaseSensitive == SCH_CASE_OFF) {
					searchCaseSensitive = SCH_CASE_ON;
				} else {
					searchCaseSensitive = SCH_CASE_OFF;
				}
			}
		});

		Button wholeWordCheck = new Button(grpOptions, SWT.CHECK);
		wholeWordCheck.setText("Whole word");

		if (searchSettings.containsValue("wholeWord")) {
			wholeWordCheck.setSelection(true);
		}// previous search

		wholeWordCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (!searchSettings.containsValue("wholeWord")) {
					searchSettings.put("wholeWord", "wholeWord");
				} else {
					searchSettings.put("wholeWord", "notWholeWord");
				}

				if (searchWholeWord == SCH_WHOLE_OFF)
					searchWholeWord = SCH_WHOLE_ON;
				else
					searchWholeWord = SCH_WHOLE_OFF;
			}
		});

		Button regExpressionsCheck = new Button(grpOptions, SWT.CHECK);
		regExpressionsCheck.setText("Regular expressions");
		regExpressionsCheck.setEnabled(false);

		Button wrapSearchCheck = new Button(grpOptions, SWT.CHECK);
		wrapSearchCheck.setText("Wrap search");

		if (searchSettings.containsValue("wrapSearch")) {
			wrapSearchCheck.setSelection(true);
		}// if

		wrapSearchCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (!searchSettings.containsValue("wrapSearch")) {
					searchSettings.put("wrapSearch", "wrapSearch");
				} else {
					searchSettings.put("wrapSearch", "notWrapSearch");
				}
				if (searchWrap == SCH_WRAP_OFF)
					searchWrap = SCH_WRAP_ON;
				else
					searchWrap = SCH_WRAP_OFF;
			}
		});

		Button incrementalCheck = new Button(grpOptions, SWT.CHECK);
		incrementalCheck.setText("Incremental");
		incrementalCheck.setEnabled(false);

		Button findBtn = new Button(shlFindreplace, SWT.NONE);
		findBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		findBtn.setText("Find");
		shlFindreplace.setDefaultButton(findBtn);
		findBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				numberOfLoops = 0;

				if (searchDirection == SCH_FORWARD) {
					if (searchWrap == SCH_WRAP_ON) {
						if (!find())
							createErrorMessage();
					}// if findFwdWrap
					else {
						if (!find())
							createErrorMessage();
					}// else findFwdNoWrap
				}// if searchForward
				else {
					if (searchWrap == SCH_WRAP_ON) {
						if (!find())
							createErrorMessage();
					}// if findBwdWrap
					else {
						if (!find())
							createErrorMessage();
					}// else findBwdNoWrap
				}// else searchBackward

			} // widgetSelected()

		}); // btnFind.addSelectionListener()

		Button replaceFindBtn = new Button(shlFindreplace, SWT.NONE);
		replaceFindBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		replaceFindBtn.setText("Replace/Find");
		replaceFindBtn.setEnabled(false);

		Button replaceBtn = new Button(shlFindreplace, SWT.NONE);
		replaceBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		replaceBtn.setText("Replace");
		replaceBtn.setEnabled(true);
		replaceBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				numberOfLoops = 0;

				if (searchDirection == SCH_FORWARD) {
					if (searchWrap == SCH_WRAP_ON) {
						if (find()) {
							man.getText().copyAndPaste(replaceCombo.getText(),
									startCharIndex, endCharIndex);
						} else {
							createErrorMessage();
						}
					}// if replaceFwdWrap
					else {
						if (find()) {
							man.getText().copyAndPaste(replaceCombo.getText(),
									startCharIndex, endCharIndex);
						} else {
							createErrorMessage();
						}
					}// else replaceFwdNoWrap
				}// if searchForward
				else {
					if (searchWrap == SCH_WRAP_ON) {
						if (find()) {
							man.getText().copyAndPaste(replaceCombo.getText(),
									startCharIndex, endCharIndex);
						} else {
							createErrorMessage();
						}
					}// if replaceBwdWrap
					else {
						if (find()) {
							man.getText().copyAndPaste(replaceCombo.getText(),
									startCharIndex, endCharIndex);
						} else {
							createErrorMessage();
						}
					}// else replaceBwdNoWrap
				}// else searchBackward
			} // widgetSelected()

		}); // replaceBtn.addSelectionListener()

		Button replaceAllBtn = new Button(shlFindreplace, SWT.NONE);
		replaceAllBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		replaceAllBtn.setText("Replace All");
		replaceAllBtn.setEnabled(true);
		replaceAllBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				numberOfLoops = 0;
				numberReplaceAlls = 0;

				if (searchDirection == SCH_FORWARD) {
					// Replace every instance of word.
					int oldTopIndex = man.getTextView().getTopIndex();
					int oldCursorPos = man.getText().getCursorOffset();
					man.getText().setCursor(0, man);
					if (find() == true) {
						do {
							man.getText().copyAndPaste(replaceCombo.getText(),
									startCharIndex, endCharIndex);
							man.getTextView().setTopIndex(oldTopIndex);
							man.getText().setCursorOffset(oldCursorPos);
						}// do
						while (find() == true);
						replaceAllMessage();
					}// if findStr==true
					else {
						createErrorMessage();
					}// else nothing found

				}// if searchForward
				else {
					int oldTopIndex = man.getTextView().getTopIndex();
					int oldCursorPos = man.getText().getCursorOffset();
					TextView tv = man.getText();
					int numChars = tv.view.getText().length();
					man.getText().setCursor(numChars, man);
					if (find() == true) {
						do {

							man.getText().copyAndPaste(replaceCombo.getText(),
									startCharIndex, endCharIndex);
							man.getTextView().setTopIndex(oldTopIndex);
							man.getText().setCursorOffset(oldCursorPos);
						}// do
						while (find() == true);
						replaceAllMessage();
					}// if findStr == true
					else {
						createErrorMessage();
					}// else if nothing found
				}// if searchBackward
			} // widgetSelected()
		}); // replaceBtn.addSelectionListener()

		Button closeBtn = new Button(shlFindreplace, SWT.NONE);
		closeBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false,
				1, 1));
		closeBtn.setText("Close");
		closeBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlFindreplace.close();

			} // widgetSelected()

		}); // closeBtn.addSelectionListener...

		shlFindreplace.pack(true);
	}// createPreviousContents

	public void createErrorMessage() {

		display = getParent().getDisplay();
		display.beep();
		if (errorMessageShell != null) {
			errorMessageShell.close();
		}
		errorMessageShell = new Shell(display, SWT.DIALOG_TRIM);

		errorMessageShell.setLayout(new GridLayout(1, true));
		errorMessageShell.setText("Find/Replace Error");
		errorMessageShell.setLocation(500, 250);

		Label label = new Label(errorMessageShell, SWT.RESIZE);
		label.setText("BrailleBlaster cannot find your word in the document");

		Button ok = new Button(errorMessageShell, SWT.NONE);
		ok.setText("OK");
		GridData errorMessageData = new GridData(SWT.HORIZONTAL);
		ok.setLayoutData(errorMessageData);
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				errorMessageShell.setVisible(false);
				display.sleep();
				searchCombo.setFocus();
			}// widgetSelected

		});// selectionListener
		errorMessageShell.pack(true);
		errorMessageShell.open();

		errorMessageShell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
				errorMessageShell.setVisible(false);
			}
		});
	}// createErrorMessage

	private void replaceAllMessage() {
		display = getParent().getDisplay();
		display.beep();
		if (replaceAllShell != null) {
			replaceAllShell.close();
		}

		replaceAllShell = new Shell(display, SWT.DIALOG_TRIM);

		replaceAllShell.setLayout(new GridLayout(1, true));
		replaceAllShell.setLocation(500, 250);

		Label label0 = new Label(replaceAllShell, SWT.RESIZE);
		label0.setText("Replace All Complete");

		Label label = new Label(replaceAllShell, SWT.RESIZE);
		label.setText("BrailleBlaster replaced " + numberReplaceAlls + " words");

		Button ok = new Button(replaceAllShell, SWT.NONE);
		ok.setText("OK");
		GridData replaceAllData = new GridData(SWT.HORIZONTAL);
		ok.setLayoutData(replaceAllData);
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				replaceAllShell.setVisible(false);
				display.sleep();
			}// widgetSelected

		});// selectionListener
		replaceAllShell.pack(true);
		replaceAllShell.open();

		replaceAllShell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
				replaceAllShell.setVisible(false);
			}
		});
	}

	public boolean find() {

		TextView tv = man.getText();
		String view = tv.view.getText();
		/*
		 * .query(String.format("//*[text()[contains(.,'%s')]]", search),
		 * context);
		 */
		Document doc = man.getDoc();
		String search = searchCombo.getText();
		XPathContext context = null;
		Nodes nodes = (Nodes) doc
				.query(String.format("//*[text()[contains(.,'%s')]]", search),
						context);
		System.out.println("node name " + nodes.get(0).toString());
		ArrayList<Node> nodeList = new ArrayList<Node>();
		for (int i = 0; i < nodes.size(); i++) {
			for (int j = 0; j < nodes.get(i).getChildCount(); j++) {
				if (!nodes.get(i).getChild(j).toString()
						.equals("[nu.xom.Element: brl]"))
					nodeList.add(nodes.get(i).getChild(j));
			}
		}
		System.out.println("nodeList" + nodeList.toString());
		System.out.println("NUMBER OF NODES WITH MATCH " + nodes.size());
		if (nodes.size() > 0) {
			Node node = nodes.get(0).getChild(0);
			int TMEIndex = maplist.findNodeIndex(node, 0);
			maplist.setCurrent(TMEIndex);
			TextMapElement t = maplist.get(TMEIndex);
			System.out.println(maplist.getCurrent().parentElement()
					.query("//text"));
			tv.view.setCaretOffset(maplist.getCurrent().start);
			tv.view.setTopIndex(maplist.getCurrent().start);

			int indexOfSearch = view.indexOf(search,0);
			tv.view.setSelection(indexOfSearch,indexOfSearch+search.length());
			System.out.println("NODE INDEX " + TMEIndex);
			return true;
		}
		return false;
	}

	private void setPanelSize() {
		Monitor primary = shlFindreplace.getDisplay().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		int x = (bounds.width / 2) - ((bounds.width / 6) / 2);
		int y = (bounds.height / 2) - ((bounds.height / 2) / 2);
		shlFindreplace.setSize(bounds.width / 3, (int) (bounds.height / 1.5));
		shlFindreplace.setLocation(x, y);
	}

} // class SearchDialog...
