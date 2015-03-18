package org.brailleblaster.settings.ui;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.utd.BrailleSettings;
import org.brailleblaster.utd.PageSettings;
import org.brailleblaster.utd.UTDTranslationEngine;
import org.brailleblaster.utd.properties.BrailleCellType;
import org.brailleblaster.utd.utils.UnitConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
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
class PagePropertiesTab implements SettingsUITab {
	private static final Logger log = LoggerFactory.getLogger(PagePropertiesTab.class);
	private static final DecimalFormat NUMBER_FORMATTER = new DecimalFormat("###.##");
	private final UnitConverter unitConverter = new UnitConverter();
	private final List<Page> standardPages;
	private final LocaleHandler lh = new LocaleHandler();
	private final Text widthBox, heightBox, linesBox, cellsBox, marginTopBox, marginLeftBox,
			marginRightBox, marginBottomBox;
	private final Label marginTopLabel, marginBottomLabel, marginLeftLabel, marginRightLabel;
	private final Combo pageTypes, cellType;
	private final Button regionalButton, cellsLinesButton;
	private final String unitName, unitSuffix;
	private boolean marginLocalUnit = true;
	/**
	 * Internal representation always in local units (inch or mm).
	 */
	private double pageHeight, pageWidth, pageCells, pageLines, marginTop, marginBottom, marginLeft, marginRight;
	private BrailleCellType brailleCell;

