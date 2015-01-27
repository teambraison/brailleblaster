package org.brailleblaster.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.views.wp.TextView;
import org.brailleblaster.wordprocessor.WPManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SearchDialog extends Dialog {
 
	private final int MAX_SRCH_CHARS = 10000;
	protected Object result;
	protected Shell shlFindreplace;
	private Shell errorMessageShell;
	Display display = null;
	private Manager man = null;
	Combo searchCombo = null;
	Combo replaceCombo = null;
	private int startCharIndex = 0;
	private int endCharIndex = 0;

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
	
	int numberOfLoops;
	int oldCursorPos;
	int oldTopIndex;
	private String [] searchList = new String [50];
	private String [] replaceList = new String [50];
	Map<String,String> searchSettings = new HashMap<String,String>();
	int searchArraySize; 
	Map<String,String>searchMap = new HashMap<String,String>();

	
	// private final FormToolkit // formToolkit = new
	// FormToolkit(Display.getDefault());

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public SearchDialog(Shell parent, int style, Manager brailleViewController) {
		super(parent, style);
		setText("SWT Dialog");
		man = brailleViewController;
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
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
					if (findStr() == true
							&& replaceCombo.getText().length() > 0)
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
		return result;
		
	}
	
	public Object openWithPreviousValues() {
		createPreviousContents();
		shlFindreplace.open();
		shlFindreplace.layout();

		display = getParent().getDisplay();
		display.addFilter(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(Event e) {

				// If user presses F3 key, do find/replace.
				if (e.keyCode == SWT.F3) {
					if (findStr() == true
							&& replaceCombo.getText().length() > 0)
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
		return result;
	}// open with previous values
	
	private void createPreviousContents() {

		//this method loads the contents of the search dialog in between searches but within the
		//same session
		shlFindreplace = new Shell(getParent(), SWT.DIALOG_TRIM);
		shlFindreplace.setSize(262,376);
		shlFindreplace.setLocation(600,250);// I did this so it wouldn't annoy me during testing--windows specific position
		shlFindreplace.setText("Find/Replace");
		shlFindreplace.setVisible(true);

		GridLayout gl_shlFindreplace = new GridLayout(5, false);
		gl_shlFindreplace.marginTop = 10;
		gl_shlFindreplace.marginLeft = 5;
		shlFindreplace.setLayout(gl_shlFindreplace);

		Label lblFind = new Label(shlFindreplace, SWT.NONE);
		lblFind.setText("Find:");
		Label label = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label, true, true);

		searchCombo = new Combo(shlFindreplace, SWT.NONE);
		searchCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 3, 1));
		// load the replaceList from the previous session
		if (searchList != null) {
			for (int i = 0; i < searchArraySize; i++) {

				searchCombo.add(searchList[i]);

			}// for
		}// if

		searchCombo.getData();
		searchCombo.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				
				String newText = searchCombo.getText();

				if (!searchMap.containsValue(String.valueOf(newText))) {
					searchCombo.add(newText);
					searchList[searchArraySize]=newText;
					searchMap.put(newText, newText);
					searchArraySize++;
				}

			}// key traversed
		});// addTraverseListener
		
		Label lblReplaceWith = new Label(shlFindreplace, SWT.NONE);
		lblReplaceWith.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));
		lblReplaceWith.setText("Replace with:");

		replaceCombo = new Combo(shlFindreplace, SWT.NONE);
		replaceCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 3, 1));
		replaceCombo.setEnabled(true);
		// load the replaceList from the previous session
		for (int i = 0; i < replaceList.length; i++) {
			if (replaceList[i]!=null) {
				replaceCombo.add(replaceList[i]);
			}// if
		}//for
		replaceCombo.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {

				String newText = replaceCombo.getText();
				replaceCombo.add(newText);
				replaceList = replaceCombo.getItems();
				Arrays.sort(replaceList);
				if(Arrays.binarySearch(replaceList, newText)> 0) {
					replaceCombo.remove(newText);
				}//if array already contains string	
			}// key traversed
		});// addTraverseListener

		Group grpDirection = new Group(shlFindreplace, SWT.NONE);
		grpDirection.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 3, 1));
		// grpDirection.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		grpDirection.setText("Direction");
		// formToolkit.adapt(grpDirection);
		// formToolkit.paintBordersFor(grpDirection);

		Button forwardRadioBtn = new Button(grpDirection, SWT.RADIO);
		//loads the value from the previous search
		if (searchSettings.containsValue("forward")) {
		forwardRadioBtn.setSelection(true);
		}//if
		forwardRadioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchDirection = SCH_FORWARD;
				searchSettings.put("searchDirectionString", "forward");
			}//event
		});//listener
		forwardRadioBtn.setBounds(10, 21, 90, 16);
		// formToolkit.adapt(btnRadioButton, true, true);
		forwardRadioBtn.setText("Forward");

		Button backwardRadioBtn = new Button(grpDirection, SWT.RADIO);
		// load the value from the previous search
		if (searchSettings.containsValue("backward")) {
		backwardRadioBtn.setSelection(true);
		}//if
		else {
			forwardRadioBtn.setSelection(true);
		}
		backwardRadioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchDirection = SCH_BACKWARD;
				searchSettings.put("searchDirectionString", "backward");
			}//event
		});//listener
		backwardRadioBtn.setBounds(10, 43, 90, 16);
		// formToolkit.adapt(btnRadioButton_1, true, true);
		backwardRadioBtn.setText("Backward");

		Group grpScope = new Group(shlFindreplace, SWT.NONE);
		grpScope.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false,
				2, 1));
		grpScope.setText("Scope");
		// formToolkit.adapt(grpScope);
		// formToolkit.paintBordersFor(grpScope);

		Button allRadioBtn = new Button(grpScope, SWT.RADIO);
		allRadioBtn.setSelection(true);
		allRadioBtn.setBounds(10, 20, 90, 16);
		// formToolkit.adapt(btnRadioButton_2, true, true);
		allRadioBtn.setText("All");
		allRadioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchScope = SCH_SCOPE_ALL;
			}
		});
		allRadioBtn.setEnabled(false);

		Button selectedLinesBtn = new Button(grpScope, SWT.RADIO);
		selectedLinesBtn.setBounds(10, 43, 90, 16);
		// formToolkit.adapt(btnSelectedLines, true, true);
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
				false, 5, 1));
		grpOptions.setText("Options");
		// formToolkit.adapt(grpOptions);
		// formToolkit.paintBordersFor(grpOptions);

		Button caseSensitiveCheck = new Button(grpOptions, SWT.CHECK);
		caseSensitiveCheck.setBounds(10, 21, 91, 16);
		// formToolkit.adapt(btnCaseSensitive, true, true);
		caseSensitiveCheck.setText("Case sensitive");
		
		//set the value from the previous search
		if (searchSettings.containsValue("caseSensitive")) {
		caseSensitiveCheck.setSelection(true);
		}//previous search
		
		caseSensitiveCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if (!searchSettings.containsValue("caseSensitive")) {
					searchSettings.put("caseSensitive", "caseSensitive");
				}
				else {
					searchSettings.put("caseSensitive", "notCaseSensitive");
				}
				// searchCaseSensitive
				if (searchCaseSensitive == SCH_CASE_OFF) {
					searchCaseSensitive = SCH_CASE_ON;
				}
				else {
					searchCaseSensitive = SCH_CASE_OFF;
				}
			}
		});

		Button wholeWordCheck = new Button(grpOptions, SWT.CHECK);
		wholeWordCheck.setBounds(10, 43, 91, 16);
		// formToolkit.adapt(btnWholeWord, true, true);
		wholeWordCheck.setText("Whole word");
		
		//set the value from the previous search		
		if(searchSettings.containsValue("wholeWord")) {
		wholeWordCheck.setSelection(true);
		}//previous search
		
		wholeWordCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if (!searchSettings.containsValue("wholeWord")) {
					searchSettings.put("wholeWord", "wholeWord");
				}
				else {
					searchSettings.put("wholeWord", "notWholeWord");
				}
				
				if (searchWholeWord == SCH_WHOLE_OFF)
					searchWholeWord = SCH_WHOLE_ON;
				else
					searchWholeWord = SCH_WHOLE_OFF;
			}
		});

		Button regExpressionsCheck = new Button(grpOptions, SWT.CHECK);
		regExpressionsCheck.setBounds(10, 65, 124, 16);
		// formToolkit.adapt(btnRegularExpressions, true, true);
		regExpressionsCheck.setText("Regular expressions");
		regExpressionsCheck.setEnabled(false);

		Button wrapSearchCheck = new Button(grpOptions, SWT.CHECK);
		wrapSearchCheck.setBounds(107, 21, 91, 16);
		// formToolkit.adapt(btnWrapSearch, true, true);
		wrapSearchCheck.setText("Wrap search");
		
		//set selection from previous search
		if(searchSettings.containsValue("wrapSearch")) {
		wrapSearchCheck.setSelection(true);
		}//if
		
		wrapSearchCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if (!searchSettings.containsValue("wrapSearch")) {
					searchSettings.put("wrapSearch", "wrapSearch");
				}
				else {
					searchSettings.put("wrapSearch", "notWrapSearch");
				}
				if (searchWrap == SCH_WRAP_OFF)
					searchWrap = SCH_WRAP_ON;
				else
					searchWrap = SCH_WRAP_OFF;
			}
		});

		Button incrementalCheck = new Button(grpOptions, SWT.CHECK);
		incrementalCheck.setBounds(107, 43, 91, 16);
		// formToolkit.adapt(btnIncremental, true, true);
		incrementalCheck.setText("Incremental");
		incrementalCheck.setEnabled(false);

		Label label_1 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_1, true, true);

		// /////////////////////////////////////////////////////////////////////////////////
		// Define our find button.
		Button findBtn = new Button(shlFindreplace, SWT.NONE);
		findBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				3, 1));
		// formToolkit.adapt(btnFind, true, true);
		findBtn.setText("Find");
		shlFindreplace.setDefaultButton(findBtn);
		findBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				numberOfLoops = 0;
				
				if (searchDirection==SCH_FORWARD) {
					if (searchWrap==SCH_WRAP_ON) {
						if (!findFwdWrap()) 
							createErrorMessage();
					}// if findFwdWrap
					else {
						if (!findFwdNoWrap()) 
							createErrorMessage();
					}// else findFwdNoWrap
				}// if searchForward
				else {
						if (searchWrap==SCH_WRAP_ON) {
							if (!findBackWrap()) 
								createErrorMessage();
						}// if findBwdWrap
						else {
							if(!findBackNoWrap()) 
								createErrorMessage();
						}// else findBwdNoWrap
				}// else searchBackward

			} // widgetSelected()

		}); // btnFind.addSelectionListener()
		


		Button replaceFindBtn = new Button(shlFindreplace, SWT.NONE);
		replaceFindBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		// formToolkit.adapt(btnReplacefind, true, true);
		replaceFindBtn.setText("Replace/Find");
		replaceFindBtn.setEnabled(false);

		Label label_2 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_2, true, true);

		Button replaceBtn = new Button(shlFindreplace, SWT.NONE);
		replaceBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 3, 1));
		// formToolkit.adapt(btnReplace, true, true);
		replaceBtn.setText("Replace");
		replaceBtn.setEnabled(true);
		replaceBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				numberOfLoops = 0;
				
				if (searchDirection==SCH_FORWARD) {
					if (searchWrap==SCH_WRAP_ON) {
						if (replaceFwdWrap()) {
							man.getText().copyAndPaste(replaceCombo.getText(), startCharIndex, endCharIndex);
						}
						else {
							createErrorMessage();
						}
					}// if replaceFwdWrap
					else {
						if (replaceFwdNoWrap()) {
							man.getText().copyAndPaste(replaceCombo.getText(), startCharIndex, endCharIndex);
						}
						else {
							createErrorMessage();
						}
					}// else replaceFwdNoWrap
				}// if searchForward
				else {
						if (searchWrap==SCH_WRAP_ON) {
							if (replaceBackWrap()) {
								man.getText().copyAndPaste(replaceCombo.getText(), startCharIndex, endCharIndex);
							}
							else {
								createErrorMessage();
							}
						}// if replaceBwdWrap
						else {
							if(replaceBackNoWrap()) {
								man.getText().copyAndPaste(replaceCombo.getText(), startCharIndex, endCharIndex);
							}
							else {
								createErrorMessage();
							}
						}// else replaceBwdNoWrap
				}// else searchBackward
			} // widgetSelected()

		}); // replaceBtn.addSelectionListener()

		Button replaceAllBtn = new Button(shlFindreplace, SWT.NONE);
		replaceAllBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		// formToolkit.adapt(btnReplaceAll, true, true);
		replaceAllBtn.setText("Replace All");
		replaceAllBtn.setEnabled(true);
		replaceAllBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				numberOfLoops = 0;
				
				if(searchDirection == SCH_FORWARD) {
				// Replace every instance of word.
				int oldTopIndex = man.getTextView().getTopIndex();
				int oldCursorPos = man.getText().getCursorOffset();
				man.getText().setCursor(0, man);
				if(findStr() == true) {
					do {
					man.getText().copyAndPaste(replaceCombo.getText(),
							startCharIndex, endCharIndex);
					man.getTextView().setTopIndex(oldTopIndex);
					man.getText().setCursorOffset(oldCursorPos);
					}// do
				while (findStr() == true);
				}//if findStr==true
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
					if (findStr() == true) {
						do {

						man.getText().copyAndPaste(replaceCombo.getText(),
								startCharIndex, endCharIndex);	
						man.getTextView().setTopIndex(oldTopIndex);
						man.getText().setCursorOffset(oldCursorPos);
						}// do
						while (findStr() == true);
					}// if findStr == true
					else {
						createErrorMessage();
					}// else if nothing found 
				}// if searchBackward
				
			} // widgetSelected()

		}); // replaceBtn.addSelectionListener()

		Label label_3 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_3, true, true);
		Label label_4 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_4, true, true);
		Label label_5 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_5, true, true);
		Label label_6 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_6, true, true);

		Button closeBtn = new Button(shlFindreplace, SWT.NONE);
		closeBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false,
				1, 1));
		// formToolkit.adapt(btnClose, true, true);
		closeBtn.setText("Close");
		closeBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlFindreplace.close();

			} // widgetSelected()

		}); // closeBtn.addSelectionListener...
	}//createPreviousContents

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {

		shlFindreplace = new Shell(getParent(), SWT.DIALOG_TRIM);
		shlFindreplace.setSize(262,376);
		shlFindreplace.setLocation(600,250);// I did this so it wouldn't annoy me during testing--windows specific position
		shlFindreplace.setText("Find/Replace");
		shlFindreplace.setVisible(true);

		GridLayout gl_shlFindreplace = new GridLayout(5, false);
		gl_shlFindreplace.marginTop = 10;
		gl_shlFindreplace.marginLeft = 5;
		shlFindreplace.setLayout(gl_shlFindreplace);

		Label lblFind = new Label(shlFindreplace, SWT.NONE);
		lblFind.setText("Find:");
		Label label = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label, true, true);

		searchCombo = new Combo(shlFindreplace, SWT.NONE);
		searchCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 3, 1));
		searchCombo.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				
				String newText = searchCombo.getText();

				if (!searchMap.containsValue(String.valueOf(newText))) {
					searchCombo.add(newText);
					searchList[searchArraySize]=newText;
					searchMap.put(newText, newText);
					searchArraySize++;
				}

			}// key traversed
		});// addTraverseListener
		
		Label lblReplaceWith = new Label(shlFindreplace, SWT.NONE);
		lblReplaceWith.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));
		lblReplaceWith.setText("Replace with:");

		replaceCombo = new Combo(shlFindreplace, SWT.NONE);
		replaceCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 3, 1));
		replaceCombo.setEnabled(true);
		replaceCombo.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {

				String newText = replaceCombo.getText();
				replaceCombo.add(newText);
				String [] replaceList = replaceCombo.getItems();
				Arrays.sort(replaceList);
				if(Arrays.binarySearch(replaceList, newText)> 0) {
					replaceCombo.remove(newText);
				}//if array already contains string	
			}// key traversed
		});// addTraverseListener

		Group grpDirection = new Group(shlFindreplace, SWT.NONE);
		grpDirection.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 3, 1));
		// grpDirection.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		grpDirection.setText("Direction");
		// formToolkit.adapt(grpDirection);
		// formToolkit.paintBordersFor(grpDirection);

		Button forwardRadioBtn = new Button(grpDirection, SWT.RADIO);
		forwardRadioBtn.setSelection(true);
		forwardRadioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchDirection = SCH_FORWARD;
				searchSettings.put("searchDirectionString", "forward");
			}
		});
		forwardRadioBtn.setBounds(10, 21, 90, 16);
		// formToolkit.adapt(btnRadioButton, true, true);
		forwardRadioBtn.setText("Forward");

		Button backwardRadioBtn = new Button(grpDirection, SWT.RADIO);
		backwardRadioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchDirection = SCH_BACKWARD;
				searchSettings.put("searchDirectionString", "backward");
			}
		});
		backwardRadioBtn.setBounds(10, 43, 90, 16);
		// formToolkit.adapt(btnRadioButton_1, true, true);
		backwardRadioBtn.setText("Backward");

		Group grpScope = new Group(shlFindreplace, SWT.NONE);
		grpScope.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false,
				2, 1));
		grpScope.setText("Scope");
		// formToolkit.adapt(grpScope);
		// formToolkit.paintBordersFor(grpScope);

		Button allRadioBtn = new Button(grpScope, SWT.RADIO);
		allRadioBtn.setSelection(true);
		allRadioBtn.setBounds(10, 20, 90, 16);
		// formToolkit.adapt(btnRadioButton_2, true, true);
		allRadioBtn.setText("All");
		allRadioBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchScope = SCH_SCOPE_ALL;
			}
		});
		allRadioBtn.setEnabled(false);

		Button selectedLinesBtn = new Button(grpScope, SWT.RADIO);
		selectedLinesBtn.setBounds(10, 43, 90, 16);
		// formToolkit.adapt(btnSelectedLines, true, true);
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
				false, 5, 1));
		grpOptions.setText("Options");
		// formToolkit.adapt(grpOptions);
		// formToolkit.paintBordersFor(grpOptions);

		Button caseSensitiveCheck = new Button(grpOptions, SWT.CHECK);
		caseSensitiveCheck.setBounds(10, 21, 91, 16);
		// formToolkit.adapt(btnCaseSensitive, true, true);
		caseSensitiveCheck.setText("Case sensitive");
		caseSensitiveCheck.setEnabled(true);
		caseSensitiveCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// searchCaseSensitive
				
				if (!searchSettings.containsValue("caseSensitive")) {
					searchSettings.put("caseSensitive", "caseSensitive");
				}
				else {
					searchSettings.put("caseSensitive", "notCaseSensitive");
				}

				if (searchCaseSensitive == SCH_CASE_OFF) {
					searchCaseSensitive = SCH_CASE_ON;
				}
				else
					searchCaseSensitive = SCH_CASE_OFF;			
			}
		});

		Button wholeWordCheck = new Button(grpOptions, SWT.CHECK);
		wholeWordCheck.setBounds(10, 43, 91, 16);
		// formToolkit.adapt(btnWholeWord, true, true);
		wholeWordCheck.setText("Whole word");
		wholeWordCheck.setEnabled(true);
		wholeWordCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if (!searchSettings.containsValue("wholeWord")) {
					searchSettings.put("wholeWord", "wholeWord");
				}
				else {
					searchSettings.put("wholeWord", "notWholeWord");
				}
				if (searchWholeWord == SCH_WHOLE_OFF)
					searchWholeWord = SCH_WHOLE_ON;
				else
					searchWholeWord = SCH_WHOLE_OFF;
			}
		});

		Button regExpressionsCheck = new Button(grpOptions, SWT.CHECK);
		regExpressionsCheck.setBounds(10, 65, 124, 16);
		// formToolkit.adapt(btnRegularExpressions, true, true);
		regExpressionsCheck.setText("Regular expressions");
		regExpressionsCheck.setEnabled(false);

		Button wrapSearchCheck = new Button(grpOptions, SWT.CHECK);
		wrapSearchCheck.setBounds(107, 21, 91, 16);
		// formToolkit.adapt(btnWrapSearch, true, true);
		wrapSearchCheck.setText("Wrap search");
		wrapSearchCheck.setEnabled(true);
		wrapSearchCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if (!searchSettings.containsValue("wrapSearch")) {
					searchSettings.put("wrapSearch", "wrapSearch");
				}
				else {
					searchSettings.put("wrapSearch", "notWrapSearch");
				}
				if (searchWrap == SCH_WRAP_OFF)
					searchWrap = SCH_WRAP_ON;
				else
					searchWrap = SCH_WRAP_OFF;
			}
		});

		Button incrementalCheck = new Button(grpOptions, SWT.CHECK);
		incrementalCheck.setBounds(107, 43, 91, 16);
		// formToolkit.adapt(btnIncremental, true, true);
		incrementalCheck.setText("Incremental");
		incrementalCheck.setEnabled(false);

		Label label_1 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_1, true, true);

		// /////////////////////////////////////////////////////////////////////////////////
		// Define our find button.
		Button findBtn = new Button(shlFindreplace, SWT.NONE);
		findBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				3, 1));
		// formToolkit.adapt(btnFind, true, true);
		findBtn.setText("Find");
		shlFindreplace.setDefaultButton(findBtn);
		findBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				numberOfLoops = 0;
				
				if (searchDirection==SCH_FORWARD) {
					if (searchWrap==SCH_WRAP_ON) {
						if (!findFwdWrap()) 
							createErrorMessage();
					}// if findFwdWrap
					else {
						if (!findFwdNoWrap()) 
							createErrorMessage();
					}// else findFwdNoWrap
				}// if searchForward
				else {
						if (searchWrap==SCH_WRAP_ON) {
							if (!findBackWrap()) 
								createErrorMessage();
						}// if findBwdWrap
						else {
							if(!findBackNoWrap()) 
								createErrorMessage();
						}// else findBwdNoWrap
				}// else searchBackward

			} // widgetSelected()

		}); // btnFind.addSelectionListener()
		


		Button replaceFindBtn = new Button(shlFindreplace, SWT.NONE);
		replaceFindBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		// formToolkit.adapt(btnReplacefind, true, true);
		replaceFindBtn.setText("Replace/Find");
		replaceFindBtn.setEnabled(false);

		Label label_2 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_2, true, true);

		Button replaceBtn = new Button(shlFindreplace, SWT.NONE);
		replaceBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 3, 1));
		// formToolkit.adapt(btnReplace, true, true);
		replaceBtn.setText("Replace");
		replaceBtn.setEnabled(true);
		replaceBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				numberOfLoops = 0;
				
				if (searchDirection==SCH_FORWARD) {
					if (searchWrap==SCH_WRAP_ON) {
						if (replaceFwdWrap()) {
							man.getText().copyAndPaste(replaceCombo.getText(), startCharIndex, endCharIndex);
						}
						else {
							createErrorMessage();
						}
					}// if replaceFwdWrap
					else {
						if (replaceFwdNoWrap()) {
							man.getText().copyAndPaste(replaceCombo.getText(), startCharIndex, endCharIndex);
						}
						else {
							createErrorMessage();
						}
					}// else replaceFwdNoWrap
				}// if searchForward
				else {
						if (searchWrap==SCH_WRAP_ON) {
							if (replaceBackWrap()) {
								man.getText().copyAndPaste(replaceCombo.getText(), startCharIndex, endCharIndex);
							}
							else {
								createErrorMessage();
							}
						}// if replaceBwdWrap
						else {
							if(replaceBackNoWrap()) {
								man.getText().copyAndPaste(replaceCombo.getText(), startCharIndex, endCharIndex);
							}
							else {
								createErrorMessage();
							}
						}// else replaceBwdNoWrap
				}// else searchBackward
			} // widgetSelected()

		}); // replaceBtn.addSelectionListener()

		Button replaceAllBtn = new Button(shlFindreplace, SWT.NONE);
		replaceAllBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		// formToolkit.adapt(btnReplaceAll, true, true);
		replaceAllBtn.setText("Replace All");
		replaceAllBtn.setEnabled(true);
		replaceAllBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				numberOfLoops = 0;
				
				if(searchDirection == SCH_FORWARD) {
				// Replace every instance of word.
				int oldTopIndex = man.getTextView().getTopIndex();
				int oldCursorPos = man.getText().getCursorOffset();
				man.getText().setCursor(0, man);
				if(findStr() == true) {
					do {
					man.getText().copyAndPaste(replaceCombo.getText(),
							startCharIndex, endCharIndex);
					man.getTextView().setTopIndex(oldTopIndex);
					man.getText().setCursorOffset(oldCursorPos);
					}// do
				while (findStr() == true);
				}//if findStr==true
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
					if (findStr() == true) {
						do {

						man.getText().copyAndPaste(replaceCombo.getText(),
								startCharIndex, endCharIndex);	
						man.getTextView().setTopIndex(oldTopIndex);
						man.getText().setCursorOffset(oldCursorPos);
						}// do
						while (findStr() == true);
					}// if findStr == true
					else {
						createErrorMessage();
					}// else if nothing found 
				}// if searchBackward
				
			} // widgetSelected()

		}); // replaceBtn.addSelectionListener()

		Label label_3 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_3, true, true);
		Label label_4 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_4, true, true);
		Label label_5 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_5, true, true);
		Label label_6 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_6, true, true);

		Button closeBtn = new Button(shlFindreplace, SWT.NONE);
		closeBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false,
				1, 1));
		// formToolkit.adapt(btnClose, true, true);
		closeBtn.setText("Close");
		closeBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlFindreplace.close();

			} // widgetSelected()

		}); // closeBtn.addSelectionListener...

	} // createContents()
	
	public void createErrorMessage() {

		display = getParent().getDisplay();
		display.beep();
		errorMessageShell = new Shell(display, SWT.DIALOG_TRIM);
		errorMessageShell.setLayout(new GridLayout(1,true));
		errorMessageShell.setText("Find/Replace Error");
		errorMessageShell.setSize(300,100);
		errorMessageShell.setLocation(500,250);

		
		Label label = new Label(errorMessageShell,SWT.RESIZE);
		label.setText("BrailleBlaster cannot find your word in the document");

				
		Button ok = new Button (errorMessageShell,SWT.NONE);
		ok.setText("OK");
		GridData errorMessageData = new GridData(SWT.HORIZONTAL);
		ok.setLayoutData(errorMessageData);
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				errorMessageShell.close();
				searchCombo.setFocus();
			}// widgetSelected

		});// selectionListener
		errorMessageShell.open();	

	}// createErrorMessage
	
	public String[] getSearchList() {
		return searchList;
	}// get searchList
	
	public String[] getReplaceList() {
		return replaceList;
	}// get replaceList

	// /////////////////////////////////////////////////////////////////
	// Searches document for string in our combo box.
	// Returns true if one was found.
	public boolean findStr() {
		// Grab text view.
		TextView tv = man.getText();

		// Are there any characters in the text view? If there
		// are no characters, we probably don't have a document
		// loaded yet.
		if (tv.view.getCharCount() == 0)
			return false;

		// Grab search string.
		String findMeStr = searchCombo.getText();

		// Get number of characters in text view.
		int numChars = tv.view.getText().length();

		// If the search string is larger than the total number of
		// characters in the view, don't bother.
		if (findMeStr.length() > numChars)
			return false;

		// ////////////////
		// Search forward.

		// Go forth!
		if (searchDirection == SCH_FORWARD) {	
			
			// Get current cursor position.
			startCharIndex = tv.view.getCaretOffset();
			endCharIndex = startCharIndex + findMeStr.length();
			
			// Check to see if we need a to reset it to zero first; it won' t go through the while loop
			// if it's at the end
			if (searchWrap == SCH_WRAP_ON) {
		
				// Make sure we aren't in an endless loop
				if (numberOfLoops >= 2) {
					return false;
				}// if numberOfLoops
									
				// If we're at the end, move to the other end.
				while (startCharIndex >= numChars || endCharIndex >= numChars) {

				// Reset position.
					startCharIndex = 0;
					endCharIndex = startCharIndex + findMeStr.length();
					tv.setCursor(startCharIndex, man);
					numberOfLoops++;

				} // if( startCharIndex...
				
				// Scour the view for the search string.
				while (startCharIndex < numChars && endCharIndex < (numChars+1)) {
					// Get current snippet of text we're testing.
					String curViewSnippet = tv.view.getText().substring(
							startCharIndex, endCharIndex);

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
								if (tv.view
										.getText()
										.substring(startCharIndex - 1,
												startCharIndex)
										.matches("^[\\pL\\pN]*$") == true)
									haveAmatch = false;
							if (endCharIndex + 1 < numChars)
								if (tv.view.getText()
										.substring(endCharIndex, endCharIndex + 1)
										.matches("^[\\pL\\pN]*$") == true)
									haveAmatch = false;

						} // if( searchWholeWord...

						// Update view if we have a match.
						if (haveAmatch == true) {
							// Set cursor and view to point to search string we
							// found.
							tv.view.setSelection(startCharIndex, endCharIndex);
							tv.view.setTopIndex(tv.view
									.getLineAtOffset(startCharIndex));

							// Found it; break.
							return true;

						} // if( haveAmatch = true)

					} // if( curViewSnippet...

					// Move forward a character.
					startCharIndex++;
					endCharIndex++;
					
					// Check the search wrap again.  If search wrap is on, move to other end of document, if at
					// the end.
					if (searchWrap == SCH_WRAP_ON) {
				
						// Make sure we aren't in an endless loop
						if (numberOfLoops >= 2) {
							return false;
						}// if numberOfLoops
											
						// If we're at the end, move to the other end.
						while (startCharIndex >= numChars || endCharIndex >= (numChars+1)) {

						// Reset position.
							startCharIndex = 0;
							endCharIndex = startCharIndex + findMeStr.length();
							tv.setCursor(startCharIndex, man);
							numberOfLoops++;

						} // if( startCharIndex...
						
					}// if searchWrap is on

				} // while( startCharIndex...
				
			} // if( searchWrap == SCH_WRAP_ON )
			
			else {
				
			// Else is for when the searchWrap is off

			// Scour the view for the search string.
				
			while (startCharIndex < numChars && endCharIndex < (numChars+1)) {
				// Get current snippet of text we're testing.
				String curViewSnippet = tv.view.getText().substring(
						startCharIndex, endCharIndex);

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
							if (tv.view
									.getText()
									.substring(startCharIndex - 1,
											startCharIndex)
									.matches("^[\\pL\\pN]*$") == true)
								haveAmatch = false;
						if (endCharIndex + 1 < numChars)
							if (tv.view.getText()
									.substring(endCharIndex, endCharIndex + 1)
									.matches("^[\\pL\\pN]*$") == true)
								haveAmatch = false;

					} // if( searchWholeWord...

					// Update view if we have a match.
					if (haveAmatch == true) {
						// Set cursor and view to point to search string we
						// found.
//						tv.setCursor(startCharIndex, man);
						tv.view.setSelection(startCharIndex, endCharIndex);
						tv.view.setTopIndex(tv.view
								.getLineAtOffset(startCharIndex));

						// Found it; break.
						return true;

					} // if( haveAmatch = true)

				} // if( curViewSnippet...

				// Move forward a character.
				startCharIndex++;
				endCharIndex++;

			  } // while( startCharIndex...
			
			}//else

		} // if(searchDirection == SCH_FORWARD)

		// Search forward.
		// ////////////////

		// /////////////////
		// Search backward.

		// Go backward!
		if (searchDirection == SCH_BACKWARD) {
			

			
			// Get current cursor position.
			endCharIndex = tv.view.getCaretOffset();
			startCharIndex = endCharIndex - findMeStr.length();
			

			// Check to see if we need to set it to zero first.  It can't search if it's at the end.
			if (searchWrap == SCH_WRAP_ON) {

				
				// Make sure we aren't in an endless loop
				if (numberOfLoops >= 2) {
					return false;
				}//if number of loops

				// If we're at the end, move to the other end.
				while (startCharIndex < 0 || endCharIndex < 0) {
					numberOfLoops++;
					// Reset position.
					endCharIndex = numChars;
					startCharIndex = endCharIndex - findMeStr.length();
					tv.setCursor((endCharIndex), man);

				} // while startCharIndex


			// Scour the view for the search string.
			while (startCharIndex >= 0 && (endCharIndex) > 0) {
				// Get current snippet of text we're testing.
				String curViewSnippet = tv.view.getText().substring(
						startCharIndex, endCharIndex);

				// Should we be checking case sensitive version?
				if (searchCaseSensitive == SCH_CASE_OFF) {
					curViewSnippet = curViewSnippet.toLowerCase();
					findMeStr = findMeStr.toLowerCase();
				}//if searchCaseSensitive

				// Compare the two strings. Is there a match?
				if (curViewSnippet.matches(findMeStr) == true) {
					// Whole word?
					boolean haveAmatch = true;
					if (searchWholeWord == SCH_WHOLE_ON) {
						// "^[\pL\pN]*$";
						if (startCharIndex - 1 >= 0)
							if (tv.view
									.getText()
									.substring(startCharIndex - 1,
											startCharIndex)
									.matches("^[\\pL\\pN]*$") == true)
								haveAmatch = false;
						if (endCharIndex + 1 < numChars)
							if (tv.view.getText()
									.substring(endCharIndex, endCharIndex + 1)
									.matches("^[\\pL\\pN]*$") == true)
								haveAmatch = false;

					} // if( searchWholeWord...

					// Update view if we have a match.
					if (haveAmatch == true) {
						// Set cursor and view to point to search string we
						// found.
						tv.view.setSelection(startCharIndex, endCharIndex);
						tv.view.setTopIndex(tv.view
								.getLineAtOffset(startCharIndex));

						// Found it; break.
						return true;

					} // if( haveAmatch = true)

				} // if( curViewSnippet...matches

				// Move back a character.
				startCharIndex--;
				endCharIndex--;

				// If search wrap is on, move to other end of document, if at
				// the end.
				if (searchWrap == SCH_WRAP_ON) {
					
					// Make sure we aren't in an endless loop
					if (numberOfLoops >= 2) {
						return false;
					}// if numberOfLoops

					// If we're at the end, move to the other end.
					while (startCharIndex < 0 || endCharIndex < 0) {
						numberOfLoops++;
						// Reset position.
						endCharIndex = numChars;
						startCharIndex = endCharIndex - findMeStr.length();
//						tv.setCursor((endCharIndex), man);

					} // if( startCharIndex...

				} // if( searchWrap == SCH_WRAP_ON )

			} // while( startCharIndex...
			
			}// if searchWrap 
			
			else {
				
				// Else is for when search wrap is off.  

				// Scour the view for the search string.
				while (startCharIndex >= 0  && endCharIndex > 0) {
					// Get current snippet of text we're testing.
					String curViewSnippet = tv.view.getText().substring(
							startCharIndex, endCharIndex);

					// Should we be checking case sensitive version?
					if (searchCaseSensitive == SCH_CASE_OFF) {
						curViewSnippet = curViewSnippet.toLowerCase();
						findMeStr = findMeStr.toLowerCase();
					}// if searchCaseSensitive

					// Compare the two strings. Is there a match?
					if (curViewSnippet.matches(findMeStr) == true) {
						// Whole word?
						boolean haveAmatch = true;
						if (searchWholeWord == SCH_WHOLE_ON) {
							// "^[\pL\pN]*$";
							if (startCharIndex - 1 >= 0)
								if (tv.view
										.getText()
										.substring(startCharIndex - 1,
												startCharIndex)
										.matches("^[\\pL\\pN]*$") == true)
									haveAmatch = false;
							if (endCharIndex + 1 < numChars)
								if (tv.view.getText()
										.substring(endCharIndex, endCharIndex + 1)
										.matches("^[\\pL\\pN]*$") == true)
									haveAmatch = false;

						} // if( searchWholeWord...

						// Update view if we have a match.
						if (haveAmatch == true) {
							// Set cursor and view to point to search string we
							// found.
							tv.setCursor(startCharIndex,man);
							tv.view.setSelection((startCharIndex), (endCharIndex));
							tv.view.setTopIndex(tv.view
									.getLineAtOffset(startCharIndex));

							// Found it; break.
							return true;

						} // if( haveAmatch = true)

					} // if( curViewSnippet...

					// Move back a character.
					startCharIndex--;
					endCharIndex--;	
					
			}// while 
				
			}// else no searchWrap

		} // if(searchDirection == SCH_BACKWARD)

		// Search backward.
		// /////////////////

		// If for some reason we get here, couldn't find a matching string.
		return false;
		
	} // findStr()
	
