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


import it.univpm.deit.database.datatypes.Mp7ACT;
import it.univpm.deit.semedia.musicuri.core.Toolset;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
* @author Dimitrios Kourtesis
*/
public class MusicURIQuery
{
	
	/**
	 * An Audio Compact Type object as defined in the Mpeg7AudioDB library, 
	 * probably containing not only an AudioSignatureDS instance,
	 * but the whole mpeg7-audio descriptor suite
	 */
	private Mp7ACT audioCompactType;
	
	/**
	* The MD5 hash key of the original audiofile, produced by hashing
	* the actual binary file (and not the path to where it is located)
	*/
	private String originalAudioFileMD5;	

	/**
	 * List of keywords taken from the original audiofile's filename and/or id3 tag
	 * The keywords are currently extracted by tokenizing the filename
	 * Experimental use only
	 */
	private ArrayList keywords;
	
	/**
	 * List of terms that are the metaphone equivalents of each keyword in the keywords list
	 * The list of keywords was extracted from the original audiofile's filename and/or ID3 tag
	 * The terms are generated using the double metaphone equivalent algorithm 
	 * Experimental use only
	 */
	private ArrayList metaphones;
	
	/**
	 * An informative label describing a piece of music (eg could be a filename)
	 */
	private String label;
	
	
	
	
	
	/**
	 * Constructs a MusicURIQuery object setting all its private atributes to null
	 */
	public MusicURIQuery()
	{
		this.audioCompactType = null;
		this.originalAudioFileMD5 = null;
		this.keywords = null;
		this.metaphones = null;
		this.label = null;
	}
	
	/**
	 * Constructs a MusicURIQuery object by creating the given audio file's MPEG-7 
	 * AudioCompactType, hashing its binary content to create an MD5 key, and 
	 * tokenizing its filename and/or ID3 tag to create a list of keywords and their 
	 * double metaphone equivalent terms.
	 * @param musicFile the path to the source audio file
	 */
	public MusicURIQuery(File musicFile) throws SAXException, IOException, Exception
	{
		File queryFile = musicFile;//.getCanonicalFile();
		
		
		//*****************   AUDIO COMPACT TYPE   *********************
		//System.out.println("Creating Audio Compact Type");
		//Create an empty Mp7ACT object
		Mp7ACT myNewMpeg7 = new Mp7ACT();
		// get an intermediary XML stream (String) containing the encoder-generated mpeg7 description
		// extracted from the given audio file, and copy the audiosignature data to our ACT
		myNewMpeg7.fromXML(new InputSource(new StringReader(Toolset.createMPEG7Description(queryFile))));
		this.audioCompactType = myNewMpeg7;
		
		//URI queryFileURI = queryFile.toURI();
		//String pathFromURI = queryFileURI.getPath();
		//URI md5DataURI = Toolset.generateMD5URI(queryFileURI);
		// initialize the two private attributes of class Mp7ACT (describedResource=uri describedFilePath=label)
		// describedResource :urn:md5:yVh5jeZOwRwwLqUUwEXUtQ==
		// describedFilePath: /C:/musicURI/queries/0001 A Tribe Called Quest - Can I Kick It_10sec.mp3
		//Mp7ACT myNewMpeg7 = new Mp7ACT(md5DataURI, pathFromURI);
		// get an intermediary XML stream (String) containing the encoder-generated mpeg7 description 
		// (from the audio file specified in the URI), and copy the audiosignature data to our ACT
		//myNewMpeg7.fromXML(new InputSource(new StringReader(Toolset.retriveMP7(queryFileURI))));
		//this.audioCompactType = myNewMpeg7;
		
		
		//**************************   MD-5   **************************
		//System.out.println("Creating MD5 hash key");
		// Hash the given audio file's binary contents to create the MD5 hash 		
		byte[] md = Toolset.createMD5Hash(queryFile);
		this.originalAudioFileMD5 = Toolset.toHexString(md);
		//System.out.println("hash: " + Utils.toHexString(md));
		
		
		//**************************   KEYWORDS   **************************
		//System.out.println("Extracting list of keywords");
		// Tokenize the audio file's name to create a list of keywords
		this.keywords = Toolset.ExtractKeywords(queryFile);
		//System.out.println("keywords size: " + keywords.size());
    	//for (int i = 0; i < keywords.size(); i++) System.out.println(keywords.get(i).toString());
    	
		
    	// ************************   METAPHONES   *************************
		//System.out.println("Extracting keywords' metaphone equivalents");
    	// Convert the elements of the keywords list to their metaphone equivalents,
    	// to enable fuzzy phonetic matching, and robustness against mispellings
    	this.metaphones = Toolset.GenerateMetaphones(keywords);
    	//System.out.println("metaphones size: " + metaphones.size());
    	//for (int i = 0; i < metaphones.size(); i++) System.out.println(metaphones.get(i).toString());
		
    	
    	// **************************   LABEL   ****************************
    	//System.out.println("Extracting label");
    	this.label = queryFile.getName();
    	//System.out.println("label: " + label);
	}
	
	/**
	 * Gets the Mp7ACT object that is encapsulated in this MusicURIQuery object
	 * @return audioCompactType the Mp7ACT object
	 */
	public Mp7ACT getAudioCompactType()
	{
		return audioCompactType;
	}
	
	/**
	 * Sets the Mp7ACT object that is encapsulated in this MusicURIQuery object to the one specified
	 * @param audioCompactType the Mp7ACT object to be set
	 */
	public void setAudioCompactType(Mp7ACT audioCompactType)
	{
		this.audioCompactType = audioCompactType;
	}
	
	/**
	 * Gets the MD5 key of the original audio file that this MusicURIQuery object was created from
	 * @return originalAudioFileMD5 the MD5 hash key
	 */
	public String getOriginalAudioFileMD5()
	{
		return originalAudioFileMD5;
	}
	
	/**
	 * Sets the MD5 key of the original audio file that this MusicURIQuery corresponds to
	 * @param originalAudioFileMD5 the MD5 hash key to set
	 */
	public void setOriginalAudioFileMD5(String originalAudioFileMD5)
	{
		this.originalAudioFileMD5 = originalAudioFileMD5;
	}
	
	/**
	 * Gets the list of keywords in this MusicURIQuery object
	 * @return keywords the list of keywords
	 */
	public ArrayList getKeywords()
	{
		return keywords;
	}
	
	/**
	 * Sets the list of keywords in this MusicURIQuery object
	 * @return keywords the list of keywords to set
	 */
	public void setKeywords(ArrayList keywords)
	{
		this.keywords = keywords;
	}
	
	/**
	 * Gets the list of metaphone equivalent terms that correspond to the keywords contained in the keywords list
	 * @return metaphones the list of metaphones
	 */
	public ArrayList getMetaphones()
	{
		return metaphones;
	}
	
	/**
	 * Sets the list of metaphone equivalent terms that correspond to the keywords contained in the keywords list
	 * @param metaphones the list of metaphones to set
	 */ 
	public void setMetaphones(ArrayList metaphones)
	{
		this.metaphones = metaphones;
	}
	
	/**
	 * Gets the label describing the audio file this MusicURIQuery has been created from
	 * @return label the label describing the original audio file
	 */
	public String getLabel()
	{
		return label;
	}
	
	/**
	 * Sets the label describing the audio file this MusicURIQuery has been created from
	 * @param label the label to be set, describing the original audio file 
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}
	
	
}//end class
