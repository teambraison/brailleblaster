package org.brailleblaster.printers;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import org.brailleblaster.localization.LocaleHandler;
import org.brailleblaster.util.Notify;

public class PrintersManager implements Printable {

    int[] pageBreaks;  // array of page break line positions.

    String[] textLines;
    String textToPrint = null;
    
    LocaleHandler lh = new LocaleHandler();
    
    private void initTextLines() {
    	int numLines = 0;
    	int numChar = 0;
    	int j = 0;
        char c = 0;
        if (textLines == null) {
        	for (j = 0; j < textToPrint.length(); j++) { 
        		if ((++numChar > 80) || ((c = textToPrint.charAt(j)) == 0x0a) ){
        			numLines++;
        			numChar = 0;
        		} 
        	}

        	if (c != 0x0a) {
        		textToPrint = textToPrint + "\n";
        		numLines++;
        	}
        	textLines = new String[numLines];
        	int end = 0;
        	int start = 0;
        	numChar = 0;
        	int i = 0;
            while (i < numLines) {
            	for (end = start; end < textToPrint.length(); end++) {  
            		if (((c=textToPrint.charAt(end)) == 0x0a) || (++numChar > 80)) {
                    	textLines[i++] = textToPrint.substring(start, end);
                    	numChar = 0;
                        start = end + 1;
            		}
            	}
            }
        }
    }

    public int print(Graphics g, PageFormat pf, int pageIndex)
             throws PrinterException {

        Font font = new Font("Serif", Font.PLAIN, 10);
        FontMetrics metrics = g.getFontMetrics(font);
        int lineHeight = metrics.getHeight();

        if (pageBreaks == null) {
            initTextLines();
            int linesPerPage = ((int)(pf.getImageableHeight()/lineHeight)) - 2;
            int numBreaks = (textLines.length-1)/linesPerPage;
            pageBreaks = new int[numBreaks];
            for (int b=0; b<numBreaks; b++) {
                pageBreaks[b] = (b+1)*linesPerPage; 
            }
        }

        if (pageIndex > pageBreaks.length) {
            return NO_SUCH_PAGE;
        }

        /* User (0,0) is typically outside the imageable area, so we must
         * translate by the X and Y values in the PageFormat to avoid clipping
         * Since we are drawing text we
         */
        Graphics2D g2d = (Graphics2D)g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        /* Draw each line that is on this page.
         * Increment 'y' position by lineHeight for each line.
         */
        
        int y = lineHeight; 
        int start = (pageIndex == 0) ? 0 : pageBreaks[pageIndex-1];
        int end   = (pageIndex == pageBreaks.length)
                         ? textLines.length : pageBreaks[pageIndex];
        for (int line=start; line<end; line++) {
            y += lineHeight;
            g.drawString(textLines[line], 36, y);
        }

        /* tell the caller that this page is part of the printed document */
        return PAGE_EXISTS;
    }

    public void printText(String text ) {
       	 this.textToPrint = text;
       	 
         PrinterJob job = PrinterJob.getPrinterJob();
         job.setPrintable(this);
         boolean ok = job.printDialog();
         if (ok) {
             try {
                  job.print();
             } catch (PrinterException ex) {
            	 System.err.println(ex.getMessage());
            	 new Notify(lh.localValue("cannotPrint") + " " + job.getPrintService().getName()) ;
             }
         }
    }
}

