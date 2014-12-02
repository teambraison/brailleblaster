package org.brailleblaster.settings;

import java.util.*;

import java.io.BufferedReader;

public class SettingsManagerTest {
	
	public static void main(String[]args){

	SettingsManager settingsManagerTest = new SettingsManager("string");
	
	settingsManagerTest.calculateHeightFromLength(10);
	
	assertEquals(100.0,settingsManagerTest.calculateHeightFromLength(10));
	
	}	
}
