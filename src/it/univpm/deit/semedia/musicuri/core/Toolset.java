/*
 Copyright (c) 2005, Dimitrios Kourtesis
 
 This file is part of MusicURI.
 
 MusicURI is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 MusicURI is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with MPEG7AudioEnc; see the file COPYING. If not, write to
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 MA  02111-1307 USA
 */

package it.univpm.deit.semedia.musicuri.core;


import it.univpm.deit.database.Mpeg7ACTDatabase;
import it.univpm.deit.database.Mpeg7ACTDatabase.DbExtractionConfiguration;
import it.univpm.deit.database.util.AudioFileToXML;
import it.univpm.deit.database.util.URIretriver;
import it.univpm.deit.semedia.musicuri.utils.experimental.Mpeg7XMAudioSignatureSearch;

//import it.univpm.deit.database.util.Utils;
//import it.univpm.deit.webservice.makeItEasy.ConfigMaker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.commons.codec.language.*;

import com.wcohen.secondstring.tokens.SimpleTokenizer;
import com.wcohen.secondstring.tokens.Token;

import de.crysandt.audio.AudioInFloat;
import de.crysandt.audio.AudioInFloatSampled;
import de.crysandt.audio.mpeg7audio.Config;
import de.crysandt.audio.mpeg7audio.ConfigDefault;
import de.crysandt.audio.mpeg7audio.Encoder;
import de.crysandt.audio.mpeg7audio.MP7MediaInformation;
import de.crysandt.audio.mpeg7audio.MP7Writer;

/**
* @author Dimitrios Kourtesis
*/
public class Toolset
{
	
	/**
	 * Calculates the euclidian distance between two multidimentional vectors, having same dimensions
	 * @param vector1 the first input vector as an array of floats
	 * @param vector2 the second input vector as an array of floats
	 * @param vectorDim the vector dimensions
	 * @return the distance between the two vectors
	 */
	private static double getVectorDistance(float[] vector1, float[] vector2, int vectorDim)
	{
		double sum = 0.0;
		double diff = 0.0;
		for (int i = 0; i < vectorDim; ++i) 
		{
			diff = vector1[i] - vector2[i];
			sum += Math.sqrt (diff * diff);
		}
		return sum;
	}
	
	/**
	 * Calculates the weighted euclidian distance between two multidimentional vectors, having same dimensions
	 * @param vector1 the first input vector as an array of floats
	 * @param vector2 the second input vector as an array of floats
	 * @param vectorDim the vector dimentions
	 * @param weightVector an extra vector containing the weights
	 * @return the distance between the two vectors
	 */
	private static float getWeightedVectorDistance(float[] vector1, float[] vector2, int vectorDim, double[] weightVector)
	{
		double sum = 0.0;
		double diff = 0.0;
		for (int i = 0; i < vectorDim; ++i) 
		{
			diff = (vector1[i] * weightVector[i]) - (vector2[i] * weightVector[i]);
			sum += diff * diff;
			//double v1 = vector1[i] * weightVector[i];
			//double v2 = vector2[i] * weightVector[i];
			//sum += (v1-v2)*(v1-v2);
		}
		return (float) Math.sqrt(sum);
	}
	