public boolean findFwdWrap() {
	// /////////////////////////////////////////////////////////////////
	// Searches document for string in our combo box.
	// Returns true if one was found.
		// Grab text view.
		TextView tv = man.getText();

		// Are there any characters in the text view? If there
		// are no characters, we probably don't have a document
		// loaded yet.
		if (tv.view.getCharCount() == 0)
			return false;

		// Grab search string.
		String findMeStr = searchCombo.getText();

		// Get number of characters in text view.
		int numChars = tv.view.getText().length();

		// If the search string is larger than the total number of
		// characters in the view, don't bother.
		if (findMeStr.length() > numChars)
			return false;

			startCharIndex = tv.view.getCaretOffset();
			endCharIndex = startCharIndex + findMeStr.length();
			
			// Make sure we aren't in an endless loop
			if (numberOfLoops <= 2) {

			// If search wrap is on, move to other end of document, if at
			// the end.
			if (searchWrap == SCH_WRAP_ON) {
				// If we're at the end, move to the other end.
				if (startCharIndex >= numChars || endCharIndex >= (numChars+1)) {
					// Reset position.
					startCharIndex = 0;
					endCharIndex = startCharIndex + findMeStr.length();
					tv.setCursor(startCharIndex, man);
					numberOfLoops++;

				} // if( startCharIndex...

			} // if( searchWrap == SCH_WRAP_ON )
			
			}// number of loops

			// Scour the view for the search string.
			while (startCharIndex < numChars && endCharIndex < (numChars+1)) {
				// Get current snippet of text we're testing.
				String curViewSnippet = tv.view.getText().substring(
						startCharIndex, endCharIndex);

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
							if (tv.view
									.getText()
									.substring(startCharIndex - 1,
											startCharIndex)
									.matches("^[\\pL\\pN]*$") == true)
								haveAmatch = false;
						if (endCharIndex + 1 < numChars)
							if (tv.view.getText()
									.substring(endCharIndex, endCharIndex + 1)
									.matches("^[\\pL\\pN]*$") == true)
								haveAmatch = false;

					} // if( searchWholeWord...

					// Update view if we have a match.
					if (haveAmatch == true) {
						// Set cursor and view to point to search string we
						// found.
						tv.view.setSelection(startCharIndex, endCharIndex);
						tv.view.setTopIndex(tv.view
								.getLineAtOffset(startCharIndex));

						// Found it; break.
						return true;

					} // if( haveAmatch = true)

				} // if( curViewSnippet...

				// Move forward a character.
				startCharIndex++;
				endCharIndex++;
				
				// Make sure we aren't in an endless loop
				if (numberOfLoops <= 2) {

				// If search wrap is on, move to other end of document, if at
				// the end.
				if (searchWrap == SCH_WRAP_ON) {
					// If we're at the end, move to the other end.
					if (startCharIndex >= numChars || endCharIndex > (numChars+1)) {
						// Reset position.
						startCharIndex = 0;
						endCharIndex = startCharIndex + findMeStr.length();
						tv.setCursor(startCharIndex, man);
						numberOfLoops++;

					} // if( startCharIndex...

				} // if( searchWrap == SCH_WRAP_ON )
				
				}// number of loops

			} // while( startCharIndex

		// If for some reason we get here, couldn't find a matching string.

			return false;


}// findFwdWrap

