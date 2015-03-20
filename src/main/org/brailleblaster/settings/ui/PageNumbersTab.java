package org.brailleblaster.settings.ui;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.utd.PageSettings;
import org.brailleblaster.utd.UTDTranslationEngine;
import org.brailleblaster.utd.properties.PageNumberPosition;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

class PageNumbersTab implements SettingsUITab {
	private final LocaleHandler lh = new LocaleHandler();
	private final Combo interpointCombo, brailleNumCombo, printNumCombo, continueSymbolsCombo, continuePagesCombo;

	PageNumbersTab(TabFolder folder, PageSettings pageSettingsDefault) {
		TabItem item = new TabItem(folder, 0);
		item.setText(lh.localValue("pageNumbers"));

		//----Setup UI----
		Composite parent = new Composite(folder, 0);
		parent.setLayout(new GridLayout(1, true));
		item.setControl(parent);

		//Braille group
		Group brailleGroup = new Group(parent, 0);
		brailleGroup.setLayout(new GridLayout(2, true));
		brailleGroup.setText(lh.localValue("braille"));
		SettingsUIUtils.setGridDataGroup(brailleGroup);

		SettingsUIUtils.addLabel(brailleGroup, "Interpoint");
		interpointCombo = makeYesNoCombo(brailleGroup, pageSettingsDefault.getInterpoint());

		SettingsUIUtils.addLabel(brailleGroup, "Braille Page Number Location");
		brailleNumCombo = makeNumberPositionCombo(brailleGroup, pageSettingsDefault.getBraillePageNumberAt());

		//Print group
		Group printGroup = new Group(parent, 0);
		printGroup.setLayout(new GridLayout(2, true));
		printGroup.setText(lh.localValue("print"));
		SettingsUIUtils.setGridDataGroup(printGroup);

		SettingsUIUtils.addLabel(printGroup, "Print Page Number Location");
		printNumCombo = makeNumberPositionCombo(printGroup, pageSettingsDefault.getPrintPageNumberAt());

		SettingsUIUtils.addLabel(printGroup, "Continuation Symbols For Print Pages");
		continueSymbolsCombo = makeYesNoCombo(printGroup, pageSettingsDefault.isPrintPageNumberRange());

		//Continue pages group
		Group cpGroup = new Group(parent, 0);
		cpGroup.setLayout(new GridLayout(2, true));
		cpGroup.setText(lh.localValue("continue"));
		SettingsUIUtils.setGridDataGroup(cpGroup);

		SettingsUIUtils.addLabel(cpGroup, "Continue Pages");
		continuePagesCombo = makeYesNoCombo(cpGroup, pageSettingsDefault.isContinuePages());
		SettingsUIUtils.setGridData(continuePagesCombo);
	}

	@Override
	public String validate() {
		//No validation needed as there is only Combos with a fixed set of values
		return null;
	}

	@Override
	public boolean updateEngine(UTDTranslationEngine engine) {
		PageSettings pageSettings = engine.getPageSettings();
		boolean updated = false;

		updated = SettingsUIUtils.updateObject(pageSettings::getInterpoint, pageSettings::setInterpoint,
				interpointCombo.getText().equals("Yes"), updated);
		updated = SettingsUIUtils.updateObject(pageSettings::getBraillePageNumberAt, pageSettings::setBraillePageNumberAt,
				PageNumberPosition.valueOf(brailleNumCombo.getText()), updated);
		updated = SettingsUIUtils.updateObject(pageSettings::getPrintPageNumberAt, pageSettings::setPrintPageNumberAt,
				PageNumberPosition.valueOf(printNumCombo.getText()), updated);
		updated = SettingsUIUtils.updateObject(pageSettings::isPrintPageNumberRange, pageSettings::setPrintPageNumberRange,
				continueSymbolsCombo.getText().equals("Yes"), updated);
		updated = SettingsUIUtils.updateObject(pageSettings::isContinuePages, pageSettings::setContinuePages,
				continuePagesCombo.getText().equals("Yes"), updated);

		return updated;
	}

	private static Combo makeNumberPositionCombo(Composite parent, PageNumberPosition defaultValue) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);

		for (PageNumberPosition curLoc : PageNumberPosition.values())
			combo.add(curLoc.name());
		combo.setText(defaultValue.name());

		SettingsUIUtils.setGridData(combo);
		return combo;
	}

	private static Combo makeYesNoCombo(Composite parent, boolean defaultValue) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		combo.add("Yes");
		combo.add("No");

		if (defaultValue)
			combo.setText("Yes");
		else
			combo.setText("No");

		SettingsUIUtils.setGridData(combo);
		return combo;
	}
}