	/**
	 * Calculates the weighted or non-weighted euclidian distance between two AudioSignatureDS instances 
	 * @param refMeanMatrix the 2-dimentional array of floats containing the reference mean vectors
	 * @param refVarianceMatrix the 2-dimentional array of floats containing the reference variance vectors
	 * @param queryMeanMatrix the 2-dimentional array of floats containing the query mean vectors
	 * @param queryVarianceMatrix the 2-dimentional array of floats containing the query variance vectors
	 * @param vectorDim the vector dimentions
	 * @param usingWeightVectors a boolean flag to determine if weighting should be used
	 * @return the distance between the two AudioSignatureDS instances
	 */
	public static double getEuclidianDistance ( float[][] refMeanMatrix,
												float[][] refVarianceMatrix,
												float[][] queryMeanMatrix,
												float[][] queryVarianceMatrix,
												int vectorDim,
												boolean usingWeightVectors)
	{

		int numRefVec = refMeanMatrix.length;
		int numQueryVec = queryMeanMatrix.length;
		double distanceBetweenMeanVectors;
		double distanceBetweenVarianceVectors;
		double QuerySumDistance;
		
		double theoreticalMaximum = (vectorDim * Math.sqrt(1)) * numQueryVec;
		double smallestQuerySumDistanceFoundYet = theoreticalMaximum; //= 9999.999f;
		int closestSoundingPointInSong = 0;
		
		for(int i = 0; i < numRefVec-numQueryVec +1; i++) // eg 240-10+1 = 231 times
		{
			distanceBetweenMeanVectors = 0.0f;
			distanceBetweenVarianceVectors = 0.0f;
			QuerySumDistance = 0.0f;
			
			for(int j = 0; j < numQueryVec; j++) // eg 10 times for a small query, 224 times for a full song
			{
				if (usingWeightVectors)
				{
					distanceBetweenMeanVectors = getWeightedVectorDistance(queryMeanMatrix[j], refMeanMatrix[i+j], vectorDim, Mpeg7XMAudioSignatureSearch.meanWeight);
				}
				else
				{
					distanceBetweenMeanVectors = getVectorDistance(queryMeanMatrix[j], refMeanMatrix[i+j], vectorDim);
				}
				
				QuerySumDistance += distanceBetweenMeanVectors + distanceBetweenVarianceVectors;
			}
			
			//if (i == 0) smallestDistanceFoundYet = QueryVecDistance;
			if(QuerySumDistance < smallestQuerySumDistanceFoundYet) 
			{
				smallestQuerySumDistanceFoundYet = QuerySumDistance;
				closestSoundingPointInSong = i;
			}
			//System.out.println("smallestQuerySumDistanceFoundYet: " + smallestQuerySumDistanceFoundYet);
		}
		
		//System.out.println("Closest match found at second: " + closestSoundingPointInSong);
		return smallestQuerySumDistanceFoundYet;
	}
	
	/**
	 * Copies data from a 2-d matrix of floats to a 1-d array of doubles
	 * @param matrix the 2-dimentional array of floats to be flattened
	 * @param dimensions the vector dimentions
	 * @return result the 1-dimensional flattened array 
	 */
	public static double[] copyFloatMatrixToDoubleArray(float[][] matrix, int dimensions)
	{
		double[] result = new double [matrix.length * dimensions];
		
		for (int rows = 0; rows < matrix.length; rows++)
		{
			for (int cols = 0; cols < dimensions; cols++)
			{
				result[(rows * dimensions) + cols] = matrix[rows][cols];
			}
		}
		return result;
	}
	
	/**
	 * Adapter to method addToDatabase of class Mpeg7ACTDatabase. Useful for adding a 
	 * single file only, and not all the files in a folder (experimental use only).
	 * @param filePathToAdd the path of the audio file to add
	 * @param db the Mpeg7ACTDatabase object (db) to add the to
	 */
	public static void addSingleFileToDatabase(String filePathToAdd, Mpeg7ACTDatabase db)
	{
		ArrayList container = new ArrayList();
		File fileToAdd = new File(filePathToAdd);
		if (fileToAdd.isFile())
		{
			for (int j = 0; j < db.knownAudioFiles.length; j++)
			{
				if (fileToAdd.getName().matches(db.knownAudioFiles[j])) container.add(fileToAdd);
			}
		}
		//folderCollect(path, masks, container);
		URI[] result = new URI[container.size()];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = ((File) container.get(i)).toURI();
		}
		
