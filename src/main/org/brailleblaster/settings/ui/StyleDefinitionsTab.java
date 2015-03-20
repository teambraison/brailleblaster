package org.brailleblaster.settings.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.utd.IStyle;
import org.brailleblaster.utd.Style;
import org.brailleblaster.utd.StyleStack;
import org.brailleblaster.utd.UTDTranslationEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lblakey
 */
class StyleDefinitionsTab implements SettingsUITab {
	private static final Logger log = LoggerFactory.getLogger(StyleDefinitionsTab.class);
	private final LocaleHandler lh = new LocaleHandler();
	private final List<IStyle> newStyles = new ArrayList<>();
	private final List<StyleLevel> styleLevels = new ArrayList<>();
	private final Composite parent;
	private final ConfigPanel configPanel;
	private final Group groupSelect;
	private final Text fieldName;
	private final Spinner fieldLinesBefore, fieldLinesAfter, fieldLeftMargin, fieldRightMargin,
			fieldFirstLineIndent, fieldSkipPages, fieldOrphanControl, fieldLineSpacing;
	private final Combo fieldAlign, fieldFormat, fieldTranslation, fieldPageSide, fieldBrailleNumFormat;
	private final Button fieldSkipNumberLines, fieldNewPageBefore, fieldNewPageAfter, fieldDontSplit,
			fieldKeepWithNext, fieldKeepwithPrevious;
	private IStyle selectedStyle;