public boolean findFwdNoWrap() {
	// /////////////////////////////////////////////////////////////////
	// Searches document for string in our combo box.
	// Returns true if one was found.
		// Grab text view.
		TextView tv = man.getText();

		// Are there any characters in the text view? If there
		// are no characters, we probably don't have a document
		// loaded yet.
		if (tv.view.getCharCount() == 0)
			return false;

		// Grab search string.
		String findMeStr = searchCombo.getText();

		// Get number of characters in text view.
		int numChars = tv.view.getText().length();

		// If the search string is larger than the total number of
		// characters in the view, don't bother.
		if (findMeStr.length() > numChars)
			return false;

			// Get current cursor position.
			startCharIndex = tv.view.getCaretOffset();
			endCharIndex = startCharIndex + findMeStr.length();

			// Scour the view for the search string.
			while (startCharIndex < numChars && endCharIndex < (numChars+1)) {
				// Get current snippet of text we're testing.
				String curViewSnippet = tv.view.getText().substring(
						startCharIndex, endCharIndex);

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
							if (tv.view
									.getText()
									.substring(startCharIndex - 1,
											startCharIndex)
									.matches("^[\\pL\\pN]*$") == true)
								haveAmatch = false;
						if (endCharIndex + 1 < numChars)
							if (tv.view.getText()
									.substring(endCharIndex, endCharIndex + 1)
									.matches("^[\\pL\\pN]*$") == true)
								haveAmatch = false;

					} // if( searchWholeWord...

					// Update view if we have a match.
					if (haveAmatch == true) {
						// Set cursor and view to point to search string we
						// found.
						tv.view.setSelection(startCharIndex, endCharIndex);
						tv.view.setTopIndex(tv.view
								.getLineAtOffset(startCharIndex));

						// Found it; break.
						return true;

					} // if( haveAmatch = true)

				} // if( curViewSnippet...

				// Move forward a character.
				startCharIndex++;
				endCharIndex++;

			} // while( startCharIndex...

		// If for some reason we get here, couldn't find a matching string.
		return false;

}// findFwdNoWrap

