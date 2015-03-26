package org.brailleblaster.settings.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.utd.IStyle;
import org.brailleblaster.utd.Style;
import org.brailleblaster.utd.UTDTranslationEngine;
import org.brailleblaster.utd.config.StyleDefinitions;
import org.brailleblaster.utd.properties.PageNumberType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
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
	private final Style defaultStyle;
	private final LocaleHandler lh = new LocaleHandler();
	private final List<Style> newStyles = new ArrayList<>();
	private Style selectedStyle;
	private final Text fieldName;
	private final Spinner fieldLinesBefore, fieldLinesAfter, fieldLeftMargin, fieldRightMargin,
			fieldFirstLineIndent, fieldSkipPages, fieldOrphanControl, fieldLineSpacing;
	private final Combo styleCombo, fieldAlign, fieldFormat, fieldTranslation, fieldPageSide, fieldBrailleNumFormat;
	private final Combo fieldSkipNumberLines, fieldNewPageBefore, fieldNewPageAfter, fieldDontSplit,
			fieldKeepWithNext, fieldKeepwithPrevious;
	private StyleDefinitions styleDefs;

	StyleDefinitionsTab(TabFolder folder, Manager manager) {
		styleDefs = manager.getDocument().getEngine().getStyleDefinitions();
		defaultStyle = styleDefs.getDefaultStyle();
		for(Style curStyle : styleDefs.getStyles())
			//Create copy so we don't edit the live style
			newStyles.add(new Style(curStyle));

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
		
		SettingsUIUtils.addLabel(groupSelect, "");

		//Add sub box
		Composite groupAdd = new Composite(groupSelect, 0);
		groupAdd.setLayout(new GridLayout(3, true));
		SettingsUIUtils.setGridData(groupAdd);

		Button addButton = new Button(groupAdd, 0);
		addButton.setText("+Add Style");
		addButton.setEnabled(false);

		Text addText = new Text(groupAdd, SWT.BORDER);
		SettingsUIUtils.setGridData(addText);
		((GridData) addText.getLayoutData()).horizontalSpan = 2;

		//Fields for currently selected style
		Group groupStyle = new Group(parent, 0);
		groupStyle.setText(lh.localValue("styleDefsTab.headerFields"));
		groupStyle.setLayout(new GridLayout(2, true));
		SettingsUIUtils.setGridDataGroup(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Name");
		fieldName = new Text(groupStyle, SWT.BORDER);
		fieldName.setText("");
		SettingsUIUtils.setGridData(fieldName);

		SettingsUIUtils.addLabel(groupStyle, "Lines Before");
		fieldLinesBefore = makeSpinner(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Lines After");
		fieldLinesAfter = makeSpinner(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Left Margin");
		fieldLeftMargin = makeSpinner(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Right Margin");
		fieldRightMargin = makeSpinner(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "First Line Indent");
		fieldFirstLineIndent = makeSpinner(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Skip Number Lines");
		fieldSkipNumberLines = makeYesNoCombo(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Skip Pages");
		fieldSkipPages = makeSpinner(groupStyle);

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
		fieldOrphanControl = makeSpinner(groupStyle);

		SettingsUIUtils.addLabel(groupStyle, "Line Spacing");
		fieldLineSpacing = makeSpinner(groupStyle);

		//------Listeners-------
		styleCombo.addSelectionListener(SettingsUIUtils.makeSelectedListener((e) -> onStyleSelect()));
		addText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent ke) {
				addButton.setEnabled(!StringUtils.isBlank(addText.getText()));
			}
		});
		addButton.addSelectionListener(SettingsUIUtils.makeSelectedListener((e) -> {
			String newStyleName = addText.getText();
			log.debug("Adding style {}", newStyleName);
			Style style = new Style();
			style.setName(newStyleName);
			newStyles.add(style);
			resetStyleCombo(false);
			addText.setText("");
			addButton.setEnabled(false);
		}));
		
		 //------Set styler data-----------
		log.debug("Default style {}", defaultStyle);
		styleCombo.add(defaultStyle.getName());
		resetStyleCombo(true);

		setStyleFieldsEnabled(false);
	}
	
	public void resetStyleCombo(boolean defaultEmpty) {
		styleCombo.removeAll();
		for (IStyle style : newStyles) {
 			styleCombo.add(style.getName());
 		}
 
		setStyleFieldsEnabled(false);
		if (!defaultEmpty) {
			styleCombo.select(newStyles.size() - 1);
			onStyleSelect();
		}
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

	private void saveStyleFields() {
		updateStyle((s) -> s.getName(), selectedStyle::setName, fieldName, null);
		updateStyle((s) -> s.getLinesBefore(), selectedStyle::setLinesBefore, fieldLinesBefore, null);
		updateStyle((s) -> s.getLinesAfter(), selectedStyle::setLinesAfter, fieldLinesAfter, null);
		updateStyle((s) -> s.getLeftMargin(), selectedStyle::setLeftMargin, fieldLeftMargin, null);
		updateStyle((s) -> s.getRightMargin(), selectedStyle::setRightMargin, fieldRightMargin, null);
		updateStyle((s) -> s.getFirstLineIndent(), selectedStyle::setFirstLineIndent, fieldFirstLineIndent, null);
		updateStyle((s) -> s.isSkipNumberLines(), selectedStyle::setSkipNumberLines, fieldSkipNumberLines, null);
		updateStyle((s) -> s.getSkipPages(), selectedStyle::setSkipPages, fieldSkipPages, null);
		updateStyle((s) -> s.getAlign(), selectedStyle::setAlign, fieldAlign, IStyle.Align.class);
		updateStyle((s) -> s.getFormat(), selectedStyle::setFormat, fieldFormat, IStyle.Format.class);
		updateStyle((s) -> s.getTranslation(), selectedStyle::setTranslation, fieldTranslation, IStyle.Translation.class);
		updateStyle((s) -> s.isNewPageBefore(), selectedStyle::setNewPageBefore, fieldNewPageBefore, null);
		updateStyle((s) -> s.isNewPageAfter(), selectedStyle::setNewPageAfter, fieldNewPageAfter, null);
		updateStyle((s) -> s.getPageSide(), selectedStyle::setPageSide, fieldPageSide, IStyle.PageSide.class);
		updateStyle((s) -> s.getBraillePageNumberFormat(), selectedStyle::setBraillePageNumberFormat, fieldBrailleNumFormat, PageNumberType.class);
		updateStyle((s) -> s.isDontSplit(), selectedStyle::setDontSplit, fieldDontSplit, null);
		updateStyle((s) -> s.isKeepWithNext(), selectedStyle::setKeepWithNext, fieldKeepWithNext, null);
		updateStyle((s) -> s.isKeepWithPrevious(), selectedStyle::setKeepWithPrevious, fieldKeepwithPrevious, null);
		updateStyle((s) -> s.getOrphanControl(), selectedStyle::setOrphanControl, fieldOrphanControl, null);
		updateStyle((s) -> s.getLineSpacing(), selectedStyle::setLineSpacing, fieldLineSpacing, null);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public <V, E extends Enum<E>> void updateStyle(Function<IStyle, V> getter, Consumer<V> setter, Control control, Class<E> enumClazz) {
		Consumer setterRaw = setter;
		Object oldStyleValue = getter.apply(defaultStyle);
		if (control instanceof Spinner) {
			int value = ((Spinner) control).getSelection();
			if (value == -1) {
				if (oldStyleValue == null)
					//User didn't change value and java default is null
					return;
				value = (int) oldStyleValue;
			}
			setterRaw.accept(value);
		} else if (control instanceof Combo) {
			Combo combo = (Combo) control;
			if (isYesNoCombo(combo)) {
				boolean value;
				if (StringUtils.isBlank(combo.getText()))
					value = (boolean) oldStyleValue;
				else
					value = combo.getText().equals("Yes");
				setterRaw.accept(value);
			} else {
				Object enumValue;
				if (StringUtils.isBlank(combo.getText()))
					enumValue = oldStyleValue;
				else
					enumValue = Enum.valueOf(enumClazz, combo.getText());
				setterRaw.accept(enumValue);
			}
		} else if (control instanceof Text) {
			Text text = (Text) control;
			String value = text.getText();
			if (StringUtils.isBlank(value))
				value = (String) oldStyleValue;
			setterRaw.accept(value);
		} else
			throw new UnsupportedOperationException("Unknown control " + control.getClass());
	}

	private void onStyleSelect() {
		if (selectedStyle != null)
			saveStyleFields();

		selectedStyle = isSelectedStyleDefault() ? defaultStyle : newStyles.get(styleCombo.getSelectionIndex() - 1);
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
		V oldStyleValue = styleGetter.apply(defaultStyle);
		V newStyleValue = styleGetter.apply(selectedStyle);
		if (newStyleValue == null)
			setStyleFieldEmpty(control);
		else if (newStyleValue.equals(oldStyleValue))
			setStyleFieldEmpty(control);
		else {
			//Put a real value into the field
			if (control instanceof Spinner)
				((Spinner) control).setSelection((Integer) newStyleValue);
			else if (control instanceof Combo) {
				Combo combo = (Combo) control;
				if (isYesNoCombo(combo)) {
					if ((Boolean) newStyleValue)
						combo.setText("Yes");
					else
						combo.setText("No");
				} else {
					Enum newStyleValueEnum = (Enum) newStyleValue;
					((Combo) control).setText(newStyleValueEnum. name());
				}
			} else if (control instanceof Text)
				((Text) control).setText((String) newStyleValue);
			else
				throw new UnsupportedOperationException("Unknown control " + control.getClass());
		}
	}
	
	public boolean isSelectedStyleDefault() {
		return styleCombo.getText().equals(defaultStyle.getName());
 	}

	@Override
	public String validate() {
		return null;
	}

	@Override
	public boolean updateEngine(UTDTranslationEngine engine) {
		saveStyleFields();
		StyleDefinitions styleDefs = engine.getStyleDefinitions();
		ArrayList<Style> oldStyles = new ArrayList<>(styleDefs.getStyles());
		if (oldStyles.equals(newStyles)) {
			return false;
		}

		//Just remove all the old ones and re-add
		for (Style style : oldStyles)
			styleDefs.removeStyle(style);
		styleDefs.addStyles(newStyles);
		return true;
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

	private static boolean isYesNoCombo(Combo combo) {
		return Arrays.equals(combo.getItems(), new String[]{"", "Yes", "No"});
	}

	private static Spinner makeSpinner(Composite parent) {
		Spinner spinner = new Spinner(parent, SWT.BORDER);
		spinner.setMinimum(-1);
		spinner.setSelection(-1);
		return spinner;
	}

	private static int defaultInt(Integer someInteger) {
		return someInteger == null ? 0 : someInteger;
	}
}