	StyleDefinitionsTab(ConfigPanel configPanel, TabFolder folder, Manager manager) {
		this.configPanel = configPanel;
		newStyles.addAll(manager.getDocument().getEngine().getStyleDefinitions().getStyles());

		TabItem item = new TabItem(folder, 0);
		item.setText(lh.localValue("styleDefsTab"));

		parent = new Composite(folder, 0);
		parent.setLayout(new GridLayout(1, true));
		item.setControl(parent);

		//Top part where user selects which style and/or style stack
		groupSelect = new Group(parent, 0);
		groupSelect.setText(lh.localValue("styleDefsTab.headerSelect"));
		groupSelect.setLayout(new GridLayout(1, true));
		SettingsUIUtils.setGridDataGroup(groupSelect);

		styleLevels.add(new StyleLevel(groupSelect, newStyles));

		//Fields for currently selected style
		Group groupStyle = new Group(parent, 0);
		groupStyle.setText(lh.localValue("styleDefsTab.headerFields"));
		groupStyle.setLayout(new GridLayout(2, true));
		SettingsUIUtils.setGridDataGroup(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Name");
		fieldName = new Text(groupStyle, 0);

		SettingsUIUtils.addLabel(groupStyle, "Lines Before");
		fieldLinesBefore = new Spinner(groupStyle, 0);

		SettingsUIUtils.addLabel(groupStyle, "Lines After");
		fieldLinesAfter = new Spinner(groupStyle, 0);

		SettingsUIUtils.addLabel(groupStyle, "Left Margin");
		fieldLeftMargin = new Spinner(groupStyle, 0);

		SettingsUIUtils.addLabel(groupStyle, "Right Margin");
		fieldRightMargin = new Spinner(groupStyle, 0);

		SettingsUIUtils.addLabel(groupStyle, "First Line Indent");
		fieldFirstLineIndent = new Spinner(groupStyle, 0);

		SettingsUIUtils.addLabel(groupStyle, "Skip Number Lines");
		fieldSkipNumberLines = makeEnabledCheckbox(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Skip Pages");
		fieldSkipPages = new Spinner(groupStyle, 0);

		SettingsUIUtils.addLabel(groupStyle, "Align");
		fieldAlign = makeCombo(groupStyle, IStyle.Align.values());

		SettingsUIUtils.addLabel(groupStyle, "Format");
		fieldFormat = makeCombo(groupStyle, IStyle.Format.values());

		SettingsUIUtils.addLabel(groupStyle, "Translation");
		fieldTranslation = makeCombo(groupStyle, IStyle.Translation.values());

		SettingsUIUtils.addLabel(groupStyle, "New Page Before");
		fieldNewPageBefore = makeEnabledCheckbox(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "New Page After");
		fieldNewPageAfter = makeEnabledCheckbox(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Page Side");
		fieldPageSide = makeCombo(groupStyle, IStyle.PageSide.values());

		SettingsUIUtils.addLabel(groupStyle, "Braille Page Number Format");
		fieldBrailleNumFormat = makeCombo(groupStyle, IStyle.BraillePageNumberFormat.values());

		SettingsUIUtils.addLabel(groupStyle, "Don't Split");
		fieldDontSplit = makeEnabledCheckbox(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Keep with Next");
		fieldKeepWithNext = makeEnabledCheckbox(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Keep with Previous");
		fieldKeepwithPrevious = makeEnabledCheckbox(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Orphan Control");
		fieldOrphanControl = new Spinner(groupStyle, 0);

		SettingsUIUtils.addLabel(groupStyle, "Line Spacing");
		fieldLineSpacing = new Spinner(groupStyle, 0);

		setStyleFieldsEnabled(false);
	}

	private void setStyleFieldsEnabled(boolean enabled) {
		fieldName.setEnabled(enabled);
		fieldLinesBefore.setEnabled(enabled);
		fieldLinesAfter.setEnabled(enabled);
		fieldLeftMargin.setEnabled(enabled);
		fieldRightMargin.setEnabled(enabled);
		fieldFirstLineIndent.setEnabled(enabled);
		fieldSkipNumberLines.setEnabled(enabled);
		fieldSkipPages.setEnabled(enabled);
		fieldAlign.setEnabled(enabled);
		fieldFormat.setEnabled(enabled);
		fieldTranslation.setEnabled(enabled);
		fieldNewPageBefore.setEnabled(enabled);
		fieldNewPageAfter.setEnabled(enabled);
		fieldPageSide.setEnabled(enabled);
		fieldBrailleNumFormat.setEnabled(enabled);
		fieldDontSplit.setEnabled(enabled);
		fieldKeepWithNext.setEnabled(enabled);
		fieldKeepwithPrevious.setEnabled(enabled);
		fieldOrphanControl.setEnabled(enabled);
		fieldLineSpacing.setEnabled(enabled);

		if (!enabled) {
			setStyleFieldEmpty(fieldName);
			setStyleFieldEmpty(fieldLinesBefore);
			setStyleFieldEmpty(fieldLinesAfter);
			setStyleFieldEmpty(fieldLeftMargin);
			setStyleFieldEmpty(fieldRightMargin);
			setStyleFieldEmpty(fieldFirstLineIndent);
			setStyleFieldEmpty(fieldSkipNumberLines);
			setStyleFieldEmpty(fieldSkipPages);
			setStyleFieldEmpty(fieldAlign);
			setStyleFieldEmpty(fieldFormat);
			setStyleFieldEmpty(fieldTranslation);
			setStyleFieldEmpty(fieldNewPageBefore);
			setStyleFieldEmpty(fieldNewPageAfter);
			setStyleFieldEmpty(fieldPageSide);
			setStyleFieldEmpty(fieldBrailleNumFormat);
			setStyleFieldEmpty(fieldDontSplit);
			setStyleFieldEmpty(fieldKeepWithNext);
			setStyleFieldEmpty(fieldKeepwithPrevious);
			setStyleFieldEmpty(fieldOrphanControl);
			setStyleFieldEmpty(fieldLineSpacing);
		}
	}

	private void setStyleFieldEmpty(Control control) {
		if (control instanceof Spinner)
			((Spinner) control).setSelection(-1);
		else if (control instanceof Combo)
			((Combo) control).select(0);
		else if (control instanceof Text)
			((Text) control).setText("");
		else if (control instanceof Button)
			((Button) control).setSelection(false);
		else
			throw new UnsupportedOperationException("Unknown control " + control.getClass());
	}

	private void onStyleSelect(IStyle curStyle, StyleLevel level) {
		this.selectedStyle = curStyle;
		//log.debug("style level {} total {}", styleLevels.indexOf(level), styleLevels.size() - 1);
		while (styleLevels.indexOf(level) < styleLevels.size() - 1) {
			//Eg level->level(changed)->level, need to remove last level
			//log.debug("while style level {} total {}", styleLevels.indexOf(level), styleLevels.size() - 1);
			StyleLevel curLevel = styleLevels.get(styleLevels.size() - 1);
			curLevel.dispose();
			styleLevels.remove(curLevel);
			parent.layout();
			configPanel.resize();
		}

		if (curStyle instanceof StyleStack) {
			styleLevels.add(new StyleLevel(groupSelect, (StyleStack) curStyle));
			setStyleFieldsEnabled(false);
			parent.layout();
			configPanel.resize();
		} else if (curStyle instanceof Style) {
			fieldName.setText(curStyle.getName());
			fieldLinesBefore.setSelection(curStyle.getLinesBefore());
			fieldLinesAfter.setSelection(curStyle.getLinesAfter());
			fieldLeftMargin.setSelection(defaultInt(curStyle.getLeftMargin()));
			fieldRightMargin.setSelection(defaultInt(curStyle.getRightMargin()));
			fieldFirstLineIndent.setSelection(defaultInt(curStyle.getFirstLineIndent()));
			fieldSkipNumberLines.setSelection(curStyle.isSkipNumberLines());
			fieldSkipPages.setSelection(curStyle.getSkipPages());
			fieldAlign.setText(curStyle.getAlign().name());
			fieldFormat.setText(curStyle.getFormat().name());
			fieldTranslation.setText(curStyle.getTranslation().name());
			fieldNewPageBefore.setSelection(curStyle.isNewPageBefore());
			fieldNewPageAfter.setSelection(curStyle.isNewPageAfter());
			fieldPageSide.setText(curStyle.getPageSide().name());
			fieldBrailleNumFormat.setText(curStyle.getBraillePageNumberFormat().name());
			fieldDontSplit.setSelection(curStyle.isDontSplit());
			fieldKeepWithNext.setSelection(curStyle.isKeepWithNext());
			fieldKeepwithPrevious.setSelection(curStyle.isKeepWithPrevious());
			fieldOrphanControl.setSelection(curStyle.getOrphanControl());
			fieldLineSpacing.setSelection(curStyle.getLineSpacing());
			setStyleFieldsEnabled(true);
		} else {
			throw new UnsupportedOperationException("Unknown style " + curStyle.getClass());
		}
	}

	@Override
	public String validate() {
		return null;
	}

	@Override
	public boolean updateEngine(UTDTranslationEngine engine) {
		return false;
	}

	private static Combo makeCombo(Composite parent, Enum<?>[] enumValues) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		for (Object curEnumValue : enumValues)
			combo.add(curEnumValue.toString());
		SettingsUIUtils.setGridData(combo);
		return combo;
	}

	private static Button makeEnabledCheckbox(Composite parent) {
		Button button = new Button(parent, SWT.CHECK);
		button.setText("Enabled");
		SettingsUIUtils.setGridData(button);
		return button;
	}
	
	private static int defaultInt(Integer someInteger) {
		return someInteger == null ? 0 : someInteger;
	}

	private class StyleLevel {
		private final Composite container;
		private final Label label;
		private final Combo combo;
		private final Map<String, IStyle> nameToStyle = new HashMap<>();

		public StyleLevel(Composite wrapperContainer, Collection<IStyle> styleList) {
			container = new Composite(wrapperContainer, 0);
			container.setLayout(new GridLayout(2, true));
			SettingsUIUtils.setGridData(container);

			label = new Label(container, 0);
			if (styleList instanceof StyleStack)
				label.setText("Select SubStyle");
			else
				label.setText("Select Style");
			SettingsUIUtils.setGridData(label);

			combo = new Combo(container, SWT.READ_ONLY);
			SettingsUIUtils.setGridData(combo);
			for (IStyle style : styleList) {
				nameToStyle.put(style.getName(), style);
				combo.add(style.getName());
			}

			combo.addSelectionListener(SettingsUIUtils.makeSelectedListener(
					(e) -> onStyleSelect(nameToStyle.get(combo.getText()), StyleLevel.this)
			));
		}

		public void dispose() {
			container.dispose();
		}
	}
}
