package org.brailleblaster.settings.ui;

import java.awt.Event;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import org.brailleblaster.BBIni;
import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.settings.SettingsManager;
import org.brailleblaster.util.Notify;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Display;//rl
import org.brailleblaster.util.PropertyFileManager;
import org.brailleblaster.settings.ui.ConfigPanel;
import org.brailleblaster.util.FileUtils;

public class PagePropertiesTab {
	HashMap<String, String> settingsMap;
	SettingsManager sm;
	TabItem item;
	Composite group;
	PropertyFileManager pfm;
	FileUtils fu;
	BBIni bbini;
	private static final String userSettings = BBIni.getUserSettings();

	Group sizeGroup, marginGroup, pageGroup, buttonGroup, unitsGroup;//
	Label pageSizeLabel, widthLabel, heightLabel, linesPerPageLabel,
			cellsPerLineLabel, marginTopLabel, marginBottomLabel,
			marginLeftLabel, marginRightLabel;

	Combo pageTypes;
	Text widthBox, heightBox, linesBox, cellsBox, marginTopBox, marginLeftBox,
			marginRightBox, marginBottomBox;
	Button okButton, cancelButton, regionalButton, cellsLinesButton;//

	boolean listenerLocked;
	LocaleHandler lh;
	public String currentUnits = "regional";//
	DecimalFormat df = new DecimalFormat("#.#");
	boolean userModified;

	PagePropertiesTab(TabFolder folder, final SettingsManager sm,
			HashMap<String, String> settingsMap) {

		lh = new LocaleHandler();
		this.sm = sm;
		this.settingsMap = settingsMap;
		listenerLocked = false;
		item = new TabItem(folder, 0);
		item.setText(lh.localValue("pageProperties"));

		group = new Composite(folder, 0);
		group.setLayout(new FormLayout());
		item.setControl(group);
		setFormLayout(group, 0, 100, 0, 60);

		unitsGroup = new Group(group, SWT.BORDER);
		unitsGroup.setText(lh.localValue("measurementUnits"));
		unitsGroup.setLayout(new FillLayout());
		setFormLayout(unitsGroup, 0, 100, 0, 20);

		regionalButton = new Button(unitsGroup, SWT.RADIO);
		regionalButton.setText(lh.localValue("regional"));

		cellsLinesButton = new Button(unitsGroup, SWT.RADIO);
		cellsLinesButton.setText(lh.localValue("cellsLines"));

		PropertyFileManager pfm = new PropertyFileManager(userSettings);
		String value = pfm.getProperty("currentUnits");
		if (value.equals("cellsLines"))
			cellsLinesButton.setSelection(true);
		else
			regionalButton.setSelection(true);

		sizeGroup = new Group(group, SWT.BORDER);
		sizeGroup.setText(lh.localValue("pageSize"));
		sizeGroup.setLayout(new FillLayout());
		setFormLayout(sizeGroup, 0, 100, 20, 65);

		pageGroup = new Group(sizeGroup, 0);
		pageGroup.setLayout(new GridLayout(2, true));

		pageSizeLabel = new Label(pageGroup, 0);
		pageSizeLabel.setText(lh.localValue("pageSize"));
		setGridData(pageSizeLabel);

		pageTypes = new Combo(pageGroup, SWT.NONE);
		setStandardPages();
		setDefault();
		setGridData(pageTypes);

		widthLabel = new Label(pageGroup, 0);
		widthLabel.setText(lh.localValue("width"));
		widthBox = new Text(pageGroup, SWT.BORDER);
		addDoubleListener(widthBox);
		setGridData(widthBox);
		setValue(widthBox, "paperWidth");

		heightLabel = new Label(pageGroup, 0);
		heightLabel.setText(lh.localValue("height"));
		heightBox = new Text(pageGroup, SWT.BORDER);
		addDoubleListener(heightBox);
		setGridData(heightBox);
		setValue(heightBox, "paperHeight");

		linesPerPageLabel = new Label(pageGroup, 0);
		linesPerPageLabel.setText(lh.localValue("linesPerPage"));

		linesBox = new Text(pageGroup, SWT.BORDER);
		setGridData(linesBox);
		linesBox.setText((String.valueOf(calculateLinesPerPage(Double
				.valueOf(settingsMap.get("paperHeight"))))));

		cellsPerLineLabel = new Label(pageGroup, 0);
		cellsPerLineLabel.setText(lh.localValue("cellsPerLine"));

		cellsBox = new Text(pageGroup, SWT.BORDER);
		setGridData(cellsBox);
		cellsBox.setText(String.valueOf(calculateCellsPerLine(Double
				.valueOf(settingsMap.get("paperWidth")))));

		marginGroup = new Group(group, SWT.BORDER);
		marginGroup.setLayout(new GridLayout(2, true));
		marginGroup.setText(lh.localValue("margins"));
		setFormLayout(marginGroup, 0, 100, 65, 100);

		marginTopLabel = new Label(marginGroup, 0);
		marginTopLabel.setText(lh.localValue("topMargin"));
		marginTopBox = new Text(marginGroup, SWT.BORDER);
		addDoubleListener(marginTopBox);
		setGridData(marginTopBox);
		setValueForMargins(marginTopBox, "topMargin");
		addMarginListener(marginTopBox, "topMargin");

		marginBottomLabel = new Label(marginGroup, 0);
		marginBottomLabel.setText(lh.localValue("bottomMargin"));
		marginBottomBox = new Text(marginGroup, SWT.BORDER);
		addDoubleListener(marginBottomBox);
		setGridData(marginBottomBox);
		setValueForMargins(marginBottomBox, "bottomMargin");
		addMarginListener(marginBottomBox, "bottomMargin");

		marginLeftLabel = new Label(marginGroup, 0);
		marginLeftLabel.setText(lh.localValue("leftMargin"));
		marginLeftBox = new Text(marginGroup, SWT.BORDER);
		addDoubleListener(marginLeftBox);
		setGridData(marginLeftBox);
		setValueForMargins(marginLeftBox, "leftMargin");
		addMarginListener(marginLeftBox, "leftMargin");

		marginRightLabel = new Label(marginGroup, 0);
		marginRightLabel.setText(lh.localValue("rightMargin"));
		marginRightBox = new Text(marginGroup, SWT.BORDER);
		addDoubleListener(marginRightBox);
		setGridData(marginRightBox);
		setValueForMargins(marginRightBox, "rightMargin");
		addMarginListener(marginRightBox, "rightMargin");

		Control[] tabList = { sizeGroup, marginGroup, unitsGroup };
		group.setTabList(tabList);

		if (widthBox.getText().length() > 0 || heightBox.getText().length() > 0)
			checkStandardSizes();

		addListeners();
	}