	PagePropertiesTab(TabFolder folder, UTDTranslationEngine engine) {
		//---Init---
		brailleCell = engine.getBrailleSettings().getCellType();
		boolean metric = unitConverter.isMetric();
		unitName = metric ? "mm" : "in";
		unitSuffix = " (" + unitName + ")";
		standardPages = Arrays.asList(
				new Page("Standard", 11.5, 11, metric),
				new Page("Letter", 8.5, 11, metric),
				new Page("Legal", 8.5, 14, metric),
				new Page("A3", 11.69, 16.54, metric),
				new Page("A4", 8.27, 11.69, metric),
				new Page("A5", 5.83, 8.27, metric)
		);

		//---Add widgets to tab---
		TabItem tab = new TabItem(folder, 0);
		tab.setText(lh.localValue("pageProperties"));

		Composite parent = new Composite(folder, 0);
		parent.setLayout(new GridLayout(1, true));
		tab.setControl(parent);

		//Group page size
		Group pageGroup = new Group(parent, 0);
		pageGroup.setText(lh.localValue("pageSize"));
		pageGroup.setLayout(new GridLayout(2, true));
		SettingsUIUtils.setGridDataGroup(pageGroup);

		SettingsUIUtils.addLabel(pageGroup, lh.localValue("pageSize"));
		pageTypes = new Combo(pageGroup, SWT.READ_ONLY);
		SettingsUIUtils.setGridData(pageTypes);

		SettingsUIUtils.addLabel(pageGroup, lh.localValue("width") + unitSuffix);
		widthBox = new Text(pageGroup, SWT.BORDER);
		addDoubleFilter(widthBox, false);
		SettingsUIUtils.setGridData(widthBox);

		SettingsUIUtils.addLabel(pageGroup, lh.localValue("height") + unitSuffix);
		heightBox = new Text(pageGroup, SWT.BORDER);
		addDoubleFilter(heightBox, false);
		SettingsUIUtils.setGridData(heightBox);
		
		SettingsUIUtils.addLabel(pageGroup, "Braille Cell Type");
		cellType = new Combo(pageGroup, SWT.READ_ONLY);
		SettingsUIUtils.setGridData(cellType);

		SettingsUIUtils.addLabel(pageGroup, lh.localValue("linesPerPage"));
		linesBox = new Text(pageGroup, SWT.BORDER);
		SettingsUIUtils.setGridData(linesBox);
		addDoubleFilter(linesBox, true);

		SettingsUIUtils.addLabel(pageGroup, lh.localValue("cellsPerLine"));
		cellsBox = new Text(pageGroup, SWT.BORDER);
		SettingsUIUtils.setGridData(cellsBox);
		addDoubleFilter(cellsBox, true);

		//Margin group
		Group marginGroup = new Group(parent, 0);
		marginGroup.setLayout(new GridLayout(2, true));
		marginGroup.setText(lh.localValue("margins"));
		SettingsUIUtils.setGridDataGroup(marginGroup);

		//Units subgroup
		SettingsUIUtils.addLabel(marginGroup, lh.localValue("measurementUnits"));
		Composite unitsGroup = new Composite(marginGroup, 0);
		unitsGroup.setLayout(new GridLayout(2, true));
		regionalButton = new Button(unitsGroup, SWT.RADIO);
		regionalButton.setText(unitName);
		regionalButton.setSelection(true);
		SettingsUIUtils.setGridData(regionalButton);
		cellsLinesButton = new Button(unitsGroup, SWT.RADIO);
		cellsLinesButton.setText(lh.localValue("cellsLines"));
		SettingsUIUtils.setGridData(cellsLinesButton);

		//All other margins
		marginTopLabel = SettingsUIUtils.addLabel(marginGroup, lh.localValue("topMargin") + unitSuffix);
		marginTopBox = new Text(marginGroup, SWT.BORDER);
		addDoubleFilter(marginTopBox, false);
		SettingsUIUtils.setGridData(marginTopBox);

		marginBottomLabel = SettingsUIUtils.addLabel(marginGroup, lh.localValue("bottomMargin") + unitSuffix);
		marginBottomBox = new Text(marginGroup, SWT.BORDER);
		addDoubleFilter(marginBottomBox, false);
		SettingsUIUtils.setGridData(marginBottomBox);

		marginLeftLabel = SettingsUIUtils.addLabel(marginGroup, lh.localValue("leftMargin") + unitSuffix);
		marginLeftBox = new Text(marginGroup, SWT.BORDER);
		addDoubleFilter(marginLeftBox, false);
		SettingsUIUtils.setGridData(marginLeftBox);

		marginRightLabel = SettingsUIUtils.addLabel(marginGroup, lh.localValue("rightMargin") + unitSuffix);
		marginRightBox = new Text(marginGroup, SWT.BORDER);
		addDoubleFilter(marginRightBox, false);
		SettingsUIUtils.setGridData(marginRightBox);

		//----Add listeners----
		//When the user selects a page from the drop down, fill out the width, height, cells, and lines boxes
		pageTypes.addSelectionListener(SettingsUIUtils.makeSelectedListener((e) -> onStandardPageSelected()));

		//Size fields
		//When a user types a digit, adjust cells, lines and page combo
		widthBox.addKeyListener(makeFieldListener(
				() -> widthBox.getText(), (v) -> pageWidth = v, (e) -> calculateCellsLinesAndUpdate()));
		heightBox.addKeyListener(makeFieldListener(
				() -> heightBox.getText(), (v) -> pageHeight = v, (e) -> calculateCellsLinesAndUpdate()));

		//Braille Cell Type 
		cellType.addSelectionListener(SettingsUIUtils.makeSelectedListener((e) -> onCellTypeChange()));
		
		//Cell fields
		cellsBox.addKeyListener(makeFieldListener(
				() -> cellsBox.getText(), (v) -> pageCells = v, (e) -> onCellLinesChange()));
		linesBox.addKeyListener(makeFieldListener(
				() -> linesBox.getText(), (v) -> pageLines = v, (e) -> onCellLinesChange()));

		//Update cells/line and lines/page when margins change
		DoubleUnaryOperator marginHeight
				= (v) -> marginLocalUnit ? v : unitConverter.calculateHeightFromLines((int) v, brailleCell);
		DoubleUnaryOperator marginWidth
				= (v) -> marginLocalUnit ? v : unitConverter.calculateWidthFromCells((int) v, brailleCell);
		marginTopBox.addKeyListener(makeFieldListener(
				() -> marginTopBox.getText(), marginHeight.andThen((v) -> marginTop = v), (e) -> calculateCellsLinesAndUpdate()));
		marginBottomBox.addKeyListener(makeFieldListener(
				() -> marginBottomBox.getText(), marginHeight.andThen((v) -> marginBottom = v), (e) -> calculateCellsLinesAndUpdate()));
		marginLeftBox.addKeyListener(makeFieldListener(
				() -> marginLeftBox.getText(), marginWidth.andThen((v) -> marginLeft = v), (e) -> calculateCellsLinesAndUpdate()));
		marginRightBox.addKeyListener(makeFieldListener(
				() -> marginRightBox.getText(), marginWidth.andThen((v) -> marginRight = v), (e) -> calculateCellsLinesAndUpdate()));

		//Margin unit suffixes
		SelectionListener marginUnitChangedListener = SettingsUIUtils.makeSelectedListener((e) -> onMarginUnitSelected());
		regionalButton.addSelectionListener(marginUnitChangedListener);
		cellsLinesButton.addSelectionListener(marginUnitChangedListener);

		//---Set field default values---
		//Pages drop down box
		for (Page curPage : standardPages)
			pageTypes.add(curPage.toString());
		
		for (BrailleCellType curType : BrailleCellType.values())
			cellType.add(curType.name());
		brailleCell = engine.getBrailleSettings().getCellType();
		cellType.setText(brailleCell.name());

		//Page size
		PageSettings pageSettings = engine.getPageSettings();
		if (pageSettings.getPaperHeight() != 0 && pageSettings.getPaperWidth() != 0) {
			//User has set their own settings stored in their own units
			pageHeight = unitConverter.mmToLocalUnits(pageSettings.getPaperHeight());
			pageWidth = unitConverter.mmToLocalUnits(pageSettings.getPaperWidth());

		} else {
			//TODO: assume the first one? 
			//Page handles unit conversions
			Page defaultPage = standardPages.get(0);
			pageHeight = defaultPage.height;
			pageWidth = defaultPage.width;
		}

		marginTop = unitConverter.mmToLocalUnits(pageSettings.getTopMargin());
		marginBottom = unitConverter.mmToLocalUnits(pageSettings.getBottomMargin());
		marginLeft = unitConverter.mmToLocalUnits(pageSettings.getLeftMargin());
		marginRight = unitConverter.mmToLocalUnits(pageSettings.getRightMargin());

		calculateCellsLinesAndUpdate();
	}

