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
import java.io.Serializable;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import org.xml.sax.InputSource;

/**
* @author Dimitrios Kourtesis
*/
public class MusicURIReference implements Serializable
{
	
	/**
	 * An Audio Compact Type object as defined in the Mpeg7AudioDB library, 
	 * probably containing not only an AudioSignatureDS instance,
	 * but the whole mpeg7-audio descriptor suite
	 */
	private Mp7ACT audioCompactType;
	
	/**
	* The MD5 hash key of the audiofile that is identified as a "representative" physical 
	* resource containing a digitally encoded conceptual resource. For example, this could be 
	* the MD5 key of the first mp3 file containing Eric Clapton's "Layla", that has been presented 
	* to the system. Thereafter all digital audio files containing the same song will be collapsed 
	* onto the specific musicURIReference containing the MD5 key of the first, original file.
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
	 * The actual music URI that uniquely identifies the specific MusicURIReference, 
	 * and thus, the underlying conceptual resource, which is a song
	 */
	private URI musicUri;
	
	
	
	
	/**
	 * Constructs a MusicURIReference object by creating the given audio file's  
	 * MPEG-7 AudioCompactType, hashing its binary content to create an MD5 key, and 
	 * tokenizing its filename and/or ID3 tag to create a list of keywords and their 
	 * double metaphone equivalent terms.
	 * @param musicFile the path to the source audio file
	 */
	public MusicURIReference(File musicFile) throws Exception
	{
		File queryFile = musicFile.getCanonicalFile();
		
		//*****************   AUDIO COMPACT TYPE   *********************
		Mp7ACT myNewMpeg7 = new Mp7ACT();
		// get an intermediary XML stream (String) containing the encoder-generated mpeg7 description
		// extracted from the given audio file, and copy the audiosignature data to our ACT
		myNewMpeg7.fromXML(new InputSource(new StringReader(Toolset.createMPEG7Description(queryFile))));
		this.audioCompactType = myNewMpeg7;
		
		
		//**************************   MD-5   **************************
		// Hash the given audio file's binary contents to create the MD5 hash 		
		byte[] md = Toolset.createMD5Hash(queryFile);
		this.originalAudioFileMD5 = Toolset.toHexString(md);
		//System.out.println("hash: " + Toolset.toHexString(md));
		
		
		//**************************   KEYWORDS   **************************
		// Tokenize the audio file's name to create a list of keywords
		this.keywords = Toolset.ExtractKeywords(queryFile);
		//System.out.println("keywords size: " + keywords.size());
		//for (int i = 0; i < keywords.size(); i++) System.out.println(keywords.get(i).toString());
    	
    	
    	// ************************   METAPHONES   *************************
    	// Convert the elements of the keywords list to their metaphone equivalents,
    	// to enable fuzzy phonetic matching, and robustness against mispellings
    	this.metaphones = Toolset.GenerateMetaphones(keywords);
		//System.out.println("metaphones size: " + metaphones.size());
		//for (int i = 0; i < metaphones.size(); i++) System.out.println(metaphones.get(i).toString());
    	
    	
    	// ************************   MUSIC URI   **************************
    	musicUri = new URI("http://musicuri.org/" + originalAudioFileMD5);
    	//System.out.println("musicUri: " + musicUri.toString());
    	
    	
    	// **************************   LABEL   ****************************
    	label = queryFile.getName();
    	//System.out.println("label: " + label);
	}
	
	/**
	 * Constructs a MusicURIReference object by setting its private attributes to the ones given
	 * @param audioCompactType the Mp7ACT object extracted from the source audio file
	 * @param originalAudioFileMD5 the MD5 hash key of the audio file
	 * @param keywords the list of keywaords extracted from the source audio file
	 * @param metaphones the list of metaphone equivalent terms generated from the keywords
	 * @param musicUri the actual URI identifying this MusicURIReference
	 * @param label the label describing the source audio file
	 */
	MusicURIReference(Mp7ACT audioCompactType, 
					String originalAudioFileMD5, 
					ArrayList keywords, 
					ArrayList metaphones, 
					URI musicUri, 
					String label)
	{
		this.audioCompactType = audioCompactType;
		this.originalAudioFileMD5 = originalAudioFileMD5;
		this.keywords = keywords;
		this.metaphones = metaphones;
		this.musicUri = musicUri;
		this.label = label;
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
	
	/**
	 * Gets the URI this MusicURIReference has been assigned to
	 * @return musicUri the URI assigned to this reference
	 */
	public URI getMusicUri()
	{
		return musicUri;
	}

	/**
	 * Sets the URI this MusicURIReference should be assigned to
	 * @param uri the URI to be set
	 */
	public void setMusicUri(URI uri)
	{
		this.musicUri = uri;
	}
	
	/**
	 * Prints some of the private attribute's values on screen (Experimental use only)
	 */	
	public void printToScreen()
	{
		System.out.println("MD5: " + getOriginalAudioFileMD5() + ", Label: "+ getLabel() + ", musicuri: " + getMusicUri());
		
		ArrayList keys = getKeywords();
		ArrayList metas = getMetaphones();
		for (int i = 0; i < keys.size(); i++)
		{
			System.out.println("keyword: "+ keys.get(i) + " metaphone: " + metas.get(i)); 
		}
	}
	
	
}//end class
