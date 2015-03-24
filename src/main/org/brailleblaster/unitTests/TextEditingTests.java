package org.brailleblaster.unitTests;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

import org.brailleblaster.BBIni;
import org.brailleblaster.document.Resolver;
import org.brailleblaster.util.Zipper;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

@RunWith(BlockJUnit4ClassRunner.class)
public class TextEditingTests {
	
	//selects range and inserts text
	public static void typeTextInRange(SWTBotStyledText bot, String text, int line, int start, int length){
		bot.setFocus();
		bot.navigateTo(line, start);
		bot.selectRange(line, start, length);
		bot.typeText(text);
	}
	
	//inserts text at current position or current selection
	public static void typeText(SWTBotStyledText bot, String text){
		bot.typeText(text);
	}
	
	public static void deleteSelection(SWTBotStyledText bot, int line, int start, int length, int keyStroke){
		bot.setFocus();
		bot.navigateTo(line, start);
		bot.selectRange(line, start, length);
		bot.pressShortcut(KeyStroke.getInstance(keyStroke));
	}
	
	protected static void pressDelete(SWTBotStyledText bot, int line, int start, int times){
	//	bot.setFocus();
	//	bot.navigateTo(line, start);
		for(int i = 0; i < times; i++)
			bot.pressShortcut(KeyStroke.getInstance(SWT.DEL));
	}
	
	public static void pressKey(SWTBotStyledText bot, int key, int times){
		for(int i = 0; i < times; i++)
			bot.pressShortcut(KeyStroke.getInstance(key));
	}
	
	protected static void pressDelete(SWTBotStyledText bot, int times){
		//	bot.setFocus();
		//	bot.navigateTo(line, start);
			for(int i = 0; i < times; i++)
				bot.pressShortcut(KeyStroke.getInstance(SWT.DEL));
	}
	
	protected static void pressBackspace(SWTBotStyledText bot, int times){
		for(int i = 0; i < times; i++)
			bot.pressShortcut(KeyStroke.getInstance(SWT.BS));
	}
	
	protected static void pressBackspace(SWTBotStyledText bot, int line, int start, int times){
		bot.setFocus();
		bot.navigateTo(line, start);
		for(int i = 0; i < times; i++)
			bot.pressShortcut(KeyStroke.getInstance(SWT.BS));
	}
	
	public static void cut(SWTBot bot, SWTBotStyledText textBot, int line, int start, int length){
		textBot.navigateTo(line, start);
		textBot.selectRange(line, start, length);
		bot.menu("&Cut").click();
	}
	
	protected static void cut(SWTBot bot){
		bot.menu("&Cut").click();
	}
	
	public static void copy(SWTBotStyledText textBot, int line, int start, int length){
		textBot.navigateTo(line, start);
		textBot.selectRange(line, start, length);
		textBot.pressShortcut(SWT.CTRL ,'c');
	}
	
	public static void paste(SWTBot bot, SWTBotStyledText textBot, int line, int start, int length){
		textBot.navigateTo(line, start);
		textBot.selectRange(line, start, length);
		bot.menu("&Paste").click();
	}
	
	public static void pasteUsingKeyboardShortcut(SWTBotStyledText textBot, int line, int start, int length){
		textBot.navigateTo(line, start);
		textBot.selectRange(line, start, length);
		textBot.pressShortcut(SWT.CTRL, 'v');
	}
	
	public static void cutUsingKeyboardShortcut(SWTBotStyledText textBot, int line, int start, int length){
		textBot.navigateTo(line, start);
		textBot.selectRange(line, start, length);
		textBot.pressShortcut(SWT.CTRL ,'x');
	}
	
	protected static void cutUsingKeyboardShortcut(SWTBotStyledText textBot){
		textBot.pressShortcut(SWT.CTRL ,'x');
	}
	
	public static void forceUpdate(SWTBot bot, SWTBotStyledText textBot){
		bot.styledText(1).setFocus();
		textBot.setFocus();
	}
	
	protected static String getResultBySelection(SWTBotStyledText bot, int line, int start, int length){
		bot.selectRange(line, start, length);
		return bot.getSelection();
	}
	
	protected static void refresh(SWTBotStyledText textBot){
		textBot.pressShortcut(KeyStroke.getInstance(SWT.F5));
	}
	
	protected static void selectAll(SWTBot bot, SWTBotStyledText textBot){
		bot.menu("Select All").click();
	}
	
	public static void navigateTo(SWTBotStyledText textBot, int pos){
		textBot.setFocus();
		textBot.navigateTo(0,0);
		for(int i = 0; i < pos; i++)
			textBot.pressShortcut(KeyStroke.getInstance(SWT.ARROW_RIGHT));
	}
	
	public static void openStylePanel(SWTBot bot){
		bot.menu("Style Panel").click();
	}
	
	public static void selectTree(SWTBot bot, String menu){
		SWTBotMenu menuBot = bot.menu("Tree");
		menuBot.menu(menu).click();
	}
	
	protected static void selectEditorView(SWTBot bot, String menu){
		SWTBotMenu menuBot = bot.menu("Editors");
		menuBot.menu(menu).click();
	}
	
	public static void applyStyle(SWTBot bot, String item){
		SWTBotTable tableBot = bot.table(0);
		tableBot.select(tableBot.indexOf(item, 1));
		bot.button("Apply").click();
	}
	
	protected static void removeStyle(SWTBot bot, String item){
		SWTBotTable tableBot = bot.table(0);
		tableBot.select(tableBot.indexOf(item, 1));
		bot.button("Remove").click();
	}
	
	public static Document openFile(String fileName){	
		File file = new File (fileName);
		XMLReader r = null;
		try {
			r = XMLReaderFactory.createXMLReader();
			r.setEntityResolver(new Resolver());
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(r != null){
			Builder parser = new Builder(r, false);
			try {
				return parser.build (file);
			} 
			catch(ConnectException e){
				e.printStackTrace();	
				return null;
			}
			catch(UnknownHostException e){
				e.printStackTrace();	
				return null;
			}
			catch (ParsingException e) {
				e.printStackTrace();	
				return null;
			} 
			catch (IOException e) {
				e.printStackTrace();	
				return null;
			}
			catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
		
		return null;
	}
	
	@SuppressWarnings({ "resource" })
	public static boolean unzipFile(String filePath, String unzipPath){    
		FileInputStream fis = null;
		ZipInputStream zipIs = null;
	    ZipEntry zEntry = null;
	    try {
	    	fis = new FileInputStream(filePath);
	    	zipIs = new ZipInputStream(new BufferedInputStream(fis));
	    	while((zEntry = zipIs.getNextEntry()) != null){
	    		try{
	    			byte[] tmp = new byte[4*1024];
	    			FileOutputStream fos = null;
	    			String opFilePath = unzipPath + BBIni.getFileSep() +zEntry.getName();
	    			fos = new FileOutputStream(opFilePath);
	    			int size = 0;
	    			while((size = zipIs.read(tmp)) != -1){
	    				fos.write(tmp, 0 , size);
	    			}
	    			fos.flush();
	    			fos.close();
	                } catch(Exception ex){
	                	ex.printStackTrace();
	                	return false;
	                }
	            }
	            zipIs.close();
	    } catch (FileNotFoundException e) {
	    	e.printStackTrace();
	    	return false;
	    } catch (IOException e) {
	    	e.printStackTrace();
	    	return false;
	    }
	    
	    return true;
	}
	
	public static void openEpub(String path, String out){
		Zipper zipper = new Zipper();
		zipper.Unzip(path, out);
	}
}