	/**
	 * Update UI with internally stored values
	 */
	private void updateFields() {
		Optional<Page> matchedPage = standardPages.stream()
				.filter((p) -> p.height == pageHeight && p.width == pageWidth)
				.findFirst();
		if (matchedPage.isPresent())
			pageTypes.select(pageTypes.indexOf(matchedPage.get().toString()));
		else if (pageTypes.getItemCount() == standardPages.size()) {
			pageTypes.add(lh.localValue("custom"));
			pageTypes.select(pageTypes.getItemCount() - 1);
		} else
			pageTypes.select(pageTypes.getItemCount() - 1);

		setTextIfDifferent(widthBox, pageWidth);
		setTextIfDifferent(heightBox, pageHeight);

		setTextIfDifferent(linesBox, pageLines);
		setTextIfDifferent(cellsBox, pageCells);

		setTextIfDifferent(marginTopBox, marginLocalUnit ? marginTop : unitConverter.calculateLinesFromHeight(marginTop, brailleCell));
		setTextIfDifferent(marginBottomBox, marginLocalUnit ? marginBottom : unitConverter.calculateLinesFromHeight(marginBottom, brailleCell));
		setTextIfDifferent(marginLeftBox, marginLocalUnit ? marginLeft : unitConverter.calculateCellsFromWidth(marginLeft, brailleCell));
		setTextIfDifferent(marginRightBox, marginLocalUnit ? marginRight : unitConverter.calculateCellsFromWidth(marginRight, brailleCell));
	}

	/**
	 * Update UI field if needed
	 *
	 * @param box
	 * @param number
	 */
	private void setTextIfDifferent(Text box, double number) {
		String given = box.getText();
		String expected = NUMBER_FORMATTER.format(number);
		//Reset if number is different
		if (!(!given.isEmpty() && Double.parseDouble(given) == number)) {
			box.setText(expected);
		}
	}

	/**
	 * Recalculate page cells and page lines
	 */
	private void calculateCellsLinesAndUpdate() {
		pageCells = unitConverter.calculateCellsFromWidth(pageWidth - marginLeft - marginRight, brailleCell);
		pageLines = unitConverter.calculateLinesFromHeight(pageHeight - marginTop - marginBottom, brailleCell);
		updateFields();
	}

	/**
	 * When the user selects a standard page size, update width, height, cells, lines fields
	 */
	private void onStandardPageSelected() {
		Page p = standardPages.get(pageTypes.getSelectionIndex());
		pageHeight = p.height;
		pageWidth = p.width;
		calculateCellsLinesAndUpdate();

		if (pageTypes.getItem(pageTypes.getItemCount() - 1).equals(lh.localValue("custom")))
			pageTypes.remove(pageTypes.getItemCount() - 1);
	}
	
	private void onCellTypeChange() {
		brailleCell = BrailleCellType.valueOf(cellType.getText());
		onMarginUnitSelected();
		calculateCellsLinesAndUpdate();
	}

	/**
	 * When cells or lines change, adjust bottom and right margins to make room
	 */
	private void onCellLinesChange() {
		marginBottom = pageHeight - marginTop - unitConverter.calculateHeightFromLines((int) pageLines, brailleCell);
		marginRight = pageWidth - marginLeft - unitConverter.calculateWidthFromCells((int) pageCells, brailleCell);
		updateFields();
	}

