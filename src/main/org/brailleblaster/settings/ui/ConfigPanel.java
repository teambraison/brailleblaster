package org.brailleblaster.settings.ui;

import java.io.IOException;
import org.apache.commons.lang3.exception.ExceptionUtils;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.perspectives.braille.Manager;
import org.brailleblaster.settings.SettingsManager;
import org.brailleblaster.utd.UTDTranslationEngine;
import org.brailleblaster.utd.config.UTDConfig;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigPanel {
	private static final Logger log = LoggerFactory.getLogger(ConfigPanel.class);
	private final LocaleHandler lh = new LocaleHandler();
	private final UTDTranslationEngine engine;
	private final SettingsManager sm;
	private final Manager m;
	private final Shell shell;
	private final SettingsUITab pageProperties, pageNumTab, styleDefsTab;

//	TranslationSettingsTab translationSettings;
	public ConfigPanel(final SettingsManager sm, final Manager m) {
		this.engine = m.getDocument().getEngine();
		this.sm = sm;
		this.m = m;

		shell = new Shell(Display.getDefault(), SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.CLOSE | SWT.TITLE | SWT.MIN);
		shell.setText(lh.localValue("settings"));
		shell.setLayout(new FormLayout());

		TabFolder folder = new TabFolder(shell, SWT.NONE);
		setFormLayout(folder, 0, 100, 0, 94);

		//TODO: Port translationSettings once Michael says how it maps to UTD
		pageProperties = new PagePropertiesTab(folder, engine.getPageSettings());
//		translationSettings = new TranslationSettingsTab(folder, sm, settingsCopy);
		pageNumTab = new PageNumbersTab(folder, engine.getPageSettings());
		styleDefsTab = new StyleDefinitionsTab(folder, m);

		Button okButton = new Button(shell, SWT.PUSH);
		okButton.setText(lh.localValue(lh.localValue("buttonOk")));
		setFormLayout(okButton, 50, 75, 94, 100);
		okButton.addSelectionListener(SettingsUIUtils.makeSelectedListener((e) -> saveConfig()));

		Button cancelButton = new Button(shell, SWT.PUSH);
		cancelButton.setText("Cancel");
		setFormLayout(cancelButton, 75, 100, 94, 100);
		cancelButton.addSelectionListener(SettingsUIUtils.makeSelectedListener((e) -> close()));

		shell.addTraverseListener((e) -> {
			if (e.keyCode == SWT.ESC)
				shell.close();
		});

		shell.addListener(SWT.Close, (e) -> close());

		//Autosize shell based on what the internal elements require
		Point size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		shell.setSize(size);

		//Show the window
		shell.open();
	}

	private void saveConfig() {
		String errorStr = null;
		//This will validate each tab and fail fast
		if ((errorStr = pageProperties.validate()) != null
				//				&& (errorStr = translationSettings.validate()) != null
				&& (errorStr = pageNumTab.validate()) != null
				&& (errorStr = styleDefsTab.validate()) != null)
			new Notify(lh.localValue(errorStr));
		else {
			try {
				//Only save if setting was changed
				if (pageProperties.updateEngine(engine))
					UTDConfig.savePageSettings(sm.getUserPageSettingsFile(), engine.getPageSettings());
				if (pageNumTab.updateEngine(engine))
					UTDConfig.savePageSettings(sm.getUserPageSettingsFile(), engine.getPageSettings());
				if (styleDefsTab.updateEngine(engine))
					UTDConfig.saveStyleDefinitions(sm.getUserPageSettingsFile(), engine.getStyleDefinitions());
			} catch (IOException e) {
				log.debug("Encountered exception when saving UTD", e);
				new Notify("Cannot save UTD, see log " + ExceptionUtils.getMessage(e));
			}
		}

		close();
		m.refresh();
	}

	private void setFormLayout(Control c, int left, int right, int top, int bottom) {
		FormData location = new FormData();

		location.left = new FormAttachment(left);
		location.right = new FormAttachment(right);
		location.top = new FormAttachment(top);
		location.bottom = new FormAttachment(bottom);

		c.setLayoutData(location);
	}

	public Shell getShell() {
		return shell;
	}

	public void close() {
		shell.dispose();
	}
}