	private void addDoubleListener(final Text t) {
		t.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (!Character.isDigit(e.character) && e.keyCode != SWT.BS
						&& e.keyCode != SWT.DEL && e.keyCode != SWT.ARROW_LEFT
						&& e.keyCode != SWT.ARROW_RIGHT) {
					if (e.character != '.'
							|| (e.character == '.' && t.getText().contains(".")))
						e.doit = false;
				}
			}
		});
	}

	private void addMarginListener(final Text t, final String type) {

		if (type.equals("leftMargin") || type.equals("rightMargin")) {

			t.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					if (!userModified) {
//						Double margin;
//						if (currentUnits.equals("regional"))
//							margin = getDoubleValue(t);
//						else
//							margin = calcWidthFromCells(getDoubleValue(t));
//
//						if (margin >= getDoubleValue(widthBox)
//								|| (getDoubleValue(marginLeftBox)
//										+ getDoubleValue(marginRightBox) >= getDoubleValue(widthBox))) {
//							new Notify(lh.localValue("incorrectMarginWidth"));
//							t.setText(settingsMap.get(type));
//						}// if notCorrectValue
//						else {

							if (listenerLocked) {
								if (cellsLinesButton.getSelection()) {
									settingsMap.put(
											type,
											String.valueOf(df
													.format(calcWidthFromCells(getDoubleValue((t))))));
								}// if cellsLinesButton
								else {
									settingsMap.put(type, getStringValue(t));
								}// else regionalButton
							} // if listenerLocked
							else {
								if (regionalButton.getSelection()) {
									settingsMap.put(type, getStringValue(t));
									cellsBox.setText(String
											.valueOf(calculateCellsPerLine(getDoubleValue(widthBox))));
									linesBox.setText(String
											.valueOf(calculateLinesPerPage(getDoubleValue(heightBox))));

								}// if regionalButton
								else {
									settingsMap.put(
											type,
											String.valueOf(df
													.format(calcWidthFromCells(getDoubleValue(t)))));
									cellsBox.setText(String
											.valueOf(calculateCellsPerLine(getDoubleValue(widthBox))));
									linesBox.setText(String
											.valueOf(calculateLinesPerPage(getDoubleValue(heightBox))));
								}// else cellsLinesButton
							}// else notListenerLocked
//						}// else correctValues

					}// modifyText
				}// if not userModified
			});// modifyListener right or left
		}// if type equals right or left
		else {
			t.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {

					if (!userModified) {
//						Double margin;
//						if (regionalButton.getSelection())
//							margin = getDoubleValue(t);
//						else
//							margin = sm.calcHeightFromLines(getDoubleValue(t));
//
//						if (margin >= Double.valueOf(heightBox.getText())
//								|| (getDoubleValue(marginTopBox)
//										+ getDoubleValue(marginBottomBox) >= getDoubleValue(widthBox))) {
//							new Notify(lh.localValue("incorectMarginHeight"));
//							if (regionalButton.getSelection()) {
//								t.setText(settingsMap.get(type));
//							} else {
//								t.setText(String.valueOf(calculateLinesPerInch(Double
//										.valueOf(settingsMap.get(type)))));
//							}
//						}// if incorrectSettings
//						else {

							if (listenerLocked) {
								if (cellsLinesButton.getSelection()) {
									settingsMap.put(
											type,
											String.valueOf(df.format(sm
													.calcHeightFromLines(getDoubleValue(t)))));
								}// if cellsLinesButton
								else {

									settingsMap.put(type, (getStringValue(t)));
								}// else regionalButton
							}// if listenerLocked
							else {
								if (regionalButton.getSelection()) {
									settingsMap.put(type, getStringValue(t));
									cellsBox.setText(String
											.valueOf(calculateCellsPerLine(getDoubleValue(widthBox))));
									linesBox.setText(String
											.valueOf(calculateLinesPerPage(getDoubleValue(heightBox))));
								}// if regionalButton
								else {
									settingsMap.put(
											type,
											String.valueOf(df.format(sm
													.calcHeightFromLines(getDoubleValue(t)))));
									cellsBox.setText(String
											.valueOf(calculateCellsPerLine(getDoubleValue(widthBox))));
									linesBox.setText(String
											.valueOf(calculateLinesPerPage(getDoubleValue(heightBox))));
								}// else cellsLinesButton
							}// else not listenerLocked
//						}// else correctSettings
					}// userModified false
				}// modify text
			});// modifyListener for top or bottom
		}// if type equals top or bottom

	}// addMargin Listener

	private void setStandardPages() {
		Page[] temp = sm.getStandardSizes();
		for (int i = 0; i < temp.length; i++) {
			if (sm.isMetric())
				pageTypes.add(temp[i].type + " (" + temp[i].mmWidth + ", "
						+ temp[i].mmHeight + ")");
			else
				pageTypes.add(temp[i].type + " (" + temp[i].width + ", "
						+ temp[i].height + ")");
		}
	}

	private int searchList(char c) {
		for (int i = 0; i < pageTypes.getItemCount(); i++) {
			if (pageTypes.getItem(i).toLowerCase().charAt(0) == c)
				return i;
		}

		return -1;
	}

	private void setGridData(Control c) {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		c.setLayoutData(gridData);
	}

	private void addListeners() {

		regionalButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				if (regionalButton.getSelection()) {
					listenerLocked = true;
					currentUnits = "regional";
					saveSettings(currentUnits);
					modifyMargins();
					listenerLocked = false;
				}
			}
		});
		cellsLinesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (cellsLinesButton.getSelection()) {
					listenerLocked = true;
					currentUnits = "cellsLines";
					saveSettings(currentUnits);
					modifyMargins();
					listenerLocked = false;
				}

			}
		});

		widthBox.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (getDoubleValue(widthBox) == 0) {
					if (e.keyCode == SWT.TAB) {
						new Notify(lh.localValue("widthZero"));
						e.doit = false;
					}
				}
			}
		});

		heightBox.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (getDoubleValue(heightBox) == 0) {
					if (e.keyCode == SWT.TAB) {
						new Notify(lh.localValue("heightZero"));
						e.doit = false;
					}
				}
			}
		});

		widthBox.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!listenerLocked) {
					settingsMap.put("paperWidth", getStringValue(widthBox));
					checkStandardSizes();
				}
			}
		});

		heightBox.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!listenerLocked) {
					settingsMap.put("paperHeight", getStringValue(heightBox));
					checkStandardSizes();
				}
			}
		});

		cellsBox.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// oldCellsBox = settingsMap.get("cellsPerLine");
				// oldLeftMargin = settingsMap.get("leftMargin");
				// oldRightMargin = settingsMap.get("rightMargin");
				if (!userModified) {
					userModifiesCellsBox();
				}
				if (!listenerLocked) {
					settingsMap.put("cellsPerLine", getStringValue(cellsBox));
				}
			}
		});

		linesBox.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {

				if (!userModified) {
					userModifiesLinesBox();
				}

				if (!listenerLocked)
					settingsMap.put("linesPerPage", getStringValue(linesBox));

			}// modifyText

		});// modifyListener

		pageTypes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Page p = sm.getStandardSizes()[pageTypes.getSelectionIndex()];
				if (sm.isMetric()) {
					widthBox.setText(String.valueOf(p.mmWidth));
					heightBox.setText(String.valueOf(p.mmHeight));
					cellsBox.setText(String
							.valueOf(calculateCellsPerLine(p.mmWidth)));
					linesBox.setText(String
							.valueOf(calculateLinesPerPage(p.mmHeight)));
				} else {
					widthBox.setText(String.valueOf(p.width));
					heightBox.setText(String.valueOf(p.height));
					cellsBox.setText(String
							.valueOf(calculateCellsPerLine(p.width)));
					linesBox.setText(String
							.valueOf(calculateLinesPerPage(p.height)));
				}

				if (pageTypes.getItem(pageTypes.getItemCount() - 1).equals(
						lh.localValue("custom")))
					pageTypes.remove(pageTypes.getItemCount() - 1);
			}
		});

		pageTypes.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode != SWT.ARROW_DOWN && e.keyCode != SWT.ARROW_UP) {
					int loc = searchList(e.character);
					if (loc != -1) {
						e.doit = false;
						pageTypes.select(loc);
						cellsBox.setText(String.valueOf(calculateCellsPerLine(Double
								.valueOf(sm.getStandardSizes()[loc].width))));
						linesBox.setText(String.valueOf(calculateLinesPerPage(Double
								.valueOf(sm.getStandardSizes()[loc].height))));
					} else
						e.doit = false;
				}
			}
		});
	}

	private void checkStandardSizes() {
		Double width = getDoubleValue(widthBox);
		Double height = getDoubleValue(heightBox);

		if (width != 0 && height != 0) {
			boolean found = false;
			for (int i = 0; i < sm.getStandardSizes().length && !found; i++) {
				if (checkEqualHeight(sm.getStandardSizes()[i], height)
						&& checkEqualWidth(sm.getStandardSizes()[i], width)) {
					pageTypes.select(i);
					found = true;

					if (!sm.isMetric()) {
						cellsBox.setText(String
								.valueOf(calculateCellsPerLine(sm
										.getStandardSizes()[i].width)));
						linesBox.setText(String
								.valueOf(calculateLinesPerPage(sm
										.getStandardSizes()[i].height)));
					} else {
						cellsBox.setText(String
								.valueOf(calculateCellsPerLine(sm
										.getStandardSizes()[i].mmWidth)));
						linesBox.setText(String
								.valueOf(calculateLinesPerPage(sm
										.getStandardSizes()[i].mmHeight)));
					}

					if (pageTypes.getItem(pageTypes.getItemCount() - 1).equals(
							lh.localValue("custom")))
						pageTypes.remove(pageTypes.getItemCount() - 1);
				}
			}

			if (!found) {
				if (pageTypes.getItemCount() == sm.getStandardSizes().length) {
					pageTypes.add(lh.localValue("custom"));
					pageTypes.select(pageTypes.getItemCount() - 1);
				} else
					pageTypes.select(pageTypes.getItemCount() - 1);

				cellsBox.setText(String.valueOf(calculateCellsPerLine(Double
						.valueOf(widthBox.getText()))));
				linesBox.setText(String.valueOf(calculateLinesPerPage(Double
						.valueOf(heightBox.getText()))));
			}
		}
	}

	/**
	 * @return
	 */
	public String validate() {
		if (Integer.valueOf(cellsBox.getText()) < Integer.parseInt(settingsMap
				.get("minCellsPerLine")))
			return "invalidSettingsCells";
		else if (Integer.valueOf(linesBox.getText()) < Integer
				.parseInt(settingsMap.get("minLinesPerPage")))
			return "invalidSettingsLines";
		else if (!cellsLinesButton.getSelection()&&(getDoubleValue(marginRightBox)+getDoubleValue
				(marginLeftBox)+(calcWidthFromCells(getDoubleValue(cellsBox))))>= 
			(Double.valueOf(settingsMap.get("paperWidth")))) 
				return "incorrectMarginWidth";
		else if (!cellsLinesButton.getSelection()&&(getDoubleValue(marginTopBox)+
				getDoubleValue(marginBottomBox)+(sm.calcHeightFromLines(getDoubleValue(linesBox))))>=
				(Double.valueOf(settingsMap.get("paperHeight"))))
			return "incorrectMarginHeight";
		else if (!regionalButton.getSelection()&&((sm.calcHeightFromLines(getDoubleValue(linesBox)))+
				(sm.calcHeightFromLines(getDoubleValue(marginTopBox)))+(sm.calcHeightFromLines
						(getDoubleValue(marginBottomBox)))>=(Double.valueOf(settingsMap.get
								("paperHeight")))))
			return "incorrectMarginHeight";
		else if (!regionalButton.getSelection()&&((calcWidthFromCells(getDoubleValue(cellsBox)))+
				(calcWidthFromCells(getDoubleValue(marginLeftBox)))+(calcWidthFromCells(getDoubleValue
						(marginRightBox))))>=(Double.valueOf(settingsMap.get("paperWidth"))))
			return "incorrectMarginWidth";
		else if (getDoubleValue(marginRightBox)<0||getDoubleValue(marginLeftBox)<0||getDoubleValue(marginTopBox)<0||getDoubleValue(marginBottomBox)<0||
				getDoubleValue(linesBox)<0||getDoubleValue(cellsBox)<0)
			return "invalidSettingsCells";
		else		
		return "SUCCESS";
	}

	private void setValue(Text text, String key) {
		if (settingsMap.containsKey(key))
			text.setText(settingsMap.get(key));
	}

	private void setValueForMargins(Text text, String key) {
		if (regionalButton.getSelection()) {
			if (settingsMap.containsKey(key))
				text.setText(settingsMap.get(key));
		} else {
			if (key.equals("leftMargin") || key.equals("rightMargin")) {
				if (settingsMap.containsKey(key))
					text.setText(String.valueOf(calculateCellsPerInch(Double
							.valueOf(settingsMap.get(key)))));
			} else {
				if (settingsMap.containsKey(key))
					text.setText(String.valueOf(calculateLinesPerInch(Double
							.valueOf(settingsMap.get(key)))));
			}
		}
	}

	private void setDefault() {
		if (settingsMap.containsKey("paperWidth")
				&& settingsMap.containsKey("paperHeight")) {
			for (int i = 0; i < sm.getStandardSizes().length; i++) {
				if (checkEqualWidth(sm.getStandardSizes()[i],
						Double.valueOf(settingsMap.get("paperWidth")))
						&& checkEqualHeight(sm.getStandardSizes()[i],
								Double.valueOf(settingsMap.get("paperHeight")))) {
					pageTypes.select(i);
					break;
				}
			}
		}
	}

	public TabItem getTab() {
		return item;
	}

	private double getDoubleValue(Text t) {
		if (t.getText().length() == 0)
			return 0.0;
		else
			return Double.valueOf(t.getText());
	}

	private String getStringValue(Text t) {
		if (t.getText().length() == 0)
			return "0";
		else
			return t.getText();
	}

	public Boolean getRadioSelection() {
		Boolean isRegional = regionalButton.getSelection();
		return isRegional;

	}

	public void modifyMargins() {

		if (!userModified) {

			Boolean isRegional = getRadioSelection();

			if (isRegional) {

				String leftMargin = settingsMap.get("leftMargin");
				marginLeftBox.setText(df.format(Double.valueOf(leftMargin)));

				String rightMargin = settingsMap.get("rightMargin");
				marginRightBox.setText(df.format(Double.valueOf(rightMargin)));

				String topMargin = settingsMap.get("topMargin");
				marginTopBox.setText(df.format(Double.valueOf(topMargin)));

				String bottomMargin = settingsMap.get("bottomMargin");
				marginBottomBox
						.setText(df.format(Double.valueOf(bottomMargin)));

			}

			else {

				String leftMargin = settingsMap.get("leftMargin");
				String convertedLeftMargin = String
						.valueOf(calculateCellsPerLine(Double
								.valueOf(leftMargin)));
				marginLeftBox.setText(convertedLeftMargin);

				String rightMargin = settingsMap.get("rightMargin");
				String convertedRightMargin = String
						.valueOf(calculateCellsPerLine(Double
								.valueOf(rightMargin)));
				marginRightBox.setText(convertedRightMargin);

				String topMargin = settingsMap.get("topMargin");
				String convertedTopMargin = String
						.valueOf(calculateLinesPerPage(Double
								.valueOf(topMargin)));
				marginTopBox.setText(convertedTopMargin);

				String bottomMargin = settingsMap.get("bottomMargin");
				String convertedBottomMargin = String
						.valueOf(calculateLinesPerPage(Double
								.valueOf(bottomMargin)));
				marginBottomBox.setText(convertedBottomMargin);

			}// else cellsLines
		}// if userModified false
	}// modify margins

	public void saveSettings(String currentUnits) {
		PropertyFileManager pfm = new PropertyFileManager(userSettings);
		pfm.save("currentUnits", currentUnits);
	}

	private int calculateCellsPerLine(double pWidth) {

		if (!listenerLocked) {

			double cellWidth;
			if (!sm.isMetric())
				cellWidth = 0.246063;
			else
				cellWidth = 6.25;

			if (settingsMap.containsKey("leftMargin"))
				pWidth -= Double.valueOf(settingsMap.get("leftMargin"));

			if (settingsMap.containsKey("rightMargin"))
				pWidth -= Double.valueOf(settingsMap.get("rightMargin"));

			return (int) (pWidth / cellWidth);
		} else {
			double cellWidth;
			if (!sm.isMetric())
				cellWidth = 0.246063;
			else
				cellWidth = 6.25;

			return (int) (pWidth / cellWidth);
		}

	}

	private int calculateLinesPerInch(double inches) {
		double cellHeight;
		if (!sm.isMetric())
			cellHeight = 0.393701;
		else
			cellHeight = 10;
		return (int) (inches / cellHeight);
	}

	private int calculateLinesPerPage(double pHeight) {

		if (!listenerLocked) {

			double cellHeight;
			if (!sm.isMetric())
				cellHeight = 0.393701;
			else
				cellHeight = 10;

			if (settingsMap.containsKey("topMargin"))
				pHeight -= Double.valueOf(settingsMap.get("topMargin"));

			if (settingsMap.containsKey("bottomMargin"))
				pHeight -= Double.valueOf(settingsMap.get("bottomMargin"));

			return (int) (pHeight / cellHeight);
		} else {
			double cellHeight;
			if (!sm.isMetric())
				cellHeight = 0.393701;
			else
				cellHeight = 10;

			return (int) (pHeight / cellHeight);
		}
	}

	public int calculateCellsPerInch(double inches) {
		double cellWidth;
		if (!sm.isMetric())
			cellWidth = 0.246063;
		else
			cellWidth = 6.25;

		return (int) (inches / cellWidth);
	}

	private double calcWidthFromCells(int numberOfCells) {
		double cellWidth;
		if (!sm.isMetric())
			cellWidth = 0.246063;
		else
			cellWidth = 6.25;

		return cellWidth * numberOfCells;

	}

	private double calcWidthFromCells(double numberOfCells) {
		double cellWidth;
		if (!sm.isMetric())
			cellWidth = 0.246063;
		else
			cellWidth = 6.25;

		return cellWidth * numberOfCells;

	}

	private boolean checkEqualWidth(Page p, double width) {
		if (sm.isMetric())
			return p.mmWidth == width;
		else
			return p.width == width;
	}

	private boolean checkEqualHeight(Page p, double height) {
		if (sm.isMetric())
			return p.mmHeight == height;
		else
			return p.height == height;
	}

	private void setFormLayout(Control c, int left, int right, int top,
			int bottom) {
		FormData location = new FormData();

		location.left = new FormAttachment(left);
		location.right = new FormAttachment(right);
		location.top = new FormAttachment(top);
		location.bottom = new FormAttachment(bottom);

		c.setLayoutData(location);
	}

	private void userModifiesLinesBox() {

		if (!directMarginEdit()) {

			userModified = true;

			int maxLines = calculateLinesPerInch(Double.valueOf(settingsMap
					.get("paperHeight")));
			if (cellsLinesButton.getSelection()) {
//				if ((getDoubleValue(marginTopBox) + getDoubleValue(linesBox)) > maxLines) {
//					new Notify(lh.localValue("incorectMarginHeight"));
//					linesBox.setText(settingsMap.get("linesPerPage"));
//				}// if incorrect input cellsLines
//				else {
					marginBottomBox
							.setText(String
									.valueOf((int) (maxLines - (getDoubleValue(linesBox) + (getDoubleValue(marginTopBox))))));
					settingsMap
							.put("bottomMargin",
									String.valueOf(sm
											.calcHeightFromLines(getDoubleValue(marginBottomBox))));
					settingsMap.put("linesPerPage", getStringValue(linesBox));
//				}// else correct input cellsLines
			}// if cellsLines button
			else {
//				if ((calculateLinesPerInch(getDoubleValue(marginTopBox)) + getDoubleValue(linesBox)) > maxLines) {
//					new Notify(lh.localValue("incorectMarginHeight"));
//					linesBox.setText(settingsMap.get("linesPerPage"));
//					marginBottomBox.setText(settingsMap.get("bottomMargin"));
//				}// if incorrect input regional
//				else {
					marginBottomBox.setText(String.valueOf(df.format(sm
							.calcHeightFromLines(maxLines)
							- (sm.calcHeightFromLines(getDoubleValue(linesBox)
									+ (getDoubleValue(marginTopBox)))))));
					settingsMap.put("bottomMargin",
							String.valueOf(getDoubleValue(marginBottomBox)));
					settingsMap.put("linesPerPage",
							String.valueOf(linesBox.getText()));
//				}// else correct input regional
			}// else regionalButton

			System.out.println(maxLines);
			System.out.println(settingsMap);

			userModified = false;

		}// is isn't focus control

	}// userModifiesLinesBox

	private void userModifiesCellsBox() {

		if (!directMarginEdit()) {

			userModified = true;
			int maxCells = calculateCellsPerInch(Double.valueOf(settingsMap
					.get("paperWidth")));
//
			if (cellsLinesButton.getSelection()) {
//				if ((getDoubleValue(marginLeftBox) + getDoubleValue(cellsBox)) > maxCells) {
//					new Notify(lh.localValue("incorrectMarginWidth"));
//					cellsBox.setText(settingsMap.get("cellsPerLine"));
//				}// if incorrect input cellsLines
//				else {
					marginRightBox
							.setText(String
									.valueOf((int) (maxCells - (getDoubleValue(cellsBox) + (getDoubleValue(marginLeftBox))))));
					settingsMap
							.put("rightMargin",
									String.valueOf(sm
											.calcWidthFromCells(getDoubleValue(marginRightBox))));
					settingsMap.put("cellsPerLine", getStringValue(cellsBox));
//				}// else correct input cellsLines
			}// if cellsLines Buttons
			else {
//				if ((calculateCellsPerInch(getDoubleValue(marginLeftBox)) + getDoubleValue(cellsBox)) > maxCells) {
//					new Notify(lh.localValue("incorrectMarginWidth"));
//					cellsBox.setText(settingsMap.get("cellsPerLine"));
//				}// if incorrect input regional
//				else {
					marginRightBox
							.setText(String.valueOf(df.format(sm
									.calcWidthFromCells(maxCells)
									- (sm.calcWidthFromCells(getDoubleValue(cellsBox)) + getDoubleValue
											(marginLeftBox)))));
					settingsMap.put("rightMargin",
							String.valueOf(getDoubleValue(marginRightBox)));
					settingsMap.put("cellsPerLine",
							String.valueOf(cellsBox.getText()));
//				}// else correct input regional

			}// else regional button

			System.out.println(maxCells);
			System.out.println(settingsMap);
			userModified = false;
		}// if not directMarginEdit
	}// userModifiesCellsBox

	private boolean directMarginEdit() {
		if (marginBottomBox.isFocusControl() || marginTopBox.isFocusControl()
				|| marginLeftBox.isFocusControl()
				|| marginRightBox.isFocusControl()) {
			return true;
		}// if
		else {
			return false;
		}// else
	}// directMarginEdit

}// pagePropertiesTabClass