		// to get here result must contain 1 element of type URI
		db.addToDatabase(result);
	}
	
	/**
	 * Maps an audio file to the URN:MD5 URI space, by hashing the content of an audio 
	 * file located at the given URI, eg the path to an audio file (experimental use only).
	 * @param audioFileURI the URI of the audio file to map
	 * @return the new URI in the URN:MD5 URI space
	 */
	public static URI generateMD5URI(URI audioFileURI) throws Exception
	{
		byte[] buff = new byte[65535];

		MessageDigest md5;
		try
		{
			md5 = MessageDigest.getInstance("MD5");
			InputStream audioFile = new BufferedInputStream(URIretriver.retrive(audioFileURI));
			int read;
			while ((read = audioFile.read(buff)) != -1)
			{
				md5.update(buff, 0, read);
			}
			return new URI("urn:md5:" + toBase64String(md5.digest()));
		} catch (NoSuchAlgorithmException e)
		{
			System.err.println("MD5 doesnt exist (?)");
		}
		return null;
	}
	
	/**
	 * Creates the MD5 hash key for a given file 
	 * @param file the binary file to hash
	 * @return a byte[] containing the MD5 hash key
	 */
    public static byte[] createMD5Hash(File file) throws NoSuchAlgorithmException, IOException
	{
		InputStream fis = new FileInputStream(file);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;
		do
		{
			numRead = fis.read(buffer);
			if (numRead > 0)
			{
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		fis.close();
		return complete.digest();
	}
    
	/**
	 * Determines the current working directory (taken from: 
	 * http://www.olegdulin.com/index.php/archives/2004/05/18/)
	 * @return the current working directory
	 */
	public static String getCWD()
	{
		// Get the "." file present in all directories..
		java.io.File f = new java.io.File(".");
		// Get the absolute path to the "." file..
		String cwd = f.getAbsolutePath();
		// Return the absolute path minus the "."..
		return cwd.substring(0, cwd.length() - 1);
	}
	
	/**
	 * Extracts/encodes the AudioSignatureDS for a given audio file
	 * @param file the audio file to encode 
	 * @return a string containing the whole XML-formatted MPEG-7 description document
	 */
	public static String createMPEG7Description(File file) throws IOException
	{
		if (isSupportedAudioFile(file))
		{
		System.out.println("Extracting Query Audio Signature");
		String xmlString = null;
		Config configuration = new ConfigDefault();
		configuration.enableAll(false);
		configuration.setValue("AudioSignature", "enable", true);
		configuration.setValue("AudioSignature", "decimation", 32);
		//System.out.println("File: " + file.getName());
				
		AudioInputStream ais = null;
		try 
		{
			ais = AudioSystem.getAudioInputStream(file);
			AudioFormat f = ais.getFormat();
			if(f.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) 
			{
				System.out.println("Converting Audio stream format");
				ais = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED,ais);
				f = ais.getFormat();
			}
			
			String workingDir = getCWD();
			String tempFilename = workingDir + "/temp.wav";
			AudioSystem.write( ais, AudioFileFormat.Type.WAVE, new File(tempFilename));
			
			File tmpFile = new File(tempFilename);
			AudioInFloatSampled audioin = new AudioInFloatSampled(tmpFile);
			
			String str = tmpFile.getCanonicalPath();
			String[] ar = {str};
			//xmlString = Encoder.fromWAVtoXML(ar);
			
			// gather information about audio file
			MP7MediaInformation media_info = new MP7MediaInformation();
			media_info.setFileSize(tmpFile.length());
			
			AudioFormat format = audioin.getSourceFormat();
			media_info.setSample( format.getSampleRate(), format.getSampleSizeInBits());
			media_info.setNumberOfChannels(audioin.isMono() ? 1 : 2);
			
			// create mpeg-7 writer
			MP7Writer mp7writer = new MP7Writer();
			mp7writer.setMediaInformation(media_info);
			
			// create encoder
			Encoder encoder = null;
			
			Config config = new ConfigDefault();
			config.enableAll(false);
			config.setValue("AudioSignature", "enable", true);
			config.setValue("AudioSignature", "decimation", 32);
			encoder = new Encoder(audioin.getSampleRate(), mp7writer, config);
			//encoder.addTimeElapsedListener(new Ticker(System.err));
			
			// copy audio signal from source to encoder
			long oldtime = System.currentTimeMillis();
			float[] audio;
			while ((audio = audioin.get()) != null) 
			{
				if (!audioin.isMono()) audio = AudioInFloat.getMono(audio);
				encoder.put(audio);
			}
			encoder.flush();
			System.out.println( "Extraction Time     : " + (System.currentTimeMillis() - oldtime) + " ms");
			
			// whole MPEG-7 description into a string
			xmlString = mp7writer.toString();
			//System.out.println( xmlString )

		} 
		catch (Exception e) 
		{
			e.printStackTrace(System.err);
		} 
		finally 
		{
			//ais.close();
		}
		
		return xmlString;
		}
		else
		{
			System.out.println( "Unsupported audio file format");
			return null;
		}
	}
	
	/**
	 * Extracts a list of keywords from an audio file (currently only using its filename),
	 * after ignoring certain words that are either among the 50 most common english words, 
	 * or are frequently occuring music-related terms commonly found in music filenames
	 * @param audiofile the audio file to extract keywords from 
	 * @return an aArrayList object containing the extracted keywordss
	 */
	public static ArrayList ExtractKeywords(File audiofile)
	{
		// Idea taken from: François Pachet and Damien Laigre, 
		//"A Naturalist Approach to Music File Name Analysis"
		// Delimiter Hypothesis, Constant Term Hypothesis, Canonical Representation
		// of Identifiers (so that different spellings of a given identifier yield the same representation)
					
		// list of words to return
		ArrayList keywords = new ArrayList();
		// list of words to ignore
		ArrayList ignored = new ArrayList();
		
		// The 50 most common words in the english language
		// http://esl.about.com/library/vocabulary/bl1000_list1.htm
		// http://www.world-english.org/english500.htm
		// http://www.duboislc.org/EducationWatch/First100Words.html
		
		ignored.add("the");		ignored.add("of");		ignored.add("and");		
		ignored.add("a");		ignored.add("to");		ignored.add("in");		
		ignored.add("is");		ignored.add("you");		ignored.add("that");		
		ignored.add("it");		ignored.add("he");		ignored.add("was");		
		ignored.add("for");		ignored.add("on");		ignored.add("are");		
		ignored.add("as");		ignored.add("with");	ignored.add("his");
		ignored.add("they");	ignored.add("i");		ignored.add("at");		
		ignored.add("be");		ignored.add("this");	ignored.add("have");		
		ignored.add("from");	ignored.add("or");		ignored.add("one");		
		ignored.add("had");		ignored.add("by");		ignored.add("word");		
		ignored.add("but");		ignored.add("not");		ignored.add("what");		
		ignored.add("all");		ignored.add("were");	ignored.add("we");		
		ignored.add("when");	ignored.add("your");	ignored.add("can");		
		ignored.add("said");	ignored.add("there");	ignored.add("use");		
		ignored.add("an");		ignored.add("each");	ignored.add("which");		
		ignored.add("she");		ignored.add("do");		ignored.add("how");		
		ignored.add("their");	ignored.add("if");
						
		// Some frequently occuring words related to music filenames
		ignored.add("mp3");		ignored.add("&");		ignored.add("featuring");
		ignored.add("+");		ignored.add("feat");	ignored.add("presenting");		
		ignored.add("pres");	ignored.add("live");	ignored.add("track");		
		ignored.add("album");	ignored.add("various");	ignored.add("artists");		
		ignored.add("artist");	ignored.add("va");		ignored.add("collection");		
		ignored.add("sampler");	ignored.add("mix");		ignored.add("complilation");		
		ignored.add("mixed");	ignored.add("remix");	ignored.add("remixed");		
		ignored.add("lp");		ignored.add("ep");		ignored.add("cd");		
		ignored.add("");
		
	
		// replace all contents of the ignored list with lowercase
		for (int i = 0; i < ignored.size(); i++)
		{
			String tmp = (String) ignored.get(i);
			ignored.remove(i);
			tmp = tmp.toLowerCase();
			ignored.add(tmp);
		}
		
		// the source used for token extraction is currently the filename 
		String name = audiofile.getName();
		
		// the delimiters to use when tokenizing
		StringTokenizer st = new StringTokenizer(name," ,._-~\"'`/()[]:{}+#\t\n\r");
		
		// loop over all tokens in string
		while (st.hasMoreTokens())
		{
			// get the next
			String token = st.nextToken();
			// convert it to lowercase
			token = token.toLowerCase();
			// if it's on the list of ignored keywords
		    if (!ignored.contains(token))
		    {
		    	// try to cast it to an integer
		    	try
		    	{
		    		// if it was an integer, do nothing
		    		Integer.parseInt(token);
		    	}
		    	catch (NumberFormatException e)
		    	{
		    		// if it wasn't, add it to the keywords list
			    	keywords.add(token);
		    	}
		    }
		}
		return keywords;
	}
	
	/**
	 * Genarates a list of terms that are the metaphone equivalents of the words in the given list.
	 * The terms are generated using the double metaphone phonetic maching algorithm (apache implementation)
	 * @param keywords an aArrayList object containing the keywords to generate metaphones for 
	 * @return an aArrayList object containing the generated metaphone equivalent terms
	 */
	public static ArrayList GenerateMetaphones (ArrayList keywords)
	{
		ArrayList metaphoneList = new ArrayList(keywords.size());
		DoubleMetaphone meta = new DoubleMetaphone();
		String tmp = null;
				
		for (int i = 0; i < keywords.size(); i++)
		{
			tmp = meta.encode((String)keywords.get(i));
			metaphoneList.add(tmp);
		}
		return metaphoneList;
	}
	
	/**
	 * Removes the first occurence of an integer within a String. It is used to 
	 * remove the integer test case identifier from filenames that are used for 
	 * testing and evaluation purposes in this project
	 * @param filename the filename to remove the identifier from
	 * @return a string containing the filename without an identifier
	 */
	public static String removeTestCaseIdentifier(String filename)
	{
		String filenameWithoutIdentifier;
		StringTokenizer tok = new StringTokenizer(filename," `~!@#$%^&*()_-+={}[]|\\:;\"'<>,.?/\t\n\r");
		int numOfTokensInString = tok.countTokens();		
		String[] tokens = new String[numOfTokensInString];
		int i = 0;
		while (tok.hasMoreTokens())
		{
			String token = tok.nextToken();	
			tokens[i] = token;
	    	i++;
		}
		
		try
		{
			Integer tmp = new Integer (tokens[0]);
			int testCaseId = tmp.intValue();
			String identifier = "";
			if (testCaseId >= 1 )
			{
				// add zero padding
				if (testCaseId >=0 && testCaseId <10 )identifier = "000" + testCaseId + " ";
				if (testCaseId >=10 && testCaseId < 100 )identifier = "00" + testCaseId + " ";
				if (testCaseId >=100 && testCaseId < 1000 )identifier = "0" + testCaseId + " ";
				if (testCaseId >1000) identifier = testCaseId + " ";
			}
			filenameWithoutIdentifier = filename.replaceFirst(identifier, "");
		}
		catch (NumberFormatException e)
		{
			// if the first token was not an integer identifier, return what you got as input
			filenameWithoutIdentifier = filename;
		}		
		return filenameWithoutIdentifier;
	}
	
	/**
	 * Gets the first occurence of an integer within a String. It is used to 
	 * retrieve the integer test case identifier from filenames that are used for 
	 * testing and evaluation purposes in this project
	 * @param filename the filename to remove the identifier from
	 * @return a string containing the filename without an identifier
	 */
	public static int getTestCaseIdentifier(String filename)
	{
		//TODO: currently returns any integer that can be cast. should return the first token
		StringTokenizer st = new StringTokenizer(filename," ,._-~\"'`/()[]:{}+#\t\n\r");
		int returnIdentifier = -1;

		// loop over all tokens in string
		while (st.hasMoreTokens())
		{
			// get the next
			String token = st.nextToken();
			// if it has 4 characters
		    if (token.length()==4)
		    {
		    	// try to cast it to an integer
		    	try
		    	{
		    		// if it can be cast to an integer, this is the test-case
		    		// identifier, assuming that no 4-digit long integer exists
		    		// in the names of any of the test-case mp3's parent folders
		    		returnIdentifier = Integer.parseInt(token);
		    	}
		    	catch (NumberFormatException e)
		    	{
		    		// if it can't, do nothing
		    		System.out.print("");
		    	}
		    }
		}
		
		return returnIdentifier;
	}
	
	/**
	 * Converts a byte array into a hex representation (copied from Mpeg7AudioDB project)
	 * @param b the data to convert
	 * @return a string with a hex representation of the given data
	 */
	public static String toHexString ( byte[] b )  
	{ 
		char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b',  'c', 'd', 'e', 'f'};
		StringBuffer sb = new StringBuffer( b.length * 2 ); 
		for ( int i=0 ; i<b.length ; i++ ) 
		{ 
			sb.append( hexChar [ ( b[ i] & 0xf0 ) >>> 4 ] ) ; 
			sb.append( hexChar [ b[ i] & 0x0f ] ) ; 
		} 
		return sb.toString() ; 
	}
	
	/**
	 * Converts a byte array into a base 64 representation (copied from Mpeg7AudioDB project)
	 * @param b the data to convert
	 * @return a string with a base 64 representation of the given data
	 */
	public static String toBase64String (byte[] b) 
	{
		return new sun.misc.BASE64Encoder().encode(b);
	}
	
	/**
	 * Reports if the filename of the given file ends with ".wav"
	 * @param file the file to check
	 * @return true if the filename ends with a ".wav" extension, false otherwise
	 */
	public static boolean isWAVFile(File file)
	{
		String fname = file.getName();
		String extension = fname.substring(fname.lastIndexOf('.') + 1);
		if (extension.equalsIgnoreCase("wav")) return true;
		else return false;
	}
	
	/**
	 * Reports if the filename of the given file ends with ".mp3"
	 * @param file the file to check
	 * @return true if the filename ends with a ".mp3" extension, false otherwise
	 */
	public static boolean isMP3File(File file)
	{
		String fname = file.getName();
		String extension = fname.substring(fname.lastIndexOf('.') + 1);
		if (extension.equalsIgnoreCase("mp3")) return true;
		else return false;
	}
	
	/**
	 * Reports if the given audio file is supported by the current configuration of the host system.
	 * Support for various audio file types depends on if the respective plugins have been installed."
	 * @param file the file to check
	 * @return true if the filename ends with a ".wav" or ".mp3"extension, false otherwise
	 */
	public static boolean isSupportedAudioFile(File file)
	{

//		if ( isWAVFile(file) || isMP3File(file) ) return true;
//		else return false;
		
		boolean supported = false;
		String fname = file.getName();
		String extension = fname.substring(fname.lastIndexOf('.') + 1);
		
		// get all audio formats supported by host system confifuration
		AudioFileFormat.Type[]	supportedAudioFileTypes = AudioSystem.getAudioFileTypes();
		for (int i = 0; i < supportedAudioFileTypes.length; i++)
		{
			if (extension.equalsIgnoreCase(supportedAudioFileTypes[i].getExtension())) return true;
		}
		return supported;
	}
	
	/**
	 * Reports if the filename of the given file ends with ".xml"
	 * @param file the file to check
	 * @return true if the filename ends with a ".xml" extension, false otherwise
	 */
	public static boolean isXMLFile(File file)
	{
		String fname = file.getName();
		String extension = fname.substring(fname.lastIndexOf('.') + 1);
		if (extension.equalsIgnoreCase("xml")) return true;
		else return false;
	}
	
	/**
	 * Reports if the filename of the given file ends with ".act"
	 * @param file the file to check
	 * @return true if the filename ends with a ".act" extension, false otherwise
	 */
	public static boolean isACTFile(File file)
	{
		String fname = file.getName();
		String extension = fname.substring(fname.lastIndexOf('.') + 1);
		if (extension.equalsIgnoreCase("act")) return true;
		else return false;
	}

}
