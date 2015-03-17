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
import org.brailleblaster.utd.config.StyleDefinitions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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
	private static final Style STYLE_DEFAULT = new Style();
	private static final Map<String, Field> STYLE_FIELDS;

	static {
		Field[] fieldsDecl = Style.class.getDeclaredFields();
		Map<String, Field> styleField = new LinkedHashMap<String, Field>();
		for (Field curField : fieldsDecl) {
			curField.setAccessible(true);
			styleField.put(curField.getName(), curField);
		}
		styleField.remove("serialVersionUID");
		STYLE_FIELDS = Collections.unmodifiableMap(styleField);
	}

	private static final Logger log = LoggerFactory.getLogger(StyleDefinitionsTab.class);
	private final LocaleHandler lh = new LocaleHandler();
	private final Map<String, Control> styleFieldToControlMap = new HashMap<String, Control>();
	private final List<StyleLevel> styleLevels = new ArrayList<StyleLevel>();
	private final Composite parent;
	private final Group groupSelect;

	StyleDefinitionsTab(TabFolder folder, Manager manager) {
		StyleDefinitions styleDefs = manager.getDocument().getEngine().getStyleDefinitions();

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

		styleLevels.add(new StyleLevel(groupSelect, styleDefs.getStyles()));

		//Fields for currently selected style
		Group groupStyle = new Group(parent, 0);
		groupStyle.setText(lh.localValue("styleDefsTab.headerFields"));
		groupStyle.setLayout(new GridLayout(2, true));
		SettingsUIUtils.setGridDataGroup(groupStyle);

		//TODO: This temporarily uses the Java field name, in the future when the Style object API stabilizies give pretty names
		for (String curField : STYLE_FIELDS.keySet()) {
			Label fieldLabel = new Label(groupStyle, 0);
			fieldLabel.setText(curField);
			SettingsUIUtils.setGridData(fieldLabel);

			Class<?> fieldType = STYLE_FIELDS.get(curField).getType();
			Control fieldControl;
			if (fieldType == int.class || fieldType == Integer.class) {
				Spinner spinner = new Spinner(groupStyle, 0);
				spinner.setMinimum(0);
				fieldControl = spinner;
			} else if (fieldType.isEnum()) {
				Combo combo = new Combo(groupStyle, SWT.READ_ONLY);
				combo.add("");
				combo.select(0);
				for (Object curEnumValue : fieldType.getEnumConstants())
					combo.add(curEnumValue.toString());
				SettingsUIUtils.setGridData(combo);
				fieldControl = combo;
			} else if (fieldType == boolean.class) {
				Button button = new Button(groupStyle, SWT.CHECK);
				button.setText("Enabled");
				fieldControl = button;
			} else {
				Text fieldText = new Text(groupStyle, SWT.BORDER);
				SettingsUIUtils.setGridData(fieldText);
				fieldControl = fieldText;
			}
			styleFieldToControlMap.put(curField, fieldControl);
		}
		setStyleFieldsEnabled(false);
	}

	private void setStyleFieldsEnabled(boolean enabled) {
		for (Control control : styleFieldToControlMap.values()) {
			control.setEnabled(enabled);
			if (!enabled)
				setStyleFieldEmpty(control);
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

	private void setStyleFieldValue(String fieldName, String value) {
		Control control = styleFieldToControlMap.get(fieldName);
		if (value == null)
			setStyleFieldEmpty(control);
		else if (control instanceof Spinner)
			((Spinner) control).setSelection(Integer.parseInt(value));
		else if (control instanceof Combo)
			((Combo) control).setText(value);
		else if (control instanceof Text)
			((Text) control).setText(value);
		else if (control instanceof Button)
			((Button) control).setSelection(BooleanUtils.toBoolean(value));
		else
			throw new UnsupportedOperationException("Unknown control " + control.getClass());
	}

	private void onStyleSelect(IStyle curStyle, StyleLevel level) {
		//log.debug("style level {} total {}", styleLevels.indexOf(level), styleLevels.size() - 1);
		while (styleLevels.indexOf(level) < styleLevels.size() - 1) {
			//Eg level->level(changed)->level, need to remove last level
			//log.debug("while style level {} total {}", styleLevels.indexOf(level), styleLevels.size() - 1);
			StyleLevel curLevel = styleLevels.get(styleLevels.size() - 1);
			curLevel.dispose();
			styleLevels.remove(curLevel);
			parent.layout();
		}

		if (curStyle instanceof StyleStack) {
			styleLevels.add(new StyleLevel(groupSelect, (StyleStack) curStyle));
			setStyleFieldsEnabled(false);
			parent.layout();
		} else if (curStyle instanceof Style) {
			for (Map.Entry<String, Field> curStyleField : STYLE_FIELDS.entrySet()) {
				String styleFieldName = curStyleField.getKey();
				try {
					Object curStyleValue = curStyleField.getValue().get(curStyle);
					Object defaultValue = curStyleField.getValue().get(STYLE_DEFAULT);
					String valueString = (curStyleValue == null || curStyleValue.equals(defaultValue)) ? null : curStyleValue.toString();
					setStyleFieldValue(styleFieldName, valueString);
				} catch (Exception e) {
					throw new RuntimeException("Cannot get field " + styleFieldName, e);
				}
			}
			setStyleFieldsEnabled(true);
		} else {
			throw new UnsupportedOperationException("Unknown style " + curStyle.getClass());
		}
	}
	
	public String validate() {
		return null;
	}
	
	public boolean updateEngine(UTDTranslationEngine engine) {
		return false;
	}

	private class StyleLevel {
		private final Composite container;
		private final Label label;
		private final Combo combo;
		private final Map<String, IStyle> nameToStyle = new HashMap<String, IStyle>();

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

			combo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent se) {
					onStyleSelect(nameToStyle.get(combo.getText()), StyleLevel.this);
				}
			});
		}

		public void dispose() {
			container.dispose();
		}
	}
}
