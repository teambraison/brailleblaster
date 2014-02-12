package org.brailleblaster.search;

import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.perspectives.braille.mapping.TextMapElement;
import org.brailleblaster.perspectives.braille.messages.Message;
import org.brailleblaster.perspectives.braille.views.wp.BrailleView;
import org.brailleblaster.perspectives.braille.views.wp.TextView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;


public class SearchDialog extends Dialog {

	private final int MAX_SRCH_CHARS = 10000; 
	protected Object result;
	protected Shell shlFindreplace;
	private Manager man = null;
	Combo combo = null;
	private int lastCharIndex = 0;
	
	// private final FormToolkit // formToolkit = new FormToolkit(Display.getDefault());

	/**
	 * Create the dialog.
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
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlFindreplace.open();
		shlFindreplace.layout();
		Display display = getParent().getDisplay();
		while (!shlFindreplace.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlFindreplace = new Shell(getParent(), SWT.DIALOG_TRIM);
		shlFindreplace.setSize(262, 376);
		shlFindreplace.setText("Find/Replace");
		GridLayout gl_shlFindreplace = new GridLayout(5, false);
		gl_shlFindreplace.marginTop = 10;
		gl_shlFindreplace.marginLeft = 5;
		shlFindreplace.setLayout(gl_shlFindreplace);
		
		Label lblFind = new Label(shlFindreplace, SWT.NONE);
		lblFind.setText("Find:");
		Label label = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label, true, true);
		
		combo = new Combo(shlFindreplace, SWT.NONE);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		
		Label lblReplaceWith = new Label(shlFindreplace, SWT.NONE);
		lblReplaceWith.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblReplaceWith.setText("Replace with:");
		
		Combo combo_1 = new Combo(shlFindreplace, SWT.NONE);
		combo_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		
		Group grpDirection = new Group(shlFindreplace, SWT.NONE);
		grpDirection.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		// grpDirection.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		grpDirection.setText("Direction");
		// formToolkit.adapt(grpDirection);
		// formToolkit.paintBordersFor(grpDirection);
		
		Button btnRadioButton = new Button(grpDirection, SWT.RADIO);
		btnRadioButton.setSelection(true);
		btnRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnRadioButton.setBounds(10, 21, 90, 16);
		// formToolkit.adapt(btnRadioButton, true, true);
		btnRadioButton.setText("Forward");
		
		Button btnRadioButton_1 = new Button(grpDirection, SWT.RADIO);
		btnRadioButton_1.setBounds(10, 43, 90, 16);
		// formToolkit.adapt(btnRadioButton_1, true, true);
		btnRadioButton_1.setText("Backward");
		
		Group grpScope = new Group(shlFindreplace, SWT.NONE);
		grpScope.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		grpScope.setText("Scope");
		// formToolkit.adapt(grpScope);
		// formToolkit.paintBordersFor(grpScope);
		
		Button btnRadioButton_2 = new Button(grpScope, SWT.RADIO);
		btnRadioButton_2.setSelection(true);
		btnRadioButton_2.setBounds(10, 20, 90, 16);
		// formToolkit.adapt(btnRadioButton_2, true, true);
		btnRadioButton_2.setText("All");
		
		Button btnSelectedLines = new Button(grpScope, SWT.RADIO);
		btnSelectedLines.setBounds(10, 43, 90, 16);
		// formToolkit.adapt(btnSelectedLines, true, true);
		btnSelectedLines.setText("Selected Lines");
		
		Group grpOptions = new Group(shlFindreplace, SWT.NONE);
		grpOptions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 5, 1));
		grpOptions.setText("Options");
		// formToolkit.adapt(grpOptions);
		// formToolkit.paintBordersFor(grpOptions);
		
		Button btnCaseSensitive = new Button(grpOptions, SWT.CHECK);
		btnCaseSensitive.setBounds(10, 21, 91, 16);
		// formToolkit.adapt(btnCaseSensitive, true, true);
		btnCaseSensitive.setText("Case sensitive");
		
		Button btnWholeWord = new Button(grpOptions, SWT.CHECK);
		btnWholeWord.setBounds(10, 43, 91, 16);
		// formToolkit.adapt(btnWholeWord, true, true);
		btnWholeWord.setText("Whole word");
		
		Button btnRegularExpressions = new Button(grpOptions, SWT.CHECK);
		btnRegularExpressions.setBounds(10, 65, 124, 16);
		// formToolkit.adapt(btnRegularExpressions, true, true);
		btnRegularExpressions.setText("Regular expressions");
		
		Button btnWrapSearch = new Button(grpOptions, SWT.CHECK);
		btnWrapSearch.setBounds(107, 21, 91, 16);
		// formToolkit.adapt(btnWrapSearch, true, true);
		btnWrapSearch.setText("Wrap search");
		
		Button btnIncremental = new Button(grpOptions, SWT.CHECK);
		btnIncremental.setBounds(107, 43, 91, 16);
		// formToolkit.adapt(btnIncremental, true, true);
		btnIncremental.setText("Incremental");
		Label label_1 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_1, true, true);
		
		Button btnFind = new Button(shlFindreplace, SWT.NONE);
		btnFind.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		// formToolkit.adapt(btnFind, true, true);
		btnFind.setText("Find");
		btnFind.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// Grab text view. We're gonna search it!
				TextView tv = man.getText();
				
				// Are there any characters in the text view? If there 
				// are no characters, we probably don't have a document 
				// loaded yet.
				if(tv.view.getCharCount() == 0)
					return;
				
				// Grab search string.
				String findMeStr = combo.getText();
				findMeStr = "commons";
				
				// Grab text map element close to cursor.
				TextMapElement curElm = man.getClosest( tv.view.getCaretOffset() );
				
				// Convert cursor offset to offset in current text map element.
				lastCharIndex = curElm.end - (curElm.end - tv.view.getCaretOffset()) - 1;
				
				// Search all elements after the cursor for search string.
				for(int curElmIdx = man.indexOf(curElm); curElmIdx < man.getListSize(); ) {
					
					// Is the search string in this text map element?
					int searchStrIndex = curElm.value().toLowerCase().indexOf(findMeStr.toLowerCase(), lastCharIndex);
					
					// If we didn't find our search string in the current text map element, 
					// move onto the next TME.
					if(searchStrIndex != -1) {
				
						// We found the search string. Highlight in text view.
						searchStrIndex = tv.view.getText(curElm.start, curElm.end - 1).toLowerCase().indexOf(findMeStr.toLowerCase(), lastCharIndex);
						int documentStartIndex = curElm.start + searchStrIndex;
						int documentEndIndex = documentStartIndex + findMeStr.length();
						tv.setCursor( documentStartIndex, man );
						tv.view.setSelection( documentStartIndex, documentEndIndex );
						tv.view.setTopIndex( tv.view.getLineAtOffset(documentStartIndex) );
						lastCharIndex = searchStrIndex + findMeStr.length();
//						if(lastCharIndex >= tv.view.getText(curElm.start, curElm.end - 1).length()) {
//							lastCharIndex = 0;
//							curElmIdx++;
//						}
						break;
						
					} // 
					
					curElmIdx++;
					if( curElmIdx < man.getListSize() )
						curElm = man.getTextMapElement(curElmIdx);
					else
						curElm = null;
					lastCharIndex = 0;
					
				} // 
				
			} // widgetSelected()
			
		}); // btnFind.addSelectionListener()
		
		Button btnReplacefind = new Button(shlFindreplace, SWT.NONE);
		btnReplacefind.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		// formToolkit.adapt(btnReplacefind, true, true);
		btnReplacefind.setText("Replace/Find");
		Label label_2 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_2, true, true);
		
		Button btnReplace = new Button(shlFindreplace, SWT.NONE);
		btnReplace.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		// formToolkit.adapt(btnReplace, true, true);
		btnReplace.setText("Replace");
		
		Button btnReplaceAll = new Button(shlFindreplace, SWT.NONE);
		btnReplaceAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		// formToolkit.adapt(btnReplaceAll, true, true);
		btnReplaceAll.setText("Replace All");
		Label label_3 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_3, true, true);
		Label label_4 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_4, true, true);
		Label label_5 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_5, true, true);
		Label label_6 = new Label(shlFindreplace, SWT.NONE);
		// formToolkit.adapt(label_6, true, true);
		
		Button btnClose = new Button(shlFindreplace, SWT.NONE);
		btnClose.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		// formToolkit.adapt(btnClose, true, true);
		btnClose.setText("Close");
	}
}
