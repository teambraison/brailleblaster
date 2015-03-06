/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.brailleblaster.settings.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.utd.IStyle;
import org.brailleblaster.utd.Style;
import org.brailleblaster.utd.StyleStack;
import org.brailleblaster.utd.config.StyleDefinitions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lblakey
 */
public class StyleDefinitionsTab {
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
	private final Map<String, Text> styleFieldToTextMap = new HashMap<String, Text>();
	private final List<StyleLevel> styleLevels = new ArrayList<StyleLevel>();
	private IStyle selectedStyle;
	private final Composite parent;
	private final Group groupSelect;

	StyleDefinitionsTab(TabFolder folder, Manager manager) {
		StyleDefinitions styleDefs = manager.getDocument().getEngine().getStyleDefinitions();
		
		TabItem item = new TabItem(folder, 0);
		item.setText(lh.localValue("styleDefsTab"));
		
		parent = new Composite(folder, SWT.BORDER);
		parent.setLayout(new GridLayout(1, true));
		item.setControl(parent);
		setGridData(parent);
		
		//Select group
		groupSelect = new Group(parent, 0);
		groupSelect.setText(lh.localValue("styleDefsTab.headerFields"));
		groupSelect.setLayout(new GridLayout(1, true));
		setGridData(groupSelect);
				
		styleLevels.add(new StyleLevel(groupSelect, styleDefs.getStyles()));
		
		//Style group
		Group groupStyle = new Group(parent, 0);
		groupStyle.setText(lh.localValue("styleDefsTab.headerProp"));
		groupStyle.setLayout(new GridLayout(2, true));
		setGridData(groupStyle);
		
		//TODO: This temporarily uses the Java field name, in the future when the Style object API stabilizies give pretty names
		for(String curField : STYLE_FIELDS.keySet()) {
			Label fieldLabel = new Label(groupStyle, 0);
			fieldLabel.setText(curField);
			setGridData(fieldLabel);
			Text fieldText = new Text(groupStyle, SWT.BORDER);
			setGridData(fieldText);
			styleFieldToTextMap.put(curField, fieldText);
		}
		toggleStyleFieldsEnabled(false);
		
		//Init with data
	}
	
	private void toggleStyleFieldsEnabled(boolean enabled) {
		for(Text curStyleText : styleFieldToTextMap.values()) {
			curStyleText.setEnabled(enabled);
			if(!enabled)
				curStyleText.setText("");
		}
	}
	
	private void onStyleSelect(IStyle curStyle, StyleLevel level) {
		log.debug("style level {} total {}", styleLevels.indexOf(level), styleLevels.size() - 1);
		while(styleLevels.indexOf(level) < styleLevels.size() - 1) {
			//Eg level->level(changed)->level
			log.debug("while style level {} total {}", styleLevels.indexOf(level), styleLevels.size() - 1);
			StyleLevel curLevel = styleLevels.get(styleLevels.size() - 1);
			curLevel.disposeAndPack();
			styleLevels.remove(curLevel);
			parent.layout();
		}
		
		if(curStyle instanceof StyleStack) {
			styleLevels.add(new StyleLevel(groupSelect, (StyleStack) curStyle));
			toggleStyleFieldsEnabled(false);
			parent.layout();
		} else if(curStyle instanceof Style) {
			selectedStyle = curStyle;
			
			for(Map.Entry<String, Field> curStyleField : STYLE_FIELDS.entrySet()) {
				String styleFieldName = curStyleField.getKey();
				try {
					Object curStyleValue = curStyleField.getValue().get(curStyle);
					Object defaultValue = curStyleField.getValue().get(STYLE_DEFAULT);
					String valueString = (curStyleValue == null || curStyleValue.equals(defaultValue)) ? "" : curStyleValue.toString();
					styleFieldToTextMap.get(styleFieldName).setText(valueString);
				} catch (Exception e) {
					throw new RuntimeException("Cannot get field " + styleFieldName, e);
				}
			}
			toggleStyleFieldsEnabled(true);
		} else {
			throw new UnsupportedOperationException("Unknown style " + curStyle.getClass());
		}
	}
	
	private class StyleLevel {
		public final Composite container;
		public final Label label;
		public final Combo combo;
		private final Map<String, IStyle> nameToStyle = new HashMap<String, IStyle>();
		
		public StyleLevel(Composite wrapperContainer, Collection<IStyle> styleList) {
			container = new Composite(wrapperContainer, 0);
			container.setLayout(new GridLayout(2, true));
			setGridData(container);
			
			label = new Label(container, 0);
			if(styleList instanceof StyleStack)
				label.setText("Select SubStyle");
			else
				label.setText("Select Style");
			setGridData(label);
			
			combo = new Combo(container, SWT.READ_ONLY);
			setGridData(combo);
			for(IStyle style : styleList) {
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
		
		public void disposeAndPack() {
			container.dispose();
			parent.layout();
		}
	}
	
	private static void setGridData(Control c){
		GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        c.setLayoutData(gridData);
	}
}