public boolean findBackWrap() {
	// /////////////////////////////////////////////////////////////////
	// Searches document for string in our combo box.
	// Returns true if one was found.
		// Grab text view.
		TextView tv = man.getText();

		// Are there any characters in the text view? If there
		// are no characters, we probably don't have a document
		// loaded yet.
		if (tv.view.getCharCount() == 0)
			return false;

		// Grab search string.
		String findMeStr = searchCombo.getText();

		// Get number of characters in text view.
		int numChars = tv.view.getText().length();

		// If the search string is larger than the total number of
		// characters in the view, don't bother.
		if (findMeStr.length() > numChars)
			return false;

			// If there is selection text, that means we're still looking at
			// what we found earlier. Move past it.
			if (tv.view.getSelectionText().length() == findMeStr.length())
				tv.setCursor(startCharIndex, man);

			// Get current cursor position.
			endCharIndex = tv.view.getCaretOffset();
			startCharIndex = endCharIndex - findMeStr.length();
			
			// Make sure we aren't in an endless loop
			if (numberOfLoops <= 2) {
			

			// If search wrap is on, move to other end of document, if at
			// the end.
			if (searchWrap == SCH_WRAP_ON) {
				// If we're at the end, move to the other end.
				if (startCharIndex < 0 || endCharIndex < 0) {
					// Reset position.
					endCharIndex = numChars;
					startCharIndex = endCharIndex - findMeStr.length();
					tv.setCursor(endCharIndex, man);
					numberOfLoops++;

				} // if( startCharIndex...

			} // if( searchWrap == SCH_WRAP_ON
			
			} // numberOfLoops

			// Scour the view for the search string.
			while (startCharIndex >= 0 && endCharIndex > 0) {
				// Get current snippet of text we're testing.
				String curViewSnippet = tv.view.getText().substring(
						startCharIndex, endCharIndex);

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
							if (tv.view
									.getText()
									.substring(startCharIndex - 1,
											startCharIndex)
									.matches("^[\\pL\\pN]*$") == true)
								haveAmatch = false;
						if (endCharIndex + 1 < numChars)
							if (tv.view.getText()
									.substring(endCharIndex, endCharIndex + 1)
									.matches("^[\\pL\\pN]*$") == true)
								haveAmatch = false;

					} // if( searchWholeWord...

					// Update view if we have a match.
					if (haveAmatch == true) {
						// Set cursor and view to point to search string we
						// found.
						tv.view.setSelection(startCharIndex, endCharIndex);
						tv.view.setTopIndex(tv.view
								.getLineAtOffset(startCharIndex));

						// Found it; break.
						return true;

					} // if( haveAmatch = true)

				} // if( curViewSnippet...

				// Move back a character.
				startCharIndex--;
				endCharIndex--;
				
				// Make sure we aren't in an endless loop
				if (numberOfLoops <= 2) {

				// If search wrap is on, move to other end of document, if at
				// the end.
				if (searchWrap == SCH_WRAP_ON) {
					// If we're at the end, move to the other end.
					if (startCharIndex < 0 || endCharIndex < 0) {
						// Reset position.
						endCharIndex = numChars;
						startCharIndex = endCharIndex - findMeStr.length();
						tv.setCursor(endCharIndex, man);
						numberOfLoops++;

					} // if( startCharIndex...

				} // if( searchWrap == SCH_WRAP_ON )
				
				}// numberOfLoops

			} // while( startCharIndex...

		// If for some reason we get here, couldn't find a matching string.
		return false;

}// findBackWrap

