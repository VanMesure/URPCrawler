package net.sourceforge.tess4j;

import java.io.File;
import net.sourceforge.tess4j.*;
public class Test {
	  public static void main(String[] args) {
	        // ImageIO.scanForPlugins(); // for server environment
	        File imageFile = new File("C:\\Users\\fan\\Desktop\\temp/a_bin.jpg");
	       // ITesseract instance = new Tesseract(); // JNA Interface Mapping
	        ITesseract instance = new Tesseract1(); // JNA Direct Mapping
	        // instance.setDatapath("<parentPath>"); // replace <parentPath> with path to parent directory of tessdata
	         instance.setLanguage("eng");
	         instance.setTessVariable("tessedit_char_whitelist", "0123456789zxcvbnmasdfghjklqwertyuiopZXCVBNMASDFGHJKLQWERTYUIOP");

	        try {
	            String result = instance.doOCR(imageFile);
	            System.out.println(result);
	        } catch (TesseractException e) {
	            System.err.println(e.getMessage());
	        }
	    }
}
