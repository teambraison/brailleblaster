package org.brailleblaster.settings.ui;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.lang3.StringUtils;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.utd.BrailleSettings;
import org.brailleblaster.utd.UTDTranslationEngine;
import org.eclipse.swt.SWT;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TranslationSettingsTab implements SettingsUITab {
	private static final Logger log = LoggerFactory.getLogger(TranslationSettingsTab.class);
	private static final String transConPath = BBIni.getProgramDataPath("liblouisutdml", "lbu_files", "languageConfigurations.xml");
	private final XMLConfiguration config;
	private final LocaleHandler lh = new LocaleHandler();
	private final Composite parent;
	private final Combo languageCombo, litteraryCombo, computerCombo;

	TranslationSettingsTab(TabFolder folder, BrailleSettings brailleSettings) {
		TabItem tab = new TabItem(folder, 0);
		tab.setText(lh.localValue("translationSettings"));

		parent = new Composite(folder, 0);
		parent.setLayout(new FillLayout(SWT.VERTICAL));
		tab.setControl(parent);

		//---Add widgets---
		//Language
		Group langGroup = new Group(parent, 0);
		langGroup.setLayout(new GridLayout(2, true));
		langGroup.setText(lh.localValue("selectLanguage"));

		SettingsUIUtils.addLabel(langGroup, "Languages");
		languageCombo = new Combo(langGroup, SWT.READ_ONLY);
		SettingsUIUtils.setGridData(languageCombo);

		//Litterary Braille type
		Group typeGroup = new Group(parent, 0);
		typeGroup.setLayout(new GridLayout(2, true));
		typeGroup.setText(lh.localValue("selectType"));

		SettingsUIUtils.addLabel(typeGroup, lh.localValue("brailleType"));
		litteraryCombo = new Combo(typeGroup, SWT.READ_ONLY);
		SettingsUIUtils.setGridData(litteraryCombo);

		//Computer braille
		Group computerGroup = new Group(parent, 0);
		computerGroup.setText(lh.localValue("selectComputerBraille"));
		computerGroup.setLayout(new GridLayout(2, true));

		SettingsUIUtils.addLabel(computerGroup, lh.localValue("brailleType"));
		computerCombo = new Combo(computerGroup, SWT.READ_ONLY);
		SettingsUIUtils.setGridData(computerCombo);

		//--Add listeners--
		languageCombo.addSelectionListener(SettingsUIUtils.makeSelectedListener((e) -> onLanguageChange()));

		//---Set data---
		config = loadConfiguration();

		for (XMLEntry entry : config.entries)
			languageCombo.add(entry.getComboName());

		XMLEntry userEntry = config.findEntryByLocale(brailleSettings.getLocale());
		log.debug("Setting locale to " + userEntry.locale);
		languageCombo.setText(userEntry.getComboName());
		onLanguageChange();

		log.debug("user lit table {}", brailleSettings.getMainTranslationTable());
		log.debug("computer table {}", brailleSettings.getComputerBrailleTable());
		if (StringUtils.isNotBlank(brailleSettings.getMainTranslationTable())) {
			String userLitteraryTable = new File(brailleSettings.getMainTranslationTable()).getName();
			XMLTable table = userEntry.literaryBraille.stream()
					.filter((v) -> v.fileName.equals(userLitteraryTable))
					.findAny()
					.orElseThrow(() -> new RuntimeException("Config uses unknown litterary table" + userLitteraryTable));
			litteraryCombo.setText(table.name);
		}

		if (StringUtils.isNotBlank(brailleSettings.getComputerBrailleTable())) {
			String userComputerTable = new File(brailleSettings.getComputerBrailleTable()).getName();
			XMLTable table = userEntry.computerBraille.stream()
					.filter((v) -> v.fileName.equals(userComputerTable))
					.findAny()
					.orElseThrow(() -> new RuntimeException("Config uses unknown computer table" + userComputerTable));
			computerCombo.setText(table.name);
		}
	}

	public void onLanguageChange() {
		litteraryCombo.removeAll();
		computerCombo.removeAll();

		XMLEntry entry = config.entries.get(languageCombo.getSelectionIndex());
		for (XMLTable curTable : entry.literaryBraille)
			litteraryCombo.add(curTable.name);
		for (XMLTable curTable : entry.computerBraille)
			computerCombo.add(curTable.name);
	}

	@Override
	public String validate() {
		log.debug("validate");
		if (litteraryCombo.getSelectionIndex() == -1)
			return "Must select litterary braille type";
		if (computerCombo.getSelectionIndex() == -1)
			return "Must select computer braille type";
		log.debug("success");
		return null;
	}

	@Override
	public boolean updateEngine(UTDTranslationEngine engine) {
		BrailleSettings settings = engine.getBrailleSettings();
		boolean updated = false;
		XMLEntry selected = config.entries.get(languageCombo.getSelectionIndex());

		updated = SettingsUIUtils.updateObject(settings::getMainTranslationTable, settings::setMainTranslationTable,
				selected.literaryBraille.get(litteraryCombo.getSelectionIndex()).fileName, updated);
		updated = SettingsUIUtils.updateObject(settings::getComputerBrailleTable, settings::setComputerBrailleTable,
				selected.computerBraille.get(computerCombo.getSelectionIndex()).fileName, updated);
		updated = SettingsUIUtils.updateObject(settings::getMathTable, settings::setMathTable,
				selected.math.fileName, updated);

		return updated;
	}

	private static XMLConfiguration loadConfiguration() {
		XMLStreamReader inputXml = null;
		try (FileInputStream input = new FileInputStream(transConPath)) {
			JAXBContext jc = JAXBContext.newInstance(XMLConfiguration.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			XMLInputFactory xif = XMLInputFactory.newInstance();
			inputXml = xif.createXMLStreamReader(input);
			JAXBElement<XMLConfiguration> result = unmarshaller.unmarshal(inputXml, XMLConfiguration.class);
			return result.getValue();
		} catch (Exception e) {
			throw new RuntimeException("Cannot load settings from file " + transConPath, e);
		} finally {
			if (inputXml != null) {
				try {
					inputXml.close();
				} catch (XMLStreamException ex) {
					throw new RuntimeException("Could not close XML", ex);
				}
			}
		}
	}

	@XmlRootElement(name = "configurations")
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class XMLConfiguration {
		@XmlElement(name = "entry")
		public List<XMLEntry> entries;

		public XMLEntry findEntryByLocale(String locale) {
			return entries.stream()
					.filter((entry) -> entry.locale.equals(locale))
					.findFirst()
					.orElseThrow(() -> new RuntimeException("unknown locale '" + locale + "'"));
		}
	}

	private static class XMLEntry {
		public String locale;
		public String language;
		@XmlElementWrapper(name = "literaryBraille")
		@XmlElement(name = "table")
		public List<XMLTable> literaryBraille;
		@XmlElementWrapper(name = "computerBraille")
		@XmlElement(name = "table")
		public List<XMLTable> computerBraille;
		public XMLMath math;

		public String getComboName() {
			return language + " (" + locale + ")";
		}
	}

	private static class XMLTable {
		public String name;
		public String fileName;

		public XMLTable(String name, String fileName) {
			this.name = name;
			this.fileName = fileName;
		}

		public XMLTable() {
		}
	}

	private static class XMLMath {
		public String fileName;

		public XMLMath(String fileName) {
			this.fileName = fileName;
		}

		public XMLMath() {
		}

	}
}
