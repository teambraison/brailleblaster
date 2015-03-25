package org.brailleblaster.settings.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.utd.IStyle;
import org.brailleblaster.utd.Style;
import org.brailleblaster.utd.StyleStack;
import org.brailleblaster.utd.UTDTranslationEngine;
import org.brailleblaster.utd.properties.PageNumberType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
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
	private static final Style DEFAULT_STYLE = new Style();
	private final LocaleHandler lh = new LocaleHandler();
	private final List<IStyle> newStyles = new ArrayList<>();
	private IStyle selectedStyle;
	private final Text fieldName;
	private final Spinner fieldLinesBefore, fieldLinesAfter, fieldLeftMargin, fieldRightMargin,
			fieldFirstLineIndent, fieldSkipPages, fieldOrphanControl, fieldLineSpacing;
	private final Combo styleCombo, fieldAlign, fieldFormat, fieldTranslation, fieldPageSide, fieldBrailleNumFormat;
	private final Combo fieldSkipNumberLines, fieldNewPageBefore, fieldNewPageAfter, fieldDontSplit,
			fieldKeepWithNext, fieldKeepwithPrevious;

	StyleDefinitionsTab(TabFolder folder, Manager manager) {
		newStyles.addAll(manager.getDocument().getEngine().getStyleDefinitions().getStyles());

		TabItem item = new TabItem(folder, 0);
		item.setText(lh.localValue("styleDefsTab"));

		Composite parent = new Composite(folder, 0);
		parent.setLayout(new GridLayout(1, true));
		item.setControl(parent);

		//Top part where user selects which style and/or style stack
		Group groupSelect = new Group(parent, 0);
		groupSelect.setText(lh.localValue("styleDefsTab.headerSelect"));
		groupSelect.setLayout(new GridLayout(2, true));
		SettingsUIUtils.setGridDataGroup(groupSelect);

		SettingsUIUtils.addLabel(groupSelect, "Select Style");
		styleCombo = new Combo(groupSelect, SWT.READ_ONLY);
		SettingsUIUtils.setGridData(styleCombo);

		//Fields for currently selected style
		Group groupStyle = new Group(parent, 0);
		groupStyle.setText(lh.localValue("styleDefsTab.headerFields"));
		groupStyle.setLayout(new GridLayout(2, true));
		SettingsUIUtils.setGridDataGroup(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Name");
		fieldName = new Text(groupStyle, 0);
		fieldName.setText("");
		SettingsUIUtils.setGridData(fieldName);

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
		fieldSkipNumberLines = makeYesNoCombo(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Skip Pages");
		fieldSkipPages = new Spinner(groupStyle, 0);

		SettingsUIUtils.addLabel(groupStyle, "Align");
		fieldAlign = makeCombo(groupStyle, IStyle.Align.values());

		SettingsUIUtils.addLabel(groupStyle, "Format");
		fieldFormat = makeCombo(groupStyle, IStyle.Format.values());

		SettingsUIUtils.addLabel(groupStyle, "Translation");
		fieldTranslation = makeCombo(groupStyle, IStyle.Translation.values());

		SettingsUIUtils.addLabel(groupStyle, "New Page Before");
		fieldNewPageBefore = makeYesNoCombo(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "New Page After");
		fieldNewPageAfter = makeYesNoCombo(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Page Side");
		fieldPageSide = makeCombo(groupStyle, IStyle.PageSide.values());

		SettingsUIUtils.addLabel(groupStyle, "Braille Page Number Format");
		fieldBrailleNumFormat = makeCombo(groupStyle, PageNumberType.values());

		SettingsUIUtils.addLabel(groupStyle, "Don't Split");
		fieldDontSplit = makeYesNoCombo(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Keep with Next");
		fieldKeepWithNext = makeYesNoCombo(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Keep with Previous");
		fieldKeepwithPrevious = makeYesNoCombo(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Orphan Control");
		fieldOrphanControl = new Spinner(groupStyle, 0);

		SettingsUIUtils.addLabel(groupStyle, "Line Spacing");
		fieldLineSpacing = new Spinner(groupStyle, 0);

		//------Listeners-------
		styleCombo.addSelectionListener(SettingsUIUtils.makeSelectedListener((e) -> onStyleSelect()));

		//------Set styler data-----------
		for (IStyle style : newStyles) {
			styleCombo.add(style.getName());
		}

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
		else
			throw new UnsupportedOperationException("Unknown control " + control.getClass());
	}

	private void onStyleSelect() {
		selectedStyle = newStyles.get(styleCombo.getSelectionIndex());
		if (!(selectedStyle instanceof Style))
			throw new UnsupportedOperationException("Unknown style " + selectedStyle.getClass());

		updateField((s) -> s.getName(), fieldName);
		updateField((s) -> s.getLinesBefore(), fieldLinesBefore);
		updateField((s) -> s.getLinesAfter(), fieldLinesAfter);
		updateField((s) -> s.getLeftMargin(), fieldLeftMargin);
		updateField((s) -> s.getRightMargin(), fieldRightMargin);
		updateField((s) -> s.getFirstLineIndent(), fieldFirstLineIndent);
		updateField((s) -> s.isSkipNumberLines(), fieldSkipNumberLines);
		updateField((s) -> s.getSkipPages(), fieldSkipPages);
		updateField((s) -> s.getAlign(), fieldAlign);
		updateField((s) -> s.getFormat(), fieldFormat);
		updateField((s) -> s.getTranslation(), fieldTranslation);
		updateField((s) -> s.isNewPageBefore(), fieldNewPageBefore);
		updateField((s) -> s.isNewPageAfter(), fieldNewPageAfter);
		updateField((s) -> s.getPageSide(), fieldPageSide);
		updateField((s) -> s.getBraillePageNumberFormat(), fieldBrailleNumFormat);
		updateField((s) -> s.isDontSplit(), fieldDontSplit);
		updateField((s) -> s.isKeepWithNext(), fieldKeepWithNext);
		updateField((s) -> s.isKeepWithPrevious(), fieldKeepwithPrevious);
		updateField((s) -> s.getOrphanControl(), fieldOrphanControl);
		updateField((s) -> s.getLineSpacing(), fieldLineSpacing);

		setStyleFieldsEnabled(true);
	}

	private <V> void updateField(Function<IStyle, V> styleGetter, Control control) {
		V oldStyleValue = styleGetter.apply(DEFAULT_STYLE);
		V newStyleValue = styleGetter.apply(selectedStyle);
		if (newStyleValue == null)
			setStyleFieldEmpty(control);
		else if (newStyleValue.equals(oldStyleValue))
			setStyleFieldEmpty(control);
		else {
			//Put a real value into the field
			if (control instanceof Spinner)
				((Spinner) control).setSelection((Integer) newStyleValue);
			else if (control instanceof Combo && newStyleValue instanceof Boolean) {
				Combo combo = (Combo) control;
				if ((Boolean) newStyleValue)
					combo.setText("Yes");
				else
					combo.setText("No");
			} else if (control instanceof Combo && newStyleValue instanceof Enum) {
				Enum newStyleValueEnum = (Enum) newStyleValue;
				((Combo) control).setText(newStyleValueEnum.name());
			} else if (control instanceof Text)
				((Text) control).setText((String) newStyleValue);
			else
				throw new UnsupportedOperationException("Unknown control " + control.getClass());
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
		combo.add("");
		for (Object curEnumValue : enumValues)
			combo.add(curEnumValue.toString());
		SettingsUIUtils.setGridData(combo);
		return combo;
	}

	private static Combo makeYesNoCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		combo.add("");
		combo.add("Yes");
		combo.add("No");

		SettingsUIUtils.setGridData(combo);
		return combo;
	}

	private static int defaultInt(Integer someInteger) {
		return someInteger == null ? 0 : someInteger;
	}
}
