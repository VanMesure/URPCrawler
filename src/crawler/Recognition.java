package crawler;
import java.io.*;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class Recognition {
	public String execute(File imgFile) {
		
        ITesseract instance = new Tesseract();  // JNA Interface Mapping
        // ITesseract instance = new Tesseract1(); // JNA Direct Mapping
        instance.setLanguage("eng");
        
        try {
            String result = instance.doOCR(imgFile);
            return result;
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
            return "";
            		
        }
	}
}