public boolean findBackNoWrap() {
	// /////////////////////////////////////////////////////////////////
	// Searches document for string in our combo box.
	// Returns true if one was found.
		// Grab text view.
		TextView tv = man.getText();

		// Are there any characters in the text view? If there
		// are no characters, we probably don't have a document
		// loaded yet.
		if (tv.view.getCharCount() == 0)
			return false;

		// Grab search string.
		String findMeStr = searchCombo.getText();

		// Get number of characters in text view.
		int numChars = tv.view.getText().length();

		// If the search string is larger than the total number of
		// characters in the view, don't bother.
		if (findMeStr.length() > numChars)
			return false;

			// If there is selection text, that means we're still looking at
			// what we found earlier. Move past it.
			if (tv.view.getSelectionText().length() == findMeStr.length())
				tv.setCursor(startCharIndex, man);

			// Get current cursor position.
			endCharIndex = tv.view.getCaretOffset();
			startCharIndex = endCharIndex - findMeStr.length();

			// Scour the view for the search string.
			while (startCharIndex >= 0 && endCharIndex > 0) {
				// Get current snippet of text we're testing.
				String curViewSnippet = tv.view.getText().substring(
						startCharIndex, endCharIndex);

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
							if (tv.view
									.getText()
									.substring(startCharIndex - 1,
											startCharIndex)
									.matches("^[\\pL\\pN]*$") == true)
								haveAmatch = false;
						if (endCharIndex + 1 < numChars)
							if (tv.view.getText()
									.substring(endCharIndex, endCharIndex + 1)
									.matches("^[\\pL\\pN]*$") == true)
								haveAmatch = false;

					} // if( searchWholeWord...

					// Update view if we have a match.
					if (haveAmatch == true) {
						// Set cursor and view to point to search string we
						// found.
						tv.view.setSelection(startCharIndex, endCharIndex);
						tv.view.setTopIndex(tv.view
								.getLineAtOffset(startCharIndex));

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

public boolean replaceFwdWrap() {
	// Grab text view.
	TextView tv = man.getText();

	// Are there any characters in the text view? If there
	// are no characters, we probably don't have a document
	// loaded yet.
	if (tv.view.getCharCount() == 0)
		return false;

	// Grab search string.
	String findMeStr = searchCombo.getText();

	// Get number of characters in text view.
	int numChars = tv.view.getText().length();

	// If the search string is larger than the total number of
	// characters in the view, don't bother.
	if (findMeStr.length() > numChars)
		return false;

		// Get current cursor position.
		startCharIndex = tv.view.getCaretOffset();
		endCharIndex = startCharIndex + findMeStr.length();
	
			// Make sure we aren't in an endless loop
			if (numberOfLoops >= 2) {
				return false;
			}// if numberOfLoops
								
			// If we're at the end, move to the other end.
			while (startCharIndex >= numChars || endCharIndex >= numChars) {

			// Reset position.
				startCharIndex = 0;
				endCharIndex = startCharIndex + findMeStr.length();
				tv.setCursor(startCharIndex, man);
				numberOfLoops++;

			} // if( startCharIndex...
			
			// Scour the view for the search string.
			while (startCharIndex < numChars && endCharIndex < (numChars+1)) {
				// Get current snippet of text we're testing.
				String curViewSnippet = tv.view.getText().substring(
						startCharIndex, endCharIndex);

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
							if (tv.view
									.getText()
									.substring(startCharIndex - 1,
											startCharIndex)
									.matches("^[\\pL\\pN]*$") == true)
								haveAmatch = false;
						if (endCharIndex + 1 < numChars)
							if (tv.view.getText()
									.substring(endCharIndex, endCharIndex + 1)
									.matches("^[\\pL\\pN]*$") == true)
								haveAmatch = false;

					} // if( searchWholeWord...

					// Update view if we have a match.
					if (haveAmatch == true) {
						// Set cursor and view to point to search string we
						// found.
						tv.view.setSelection(startCharIndex, endCharIndex);
						tv.view.setTopIndex(tv.view
								.getLineAtOffset(startCharIndex));

						// Found it; break.
						return true;

					} // if( haveAmatch = true)

				} // if( curViewSnippet...

				// Move forward a character.
				startCharIndex++;
				endCharIndex++;
				
				// Check the search wrap again.  If search wrap is on, move to other end of document, if at
				// the end.
				if (searchWrap == SCH_WRAP_ON) {
			
					// Make sure we aren't in an endless loop
					if (numberOfLoops >= 2) {
						return false;
					}// if numberOfLoops
										
					// If we're at the end, move to the other end.
					while (startCharIndex >= numChars || endCharIndex >= (numChars+1)) {

					// Reset position.
						startCharIndex = 0;
						endCharIndex = startCharIndex + findMeStr.length();
						tv.setCursor(startCharIndex, man);
						numberOfLoops++;

					} // if( startCharIndex...
					
				}// if searchWrap is on

			} // while( startCharIndex...
			
	// If for some reason we get here, couldn't find a matching string.
	return false;
	
}// replaceFwdWrap

