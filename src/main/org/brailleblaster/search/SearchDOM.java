package org.brailleblaster.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.XPathContext;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.mapping.elements.TextMapElement;
import org.brailleblaster.perspectives.braille.mapping.maps.MapList;
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

	SearchDialog sd;

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
	int numberReplaceAlls;
	String currentSearch;
	Nodes nodes;
	ArrayList<Integer> sectionArray = new ArrayList<>();
	ArrayList<Integer> visitedArray = new ArrayList<>();
	int indexOfSearchForView;
	int indexOfNode;
	int textIndex;
	int totalWords;
	private int sectionIndex;
	boolean found;
	private int startCharIndex;
	private int endCharIndex;
	private int numberOfLoops;

	public SearchDOM(Shell parent, int style, Manager brailleViewController,
			MapList list) {
		super(parent, style);
		setText("SWT Dialog");
		man = brailleViewController;
		this.maplist = list;
	}

	public Object open() {

		createContents();
		shlFindreplace.open();
		shlFindreplace.layout();
		display = getParent().getDisplay();

		while (!shlFindreplace.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
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
			}
		}

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
				}
			}
		});

		searchCombo.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				String newText = searchCombo.getText();
				if (!searchMap.containsValue(newText)) {
					searchCombo.add(newText);
					searchList[searchArraySize] = newText;
					searchMap.put(newText, newText);
					searchArraySize++;
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
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
			}
		}
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
				}
			}
		});
		replaceCombo.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				String newText = replaceCombo.getText();
				if (!replaceMap.containsValue(newText)) {
					replaceCombo.add(newText);
					replaceList[replaceArraySize] = newText;
					replaceMap.put(newText, newText);
					replaceArraySize++;
				}
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
		}
		forwardRadioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchDirection = SCH_FORWARD;
				searchSettings.put("searchDirectionString", "forward");
			}
		});
		forwardRadioBtn.setBounds(10, 21, 90, 16);
		forwardRadioBtn.setText("Forward");

		Button backwardRadioBtn = new Button(grpDirection, SWT.RADIO);
		if (searchSettings.containsValue("backward")) {
			backwardRadioBtn.setSelection(true);
		} else {
			forwardRadioBtn.setSelection(true);
		}
		backwardRadioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchDirection = SCH_BACKWARD;
				searchSettings.put("searchDirectionString", "backward");
			}
		});
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
		}

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
		}

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
				// nodeMap = new HashMap<>();
				// if (isNewSearch()) {
				// textIndexArray = new ArrayList<>();
				// sectionArray = new ArrayList<>();
				// indexOfSearchForView = 0;
				// textIndex = 0;
				// totalWords = 0;
				found = find();
				// }
				if (found == true) {
					putFoundInView();
				} else
					createErrorMessage();
				// if (find()) {
				// numberOfLoops = 0;
				//
				// if (searchDirection == SCH_FORWARD) {
				// if (searchWrap == SCH_WRAP_ON) {
				// if (!findFwdWrap())
				// createErrorMessage();
				// }// if findFwdWrap
				// else {
				// if (!findFwdNoWrap())
				// createErrorMessage();
				// }// else findFwdNoWrap
				// }// if searchForward
				// else {
				// if (searchWrap == SCH_WRAP_ON) {
				// if (!findBackWrap())
				// createErrorMessage();
				// }// if findBwdWrap
				// else {
				// if (!findBackNoWrap())
				// createErrorMessage();
				// }// else findBwdNoWrap
				// }// else searchBackward
				// } else {
				// createErrorMessage();
				// }
			}

		});

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

				if (isNewSearch()) {
					indexOfNode = 0;
					indexOfSearchForView = 0;
				}
				if (find()) {
					// putFoundInView();
					man.getText().copyAndPaste(replaceCombo.getText(),
							textIndex, textIndex + currentSearch.length());
				} else {
					createErrorMessage();
				}
			}

		});

		Button replaceAllBtn = new Button(shlFindreplace, SWT.NONE);
		replaceAllBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		replaceAllBtn.setText("Replace All");
		replaceAllBtn.setEnabled(true);
		replaceAllBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				numberReplaceAlls = 0;
				if (isNewSearch()) {
					indexOfNode = 0;
					indexOfSearchForView = 0;
					if (find()) {
						do {
							// putFoundInView();
							man.getText().copyAndPaste(replaceCombo.getText(),
									textIndex,
									textIndex + currentSearch.length());
							numberReplaceAlls++;
						} while (indexOfNode < nodes.size());
						replaceAllMessage();
					} else {
						createErrorMessage();
					}
				} else {
					do {
						// putFoundInView();
						man.getText().copyAndPaste(replaceCombo.getText(),
								textIndex, textIndex + currentSearch.length());
						numberReplaceAlls++;
					} while (indexOfNode < nodes.size());
					replaceAllMessage();
				}
			}
		});

		Button closeBtn = new Button(shlFindreplace, SWT.NONE);
		closeBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false,
				1, 1));
		closeBtn.setText("Close");
		closeBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlFindreplace.close();
			}
		});
		shlFindreplace.pack(true);
	}

	private void setPanelSize() {
		Monitor primary = shlFindreplace.getDisplay().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		int x = (bounds.width / 2) - ((bounds.width / 4) / 2);
		int y = (bounds.height / 2) - ((bounds.height / 2) / 2);
		shlFindreplace.setSize(bounds.width / 3, (int) (bounds.height / 1.5));
		shlFindreplace.setLocation(x * 2, y);
	}

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
			}

		});
		errorMessageShell.pack(true);
		errorMessageShell.open();

		errorMessageShell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
				errorMessageShell.setVisible(false);
			}
		});
	}

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
				searchCombo.setFocus();
			}

		});
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

		Document doc = man.getDoc();
		String nameSpace = doc.getRootElement().getNamespaceURI();
		currentSearch = searchCombo.getText();
		XPathContext context = new nu.xom.XPathContext("dtb", nameSpace);
		if (searchCaseSensitive == SCH_CASE_OFF) {
			currentSearch = currentSearch.toLowerCase();
			nodes = doc
					.query(String
							.format("//text()[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'%s')]"
									+ "[not(ancestor::dtb:brl)]", currentSearch),
							context);
		} else {
			nodes = doc.query(String.format("//text()[contains (.,'%s')]"
					+ "[not(ancestor::dtb:brl)]", currentSearch), context);
		}

		if (nodes.size() > 0) {
			for (int i = 0; i < nodes.size(); i++) {
				getNodeIndices(nodes.get(i));
			}
			System.out.println("sectionArray " + sectionArray.toString());
			System.out.println("sectionArray size " + sectionArray.size());
			System.out.println("NUMBER OF NODES WITH MATCH " + nodes.size());
			System.out.println();
			return true;
		}
		return false;
	}

	public void getNodeIndices(Node node) {
		int[] completeIndex = man.getNodeIndexAllSections(node);
		if (completeIndex != null) {
			int section = completeIndex[0];
			if (!sectionArray.contains(section)) {
				sectionArray.add(section);
				visitedArray.add(0);
				Collections.sort(sectionArray);
			}

		}
	}

	public void putFoundInView() {
		if (searchDirection == SCH_FORWARD) {
			if (searchWrap == SCH_WRAP_ON) {
				if (!findForward() && sectionArray.size() > 0) {
					man.resetSection(getNextSection());
					findForward();
				} else
					findForward();
			} else {
				if (!findForward()) {
					if (getNextSection() == -1)
						createErrorMessage();
					else
						man.resetSection(getNextSection());
				}
			}
		} else {
			if (searchWrap == SCH_WRAP_ON) {
				if (!findBackNoWrap())
					man.resetSection(getNextSection());
			} else {
				if (sectionIndex != sectionArray.size() - 1)
					man.resetSection(getNextSection());
				else
					createErrorMessage();
			}
		}

	}

	public int getNextSection() {

		if (searchDirection == SCH_FORWARD) {
			for (int i = 0; i < visitedArray.size(); i++) {
				if (visitedArray.get(i) == 0) {
					visitedArray.set(i, 1);
					return sectionArray.get(i);
				}
			}
		} else {
			for (int i = visitedArray.size(); i > 0; i--) {
				if (visitedArray.get(i) == 0) {
					visitedArray.set(i, 1);
					return sectionArray.get(i);
				}
			}
		}
		return -1;
	}

	public boolean isNewSearch() {
		if (!searchCombo.getText().equals(currentSearch)) {
			return true;
		}
		return false;
	}

	public boolean findForward() {

		TextView tv = man.getText();
		String textStr = tv.view.getText();
		String findMeStr = currentSearch;
		int numChars = textStr.length();

		if (tv.view.getCharCount() == 0)
			return false;

		if (findMeStr.length() > numChars)
			return false;

		startCharIndex = tv.view.getCaretOffset();
		endCharIndex = startCharIndex + findMeStr.length();

		while (startCharIndex < numChars && endCharIndex < (numChars + 1)) {
			String curViewSnippet = textStr.substring(startCharIndex,
					endCharIndex);

			if (searchCaseSensitive == SCH_CASE_OFF) {
				curViewSnippet = curViewSnippet.toLowerCase();
				findMeStr = findMeStr.toLowerCase();
			}

			if (curViewSnippet.matches(findMeStr) == true) {

				boolean haveAmatch = true;
				if (searchWholeWord == SCH_WHOLE_ON) {
					if (startCharIndex - 1 >= 0)
						if (textStr.substring(startCharIndex - 1,
								startCharIndex).matches("^[\\pL\\pN]*$") == true)
							haveAmatch = false;
					if (endCharIndex + 1 < numChars)
						if (textStr.substring(endCharIndex, endCharIndex + 1)
								.matches("^[\\pL\\pN]*$") == true)
							haveAmatch = false;
				}

				if (haveAmatch == true) {
					tv.view.setSelection(startCharIndex, endCharIndex);
					tv.view.setTopIndex(tv.view.getLineAtOffset(startCharIndex));
					return true;
				}
			}
			startCharIndex++;
			endCharIndex++;
		}
		return false;
	}

	public boolean findBackNoWrap() {
		// /////////////////////////////////////////////////////////////////
		// Searches document for string in our combo box.
		// Returns true if one was found.
		// Grab text view.
		TextView tv = man.getText();

		String textStr = tv.view.getText();

		// Are there any characters in the text view? If there
		// are no characters, we probably don't have a document
		// loaded yet.
		if (tv.view.getCharCount() == 0)
			return false;

		// Grab search string.
		String findMeStr = searchCombo.getText();

		// Get number of characters in text view.
		int numChars = textStr.length();

		// If the search string is larger than the total number of
		// characters in the view, don't bother.
		if (findMeStr.length() > numChars)
			return false;

		// If there is selection text, that means we're still looking at
		// what we found earlier. Move past it.
		if (tv.view.getSelectionText().length() == findMeStr.length())
			tv.view.setCaretOffset(startCharIndex);

		// Get current cursor position.
		endCharIndex = tv.view.getCaretOffset();
		startCharIndex = endCharIndex - findMeStr.length();

		// Scour the view for the search string.
		while (startCharIndex >= 0 && endCharIndex > 0) {
			// Get current snippet of text we're testing.
			String curViewSnippet = textStr.substring(startCharIndex,
					endCharIndex);

			// Should we be checking case sensitive version?
			if (searchCaseSensitive == SCH_CASE_OFF) {
				curViewSnippet = curViewSnippet.toLowerCase();
				findMeStr = findMeStr.toLowerCase();
			}

			// Compare the two strings. Is there a match?
			if (curViewSnippet.matches(findMeStr) == true) {
				// Whole word?
				boolean haveAmatch = true;
				if (searchWholeWord == SCH_WHOLE_ON) {
					// "^[\pL\pN]*$";
					if (startCharIndex - 1 >= 0)
						if (textStr.substring(startCharIndex - 1,
								startCharIndex).matches("^[\\pL\\pN]*$") == true)
							haveAmatch = false;
					if (endCharIndex + 1 < numChars)
						if (textStr.substring(endCharIndex, endCharIndex + 1)
								.matches("^[\\pL\\pN]*$") == true)
							haveAmatch = false;

				} // if( searchWholeWord...

				// Update view if we have a match.
				if (haveAmatch == true) {
					// Set cursor and view to point to search string we
					// found.
					tv.view.setSelection(startCharIndex, endCharIndex);
					tv.view.setTopIndex(tv.view.getLineAtOffset(startCharIndex));
					// foundStr = tv.view.getSelectionText();

					// Found it; break.
					return true;

				} // if( haveAmatch = true)

			} // if( curViewSnippet...

			// Move back a character.
			startCharIndex--;
			endCharIndex--;

		} // while( startCharIndex...

		// If for some reason we get here, couldn't find a matching string.
		return false;

	}// findBackNoWrap

} // class SearchDOM

