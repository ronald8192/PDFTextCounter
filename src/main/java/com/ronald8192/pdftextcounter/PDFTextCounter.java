package com.ronald8192.pdftextcounter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PDF Text Reader/Counter
 * 
 * @author ronald8192
 *
 */
public class PDFTextCounter {
	
	Logger log = LoggerFactory.getLogger(PDFTextCounter.class);

	static Options opts = new Options();
	static CommandLineParser clParser = new DefaultParser();
	static CommandLine cmd = null;

	static int startPage = 1;
	static String pdfFilesDir = "pdfTextCounter/pdfs/";
	static String pdfExtractTextDir = "pdfTextCounter/txt/";

	public static void main(String[] args) {
		opts.addOption("i", true, "input (pdf files) directory");
		opts.addOption("o", true, "output directory");
		opts.addOption("s", true, "PDF start page (start reading from)");
		
		try {
			cmd = clParser.parse(opts, args);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		if(cmd.hasOption('i')){
			pdfFilesDir = cmd.getOptionValue('i');
		}
		File f = new File(pdfFilesDir);
		if(!f.exists()) f.mkdirs();
		
		if(cmd.hasOption('o')){
			pdfExtractTextDir = cmd.getOptionValue('o');
		}
		f = new File(pdfExtractTextDir);
		if(!f.exists()) f.mkdirs();
			
		if(cmd.hasOption('s')){
			startPage = Integer.parseInt(cmd.getOptionValue('o'));
			if(startPage < 1){
				startPage = 1;
			}
		}
		
		new PDFTextCounter().extract().count();
	}
	
	public void count() {
		File dir = new File(pdfExtractTextDir);
		Map<String, Integer> wordMap = new HashMap<>();

		for (File txt : dir.listFiles()) {
			log.info("[Count] " + txt.getName());
			String line = "";
			try (BufferedReader br = new BufferedReader(new FileReader(txt));) {
				while ((line = br.readLine()) != null) {
					String[] words = line.toLowerCase().split(" ");
					for (int i = 0; i < words.length; i++) {
						words[i] = words[i].replaceAll("[^-a-z-]+", "");
						if (!words[i].equals("")) {
							if (wordMap.containsKey(words[i])) {
								int newVal = (int) wordMap.get(words[i]) + 1;
								wordMap.put(words[i], newVal);
							} else {
								wordMap.put(words[i], 1);
							}
						}

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Error on counting " + txt.getName());
				log.error(e.getMessage());
			}
		}

		// output file
		log.info("Output summary file...");
		try(
				FileWriter fileWritter = new FileWriter(pdfExtractTextDir + "summary.csv", true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		){
			bufferWritter.write("word,count\n");
			for (String key : wordMap.keySet()) {
				bufferWritter.write(key + "," + wordMap.get(key) + "\n");
			}
		}catch (IOException e){
			e.printStackTrace();
			log.error("Error on writing summary to disk");
			log.error(e.getMessage());
		}
	}

	public PDFTextCounter extract() {
		PDFTextStripper pdfStripper = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;

		File dir = new File(pdfFilesDir);
		
		int iter = 1;
		for (File file : dir.listFiles()) {
			if(!file.getName().toLowerCase().endsWith(".pdf") || file.isDirectory()) continue;
			
			log.info("[Extract] " + file.getName());
			try {
				PDFParser parser = new PDFParser(new FileInputStream(file));
				parser.parse();
				cosDoc = parser.getDocument();
				pdfStripper = new PDFTextStripper();
				pdDoc = new PDDocument(cosDoc);
				pdfStripper.setStartPage(startPage);
				// pdfStripper.setEndPage(5);
				String parsedText = pdfStripper.getText(pdDoc);
				log.trace(parsedText);
				
				cosDoc.close();

				FileWriter fileWritter = new FileWriter(pdfExtractTextDir + iter++ + ".txt", true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				bufferWritter.write(parsedText);
				bufferWritter.close();
			} catch (IOException e) {
				e.printStackTrace();
				log.error("Error on extract text from " + file.getName());
				log.error(e.getMessage());
			}
		}
		
		if(iter == 1){
			log.info("No PDF file found!");
			System.exit(0);
		}

		return this;
	}
}