public boolean replaceFwdNoWrap() {
	// Grab text view.
	TextView tv = man.getText();

	// Are there any characters in the text view? If there
	// are no characters, we probably don't have a document
	// loaded yet.
	if (tv.view.getCharCount() == 0)
		return false;

	// Grab search string.
	String findMeStr = searchCombo.getText();

	// Get number of characters in text view.
	int numChars = tv.view.getText().length();

	// If the search string is larger than the total number of
	// characters in the view, don't bother.
	if (findMeStr.length() > numChars)
		return false;
		
		// Get current cursor position.
		startCharIndex = tv.view.getCaretOffset();
		endCharIndex = startCharIndex + findMeStr.length();
		// Scour the view for the search string.
			
		while (startCharIndex < numChars && endCharIndex < (numChars+1)) {
			// Get current snippet of text we're testing.
			String curViewSnippet = tv.view.getText().substring(
					startCharIndex, endCharIndex);

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
						if (tv.view
								.getText()
								.substring(startCharIndex - 1,
										startCharIndex)
								.matches("^[\\pL\\pN]*$") == true)
							haveAmatch = false;
					if (endCharIndex + 1 < numChars)
						if (tv.view.getText()
								.substring(endCharIndex, endCharIndex + 1)
								.matches("^[\\pL\\pN]*$") == true)
							haveAmatch = false;

				} // if( searchWholeWord...

				// Update view if we have a match.
				if (haveAmatch == true) {
					// Set cursor and view to point to search string we
					// found.
					tv.setCursor(startCharIndex, man);
					tv.view.setSelection(startCharIndex, endCharIndex);
					tv.view.setTopIndex(tv.view
							.getLineAtOffset(startCharIndex));

					// Found it; break.
					return true;

				} // if( haveAmatch = true)

			} // if( curViewSnippet...

			// Move forward a character.
			startCharIndex++;
			endCharIndex++;

		  } // while( startCharIndex...
		
	// If for some reason we get here, couldn't find a matching string.
	return false;
	
}// replaceFwdNoWrap

