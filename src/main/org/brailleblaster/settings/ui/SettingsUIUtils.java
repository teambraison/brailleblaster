package org.brailleblaster.settings.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Useful utilities for creating the SWT UI
 *
 * @author lblakey
 */
final class SettingsUIUtils {
	private static final Logger log = LoggerFactory.getLogger(SettingsUIUtils.class);
	private SettingsUIUtils() {
	}

	public static void setGridData(Control c) {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		c.setLayoutData(gridData);
	}

	public static void setGridDataGroup(Group group) {
		setGridData(group);
		((GridData) group.getLayoutData()).grabExcessVerticalSpace = true;
	}
	
	/**
	 * If the value is different from the getter, update the object with the setter.
	 * @param <V>
	 * @param getter
	 * @param setter
	 * @param value
	 * @param updateFlag
	 * @return 
	 */
	public static <V> boolean updateObject(Supplier<V> getter, Consumer<V> setter, V value, boolean updateFlag) {
		if(!getter.get().equals(value)) {
			//log.debug("updateObject updated old {} new {} updated {}", getter.get(), value, updateFlag, new RuntimeException());
			setter.accept(value);
			return true;
		} else if (updateFlag) 
			//Value didn't need updating but still need to pass on flag
			return true;
		return false;
	}

	/**
	 * Generate a standard label
	 *
	 * @param parent
	 * @param text
	 * @return
	 */
	public static Label addLabel(Composite parent, String text) {
		Label label = new Label(parent, 0);
		label.setText(text);
		setGridData(label);
		return label;
	}

	public static SelectionListener makeSelectedListener(Consumer<SelectionEvent> function) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				function.accept(se);
			}
		};
	}
}