/*
 * XPath tries!! .query(String.format("//*[text()[contains(.,'%s')]]", search),
 * context);
 * 
 * doc.query(String.format
 * ("//*[text()[contains(.,'%s')]][not(ancestor-or-self::brl)]",
 * search),context);
 * 
 * doc.query(String.format(
 * "//*[text()[contains(.,'%s')]][not(ancestor-or-self::[node()[name() == 'brl']])]"
 * , search), context);
 * 
 * nodes = doc.query(String.format(
 * "//text()[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'%s')]"
 * + "[not(ancestor::dtb:brl)]", currentSearch), context);
 * 
 * -------------------------previous
 * replace--------------------------------------
 * 
 * numberOfLoops = 0;
 * 
 * if (searchDirection == SCH_FORWARD) { if (searchWrap == SCH_WRAP_ON) { if
 * (find()) { man.getText().copyAndPaste(replaceCombo.getText(), startCharIndex,
 * endCharIndex); } else { createErrorMessage(); } } else { if (find()) {
 * man.getText().copyAndPaste(replaceCombo.getText(), startCharIndex,
 * endCharIndex); } else { createErrorMessage(); } } } else { if (searchWrap ==
 * SCH_WRAP_ON) { if (find()) {
 * man.getText().copyAndPaste(replaceCombo.getText(), startCharIndex,
 * endCharIndex); } else { createErrorMessage(); } } else { if (find()) {
 * man.getText().copyAndPaste(replaceCombo.getText(), startCharIndex,
 * endCharIndex); } else { createErrorMessage(); } } }
 * -----------------------------------------------previous
 * replaceall------------
 * 
 * numberReplaceAlls = 0;
 * 
 * if (searchDirection == SCH_FORWARD) { int oldTopIndex =
 * man.getTextView().getTopIndex(); int oldCursorPos =
 * man.getText().getCursorOffset(); man.getText().setCursor(0, man); if (find()
 * == true) { do { man.getText().copyAndPaste(replaceCombo.getText(),
 * startCharIndex, endCharIndex); man.getTextView().setTopIndex(oldTopIndex);
 * man.getText().setCursorOffset(oldCursorPos); } while (find() == true);
 * replaceAllMessage(); } else { createErrorMessage(); }
 * 
 * } else { int oldTopIndex = man.getTextView().getTopIndex(); int oldCursorPos
 * = man.getText().getCursorOffset(); TextView tv = man.getText(); int numChars
 * = tv.view.getText().length(); man.getText().setCursor(numChars, man); if
 * (find() == true) { do {
 * 
 * man.getText().copyAndPaste(replaceCombo.getText(), startCharIndex,
 * endCharIndex); man.getTextView().setTopIndex(oldTopIndex);
 * man.getText().setCursorOffset(oldCursorPos); } while (find() == true);
 * replaceAllMessage(); } else { createErrorMessage(); } }
 */