public boolean replaceBackWrap() {
	// Grab text view.
	TextView tv = man.getText();

	// Are there any characters in the text view? If there
	// are no characters, we probably don't have a document
	// loaded yet.
	if (tv.view.getCharCount() == 0)
		return false;

	// Grab search string.
	String findMeStr = searchCombo.getText();

	// Get number of characters in text view.
	int numChars = tv.view.getText().length();

	// If the search string is larger than the total number of
	// characters in the view, don't bother.
	if (findMeStr.length() > numChars)
		return false;

		// Get current cursor position.
		endCharIndex = tv.view.getCaretOffset();
		startCharIndex = endCharIndex - findMeStr.length();		

		// Check to see if we need to set it to zero first.  It can't search if it's at the end.
		if (searchWrap == SCH_WRAP_ON) {
			
			// Make sure we aren't in an endless loop
			if (numberOfLoops >= 2) {
				return false;
			}//if number of loops

			// If we're at the end, move to the other end.
			while (startCharIndex < 0 || endCharIndex < 0) {
				numberOfLoops++;
				// Reset position.
				endCharIndex = numChars;
				startCharIndex = endCharIndex - findMeStr.length();
				tv.setCursor((endCharIndex), man);

			} // while startCharIndex

		// Scour the view for the search string.
		while (startCharIndex >= 0 && (endCharIndex) > 0) {
			// Get current snippet of text we're testing.
			String curViewSnippet = tv.view.getText().substring(
					startCharIndex, endCharIndex);

			// Should we be checking case sensitive version?
			if (searchCaseSensitive == SCH_CASE_OFF) {
				curViewSnippet = curViewSnippet.toLowerCase();
				findMeStr = findMeStr.toLowerCase();
			}//if searchCaseSensitive

			// Compare the two strings. Is there a match?
			if (curViewSnippet.matches(findMeStr) == true) {
				// Whole word?
				boolean haveAmatch = true;
				if (searchWholeWord == SCH_WHOLE_ON) {
					// "^[\pL\pN]*$";
					if (startCharIndex - 1 >= 0)
						if (tv.view
								.getText()
								.substring(startCharIndex - 1,
										startCharIndex)
								.matches("^[\\pL\\pN]*$") == true)
							haveAmatch = false;
					if (endCharIndex + 1 < numChars)
						if (tv.view.getText()
								.substring(endCharIndex, endCharIndex + 1)
								.matches("^[\\pL\\pN]*$") == true)
							haveAmatch = false;

				} // if( searchWholeWord...

				// Update view if we have a match.
				if (haveAmatch == true) {
					// Set cursor and view to point to search string we
					// found.
					tv.view.setSelection(startCharIndex, endCharIndex);
					tv.view.setTopIndex(tv.view
							.getLineAtOffset(startCharIndex));

					// Found it; break.
					return true;

				} // if( haveAmatch = true)

			} // if( curViewSnippet...matches

			// Move back a character.
			startCharIndex--;
			endCharIndex--;

			// If search wrap is on, move to other end of document, if at
			// the end.
			if (searchWrap == SCH_WRAP_ON) {
				
				// Make sure we aren't in an endless loop
				if (numberOfLoops >= 2) {
					return false;
				}// if numberOfLoops

				// If we're at the end, move to the other end.
				while (startCharIndex < 0 || endCharIndex < 0) {
					numberOfLoops++;
					// Reset position.
					endCharIndex = numChars;
					startCharIndex = endCharIndex - findMeStr.length();
					tv.setCursor((endCharIndex), man);

				} // if( startCharIndex...

			} // if( searchWrap == SCH_WRAP_ON )

		} // while( startCharIndex...
		
		}// if searchWrap 

	// If for some reason we get here, couldn't find a matching string.
	return false;
	
}// replaceBackWrap

