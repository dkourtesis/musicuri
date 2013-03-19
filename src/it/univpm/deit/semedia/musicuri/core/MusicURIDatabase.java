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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import com.thoughtworks.xstream.XStream;

/**
* @author Dimitrios Kourtesis
*/
public class MusicURIDatabase implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	/**
	 * A HashMap object containing pairs of an MD5 String as key and a MusicURIReference object as value
	 */
	private HashMap table;
	/**
	 * The path to where the serialized HashMap database is located
	 */
	private String databasePath;
	/**
	 * The database file name
	 */
	private String databaseFileName;
	
	
	
	
	/**
	 * Constructs a MusicURIDatabase object by deserializing the HashMap specified by the path
	 * @param databasePath the path to the directory containing the db
	 * @param databaseFileName the filename of the serialized hashmap
	 */
	public MusicURIDatabase(String databasePath, String databaseFileName)
	{
		table = new HashMap();
		this.databasePath = databasePath;
		this.databaseFileName = databaseFileName;
		
		try
		{
			table = (HashMap) deserialize(databasePath + databaseFileName);
			//System.out.println(table.size() + " references loaded from " + databaseFileName);
		} 
		catch (ClassNotFoundException e)
		{
			e.toString();
			//System.out.println("Can't load database file (" + databasePath + databaseFileName + ")");
		}
		catch (InvalidClassException e)
		{
			e.toString();
			//System.out.println("Can't load database file (" + databasePath + databaseFileName + ")");
		}
		catch (IOException e)
		{
			e.toString();
			//System.out.println("Can't load database file (" + databasePath + databaseFileName + ")");
		}	
		catch (Exception e)
		{
			e.toString();
			//System.out.println("Can't load database file (" + databasePath + databaseFileName + ")");
		}
	}
	
	/**
	 * Creates and adds to the DB a MusicURIReference object for every audio file contained in the directory specified
	 * @param directoryPath the path to the directory containing the audio files to be added to the DB
	 */
	public void indexAudioFilesInDirectory(String directoryPath)
	{
		File path = new File(directoryPath);
		File[] list = path.listFiles();
		if (list.length == 0)
		{
			return;
		}
		else
		{
			for (int i = 0; i < list.length; i++)
			{
				File file = list[i];
				try
				{
					if (Toolset.isSupportedAudioFile(file)) 
						addMusicURIReference(new MusicURIReference(file));
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Adds the given MusicURIReference object to the DB, and also serializes it to xml
	 * @param newReference the MusicURIReference object to add to the DB
	 */
	public boolean addMusicURIReference(MusicURIReference newReference)
	{
		
		String md5 = newReference.getOriginalAudioFileMD5(); // MD5 used askey
		String filename = md5 + ".xml";
		File serializedObject = new File(databasePath + filename);
		
		// check if MD5 is already in use, and if the object serialized to xml exists on disk
		if (table.containsKey(md5)) 
		{
			//System.out.println("This MD5 is already in use");
			//if (serializedObject.exists()) System.out.println("XML file exists");
			//else System.out.println("XML file does not exist in DB directory");
			return false;
		}
		else
		{
			//although not required, also serialize to xml for easy debugging
			serializeToXmlFile(newReference, databasePath + filename);
			//add newReference to list with MD5 String as key, MusicURIReference object as value
			table.put(md5, newReference); 
			saveDb();
			return true;
		}
	}
	
	/**
	 * Serializes the HashMap table containing all references, saving its state
	 */
	public void saveDb()
	{
		serialize(table, databasePath + databaseFileName);
		//System.out.println(table.keySet().size() + " references saved in " + databaseFileName);
	}
	
	/**
	 * Removes a MusicURIReference object from the DB
	 * @param md5 the MD5 key of the MusicURIReference object to remove
	 */
	public boolean removeMusicURIReference (String md5)
	{
		if (!table.containsKey(md5)) // check if MD5 key exists in db
		{
			//System.out.println ("This MD5 key is not registered in the DB");
			return false;
		}
		else 
		{
			//remove MD5 and associated MusicURIReference from db
			table.remove(md5); 
			saveDb();
			return true;
		}
		
	}
	
	/**
	 * Fetches a MusicURIReference object from the DB
	 * @param md5 the MD5 key of the MusicURIReference object to fetch
	 * @return the MusicURIReference that was fetched, if it exists, null otherwise
	 */
	public MusicURIReference getMusicURIReference(String md5)
	{
		if (!table.containsKey(md5)) // check if MD5 key exists in db
		{
			//System.out.println ("This MD5 key is not registered in the DB");
			return null;
		}
		else 
		{
			// get associated MusicURIReference
			return (MusicURIReference)table.get(md5);
		}
	}
	
	/**
	 * Fetches the HashMap object that comprises the DB as a set view (object removals supported)
	 * @return a set view of the keys contained in the HashMap
	 */
	public Set getSetOfMusicURIReferences()
	{
		try
		{
			//HashMap tableClone = (HashMap) Cloner.cloneObject(table);
			//return tableClone.keySet();
			return table.keySet();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns a String containing a formatted list of all the audio files that have 
	 * been indexed as references in the DB (lists their titles and URIs).
	 */
	public String textFormattedSetOfMusicURIReferences()
	{
		String text = "\nReference audio files indexed in the DB, and their assigned URIs:\n\n";
		
		Set set = table.entrySet();
		Iterator it = set.iterator();
		int index = 1;
		while (it.hasNext())
		{
			Map.Entry e = (Map.Entry)it.next();
			text = text.concat("["+ index + "]" + "\tTitle : " + ((MusicURIReference)e.getValue()).getLabel() + "\n");
			text = text.concat("\tURI   : " + ((MusicURIReference)e.getValue()).getMusicUri() + "\n\n");
			index ++;
		}
		return text;
	}
	
	/**
	 * Gets the number of entries in the HashMap object that comprises the DB 
	 * @return the size of the DB
	 */
	public int getDbSize()
	{
		return table.size();
	}
	
	/**
	 * Serializes any object to the filename specified 
	 * @param object the object to be serialized
	 * @param fileName the filename of the serialized object
	 */
	public void serialize(Object object,String fileName) 
	{
		try 
		{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
			out.writeObject(object);
			out.flush();
		} 
		catch (IOException e) 
		{
			System.err.println("Unable to serialize object: " + e.toString());
		}
	}
	
	/**
	 * Deserializes any object from the filename specified 
	 * @param filePath the filename of the serialized object
	 * @return the object that was deserialized
	 */
	public Object deserialize(String filePath) throws IOException, ClassNotFoundException 
	{
		Object object = new Object();
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath));
		object = in.readObject();
		return object;
	}
	
	/**
	 * Deserializes a HashMap object from the filename specified 
	 * @param filePath the filename of the serialized HashMap
	 * @return the HashMap object that was deserialized
	 */
	public HashMap deserializeHashMap(String filePath) throws IOException, ClassNotFoundException 
	{
		HashMap object = new HashMap();
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath));
		object = (HashMap) in.readObject();
		return object;
	}
	
	/**
	 * Serializes any object to an XML file with the filename specified 
	 * @param object the object to be serialized
	 * @param filename the XML filename of the serialized object
	 */
	public void serializeToXmlFile(Object object, String filename)
	{
		XStream xstream = new XStream();
		String xml = xstream.toXML(object);
		try
		{
			FileWriter fw = new FileWriter(filename);
			fw.write(xml);
			fw.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
//	/**
//	 * Adds an Mp7ACT object to the DB 
//	 * @param act the Mp7ACT object to be added
//	 * Experimental use only
//	 */
//	private void ACT2ReferenceConverter(Mp7ACT act) throws NoSuchAlgorithmException, IOException, URISyntaxException
//	{
//		String label = act.getLabel();
//		File mp3File = new File(label);
//		byte[] md5bytes = Toolset.createMD5Hash(mp3File);
//		String md5 = Toolset.toHexString(md5bytes);
//		ArrayList keywords = Toolset.ExtractKeywords(mp3File);
//		ArrayList metaphones = Toolset.GenerateMetaphones(keywords);
//		URI musicUri = new URI ("file", label, null);
//		
//		MusicURIReference newref = new MusicURIReference(act, md5, keywords, metaphones, musicUri, label); 
//		addMusicURIReference(newref);
//	}
	
	
	
}