// TextView tv = man.getText();
// String view;
// if (searchCaseSensitive == SCH_CASE_OFF) {
// view = tv.view.getText().toLowerCase();
// currentSearch = currentSearch.toLowerCase();
// } else {
// view = tv.view.getText();
// }
// if (searchDirection == SCH_BACKWARD) {
// sectionIndex = sectionArray.size();
// indexOfSearchForView = view.length();
// }
// if (searchDirection == SCH_FORWARD) {
// textIndex = view.indexOf(currentSearch, indexOfSearchForView);
// if (textIndex != -1) {
// tv.view.setTopIndex(textIndex);
// tv.view.setSelection(textIndex,
// textIndex + currentSearch.length());
// indexOfSearchForView = textIndex + 1;
// } else {
// if (searchWrap == SCH_WRAP_OFF) {
// if (sectionIndex < sectionArray.size()) {
// sectionIndex++;
// indexOfSearchForView = 0;
// man.resetSection(sectionArray.get(sectionIndex));
// putFoundInView();
// }
// } else {
// if (sectionIndex < sectionArray.size()) {
// sectionIndex++;
// indexOfSearchForView = 0;
// man.resetSection(sectionArray.get(sectionIndex));
// putFoundInView();
// } else {
// sectionIndex = 0;
// indexOfSearchForView = 0;
// man.resetSection(sectionArray.get(sectionIndex));
// putFoundInView();
// }
// }
// }
// } else {
//
// textIndex = view.lastIndexOf(currentSearch, indexOfSearchForView);
//
// if (textIndex != -1) {
// tv.view.setTopIndex(textIndex);
// tv.view.setSelection(textIndex,
// textIndex + currentSearch.length());
// indexOfSearchForView = textIndex - 1;
// } else {
// if (searchWrap == SCH_WRAP_OFF) {
// if (sectionIndex >= 0) {
// sectionIndex--;
// indexOfSearchForView = 0;
// man.resetSection(sectionArray.get(sectionIndex));
// putFoundInView();
// }
// } else {
// if (sectionIndex > 0) {
// sectionIndex--;
// indexOfSearchForView = 0;
// man.resetSection(sectionArray.get(sectionIndex));
// putFoundInView();
// } else {
// sectionIndex = 0;
// indexOfSearchForView = 0;
// man.resetSection(sectionArray.get(sectionIndex));
// putFoundInView();
// }
// }
// }
// }
// }
//
// public void getAllTextIndices() {
//
// for (int j = 0; j < sectionArray.size(); j++) {
// TextView tv = man.getText();
// String view;
// if (searchCaseSensitive == SCH_CASE_OFF) {
// view = tv.view.getText().toLowerCase();
// currentSearch = currentSearch.toLowerCase();
// } else {
// view = tv.view.getText();
// }
// textIndex = view.indexOf(currentSearch, indexOfSearchForView);
// while (textIndex != -1) {
// textIndexArray.add(textIndex);
// indexOfSearchForView = textIndex + 1;
// textIndex = view.indexOf(currentSearch, indexOfSearchForView);
// }
// man.resetSection(sectionArray.get(j));
// }
// }
