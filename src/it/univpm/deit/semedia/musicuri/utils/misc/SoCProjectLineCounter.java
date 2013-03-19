package it.univpm.deit.semedia.musicuri.utils.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


/**
 * Utility class used for calculating the SoC (Summer of Code) LOC (Lines Of Code) in a java source code directory
 */
public class SoCProjectLineCounter
{
	public static void main(String[] args) throws IOException
	{
		File directory = new File(args[0]);
		File[] allFiles = directory.listFiles();
		File currentFile;
		int totalLinesOfCode = 0;
		
		for (int i=0; i< allFiles.length; i++)
		{
			currentFile = allFiles[i];
			if (isJavaFile(currentFile)) totalLinesOfCode += countLines (currentFile);
		}
		System.out.println("Total Lines Of Java Code in Directory: " + directory.getName() + " - " + totalLinesOfCode);
	}
	
	private static int countLines(File file) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(file));
		int line = 0;
		while(br.readLine() != null) line++;
		return line;
	}
	
	private static boolean isJavaFile(File file)
	{
		String fname = file.getName();
		String extension = fname.substring(fname.lastIndexOf('.') + 1);
		if (extension.equalsIgnoreCase("java")) return true;
		else return false;
	}
	
}
