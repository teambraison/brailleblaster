package org.brailleblaster.settings.ui;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.utd.PageSettings;
import org.brailleblaster.utd.PageSettings.NumberLocation;
import org.brailleblaster.utd.UTDTranslationEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

class PageNumbersTab implements SettingsUITab {
	private final LocaleHandler lh = new LocaleHandler();
	private final Combo continueCombo, interpointCombo, printCombo;

	PageNumbersTab(TabFolder folder, PageSettings pageSettingsDefault) {
		super();
		TabItem item = new TabItem(folder, 0);
		item.setText(lh.localValue("pageNumbers"));

		//----Setup UI----
		Composite parent = new Composite(folder, 0);
		parent.setLayout(new GridLayout(1, true));
		item.setControl(parent);

		//Braille interpoint and continuation symbols
		Group pageNumGroup = new Group(parent, 0);
		pageNumGroup.setLayout(new GridLayout(2, true));
		pageNumGroup.setText("Page Numbers");
		SettingsUIUtils.setGridDataGroup(pageNumGroup);

		addLabel(pageNumGroup, "Braille Interpoint Page Number Location");
		interpointCombo = makeNumberPositionCombo(pageNumGroup, pageSettingsDefault.getInterpoint());

		addLabel(pageNumGroup, "Continuation Symbols Print Page Number Location");
		printCombo = makeNumberPositionCombo(pageNumGroup, pageSettingsDefault.getPrintPages());

		//Continue pages
		Group cpGroup = new Group(parent, 0);
		cpGroup.setLayout(new GridLayout(2, true));
		cpGroup.setText(lh.localValue("continue"));
		SettingsUIUtils.setGridDataGroup(cpGroup);

		addLabel(cpGroup, "Continue Pages");
		continueCombo = new Combo(cpGroup, SWT.READ_ONLY);
		continueCombo.add("No");
		continueCombo.add("Yes");
		if (pageSettingsDefault.isContinuePages())
			continueCombo.setText("Yes");
		else
			continueCombo.setText("No");
		setGridData(continueCombo);
	}

	@Override
	public String validate() {
		return null;
	}

	@Override
	public boolean updateEngine(UTDTranslationEngine engine) {
		return false;
	}

	public void updateEngine(PageSettings pageSettingsNew) {
		NumberLocation interpoint = NumberLocation.valueOf(interpointCombo.getText());
		pageSettingsNew.setInterpoint(interpoint);

		NumberLocation print = NumberLocation.valueOf(printCombo.getText());
		pageSettingsNew.setPrintPages(print);

		boolean continueVal = continueCombo.getText().equals("Yes");
		pageSettingsNew.setContinuePages(continueVal);
	}

	private static Combo makeNumberPositionCombo(Composite parent, NumberLocation defaultValue) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);

		for (NumberLocation curLoc : NumberLocation.values())
			combo.add(curLoc.name());
		combo.setText(defaultValue.name());

		setGridData(combo);
		return combo;
	}

	private static Label addLabel(Composite parent, String text) {
		Label label = new Label(parent, 0);
		label.setText(text);
		setGridData(label);
		return label;
	}

	private static void setGridData(Control c) {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		c.setLayoutData(gridData);
	}
}