	/**
	 * When the user switches between displaying margins as inch/mm or cells,
	 * update label suffix and convert fields to new unit
	 */
	private void onMarginUnitSelected() {
		if (isAnyFieldEmpty() || marginLocalUnit == regionalButton.getSelection())
			return;
		if (regionalButton.getSelection()) {
			marginLocalUnit = true;
			marginTopLabel.setText(replaceOldUnitLabel(marginTopLabel, unitName));
			marginBottomLabel.setText(replaceOldUnitLabel(marginBottomLabel, unitName));
			marginLeftLabel.setText(replaceOldUnitLabel(marginLeftLabel, unitName));
			marginRightLabel.setText(replaceOldUnitLabel(marginRightLabel, unitName));
			regionalButton.setSelection(true);
			cellsLinesButton.setSelection(false);
		} else {
			marginLocalUnit = false;
			marginTopLabel.setText(replaceOldUnitLabel(marginTopLabel, "lines"));
			marginBottomLabel.setText(replaceOldUnitLabel(marginBottomLabel, "lines"));
			marginLeftLabel.setText(replaceOldUnitLabel(marginLeftLabel, "cells"));
			marginRightLabel.setText(replaceOldUnitLabel(marginRightLabel, "cells"));
			regionalButton.setSelection(false);
			cellsLinesButton.setSelection(true);
		}
		updateFields();
	}

	private String replaceOldUnitLabel(Label label, String newUnit) {
		//Replace the last word with the new unit
		String text = label.getText();
		text = text.substring(0, text.lastIndexOf(" "));
		return text + " (" + newUnit + ")";
	}

	/**
	 * If this is false the user cleared out a field
	 *
	 * @return
	 */
	private boolean isAnyFieldEmpty() {
		return widthBox.getText().isEmpty()
				|| heightBox.getText().isEmpty()
				|| linesBox.getText().isEmpty()
				|| cellsBox.getText().isEmpty()
				|| marginTopBox.getText().isEmpty()
				|| marginLeftBox.getText().isEmpty()
				|| marginRightBox.getText().isEmpty()
				|| marginBottomBox.getText().isEmpty();
	}

	@Override
	public String validate() {
		if (marginRight + marginLeft + unitConverter.calculateWidthFromCells((int) pageCells, brailleCell) > pageWidth)
			return "incorrectMarginWidth";
		if (marginTop + marginBottom + unitConverter.calculateWidthFromCells((int) pageLines, brailleCell) > pageHeight)
			return "incorrectMarginHeight";
		if (pageHeight < 0 || pageWidth < 0 || pageLines < 0 || pageCells < 0
				|| marginTop < 0 || marginBottom < 0 || marginLeft < 0 || marginRight < 0)
			return "settingsBelowZero";
		return null;
	}

	@Override
	public boolean updateEngine(UTDTranslationEngine engine) {
		PageSettings pageSettings = engine.getPageSettings();
		BrailleSettings brailleSettings = engine.getBrailleSettings();
		boolean updated = false;

		updated = SettingsUIUtils.updateObject(brailleSettings::getCellType, brailleSettings::setCellType,
				brailleCell, updated);
		updated = SettingsUIUtils.updateObject(pageSettings::getPaperHeight, pageSettings::setPaperHeight,
				unitConverter.localUnitsToMM(pageHeight), updated);
		updated = SettingsUIUtils.updateObject(pageSettings::getPaperWidth, pageSettings::setPaperWidth,
				unitConverter.localUnitsToMM(pageWidth), updated);
		updated = SettingsUIUtils.updateObject(pageSettings::getTopMargin, pageSettings::setTopMargin,
				unitConverter.localUnitsToMM(marginTop), updated);
		updated = SettingsUIUtils.updateObject(pageSettings::getBottomMargin, pageSettings::setBottomMargin,
				unitConverter.localUnitsToMM(marginBottom), updated);
		updated = SettingsUIUtils.updateObject(pageSettings::getLeftMargin, pageSettings::setLeftMargin,
				unitConverter.localUnitsToMM(marginLeft), updated);
		updated = SettingsUIUtils.updateObject(pageSettings::getRightMargin, pageSettings::setRightMargin,
				unitConverter.localUnitsToMM(marginRight), updated);

		return updated;
	}

	/**
	 * Disallow non-number and non-navigation keys in the text field
	 *
	 * @param t
	 */
	private static void addDoubleFilter(final Text t, boolean noDecimal) {
		t.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (!Character.isDigit(e.character) && e.keyCode != SWT.BS
						&& e.keyCode != SWT.DEL && e.keyCode != SWT.ARROW_LEFT
						&& e.keyCode != SWT.ARROW_RIGHT) {
					if (noDecimal && e.character == '.')
						e.doit = false;
					else if (e.character != '.' || (e.character == '.' && t.getText().contains(".")))
						e.doit = false;
				}
			}
		});
	}

	private KeyListener makeFieldListener(Supplier<String> getRawValue, DoubleUnaryOperator setParsedValue, Consumer<KeyEvent> function) {
		return new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (isAnyFieldEmpty() || !e.doit)
					return;
				String rawValue = getRawValue.get();
				if (rawValue.equals(".") || rawValue.equals("-"))
					return;
				double value = Double.parseDouble(rawValue);
				setParsedValue.applyAsDouble(value);
				function.accept(e);
			}
		};
	}
}