public boolean replaceBackNoWrap() {
	// Grab text view.
	TextView tv = man.getText();

	// Are there any characters in the text view? If there
	// are no characters, we probably don't have a document
	// loaded yet.
	if (tv.view.getCharCount() == 0)
		return false;

	// Grab search string.
	String findMeStr = searchCombo.getText();

	// Get number of characters in text view.
	int numChars = tv.view.getText().length();

	// If the search string is larger than the total number of
	// characters in the view, don't bother.
	if (findMeStr.length() > numChars)
		return false;
	
		// Get current cursor position.
		endCharIndex = tv.view.getCaretOffset();
		startCharIndex = endCharIndex - findMeStr.length();
			
			// Else is for when search wrap is off.  

			// Scour the view for the search string.
			while (startCharIndex >= 0  && endCharIndex > 0) {
				// Get current snippet of text we're testing.
				String curViewSnippet = tv.view.getText().substring(
						startCharIndex, endCharIndex);

				// Should we be checking case sensitive version?
				if (searchCaseSensitive == SCH_CASE_OFF) {
					curViewSnippet = curViewSnippet.toLowerCase();
					findMeStr = findMeStr.toLowerCase();
				}// if searchCaseSensitive

				// Compare the two strings. Is there a match?
				if (curViewSnippet.matches(findMeStr) == true) {
					// Whole word?
					boolean haveAmatch = true;
					if (searchWholeWord == SCH_WHOLE_ON) {
						// "^[\pL\pN]*$";
						if (startCharIndex - 1 >= 0)
							if (tv.view
									.getText()
									.substring(startCharIndex - 1,
											startCharIndex)
									.matches("^[\\pL\\pN]*$") == true)
								haveAmatch = false;
						if (endCharIndex + 1 < numChars)
							if (tv.view.getText()
									.substring(endCharIndex, endCharIndex + 1)
									.matches("^[\\pL\\pN]*$") == true)
								haveAmatch = false;

					} // if( searchWholeWord...

					// Update view if we have a match.
					if (haveAmatch == true) {
						// Set cursor and view to point to search string we
						// found.
						tv.setCursor(startCharIndex,man);
						tv.view.setSelection((startCharIndex), (endCharIndex));
						tv.view.setTopIndex(tv.view
								.getLineAtOffset(startCharIndex));

						// Found it; break.
						return true;

					} // if( haveAmatch = true)

				} // if( curViewSnippet...

				// Move back a character.
				startCharIndex--;
				endCharIndex--;	
				
		}// while 

	// If for some reason we get here, couldn't find a matching string.
	return false;
	
}// replaceBackNoWrap

} // class SearchDialog...




