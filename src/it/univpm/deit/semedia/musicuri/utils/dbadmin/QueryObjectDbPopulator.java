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

package it.univpm.deit.semedia.musicuri.utils.dbadmin;


import it.univpm.deit.semedia.musicuri.core.MusicURIQuery;
import it.univpm.deit.semedia.musicuri.core.Toolset;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.thoughtworks.xstream.XStream;


/**
 * Utility class used for experimenting with object serialization
 */
public class QueryObjectDbPopulator
{
	
	public static void main(String[] args) throws Exception
	{
		//System.out.println("Input: " + args[0]);
		
		if ((args.length == 1) && (new File (args[0]).exists())) 
		{
			// get the file's canonical path
			File givenHandle = new File(args[0]);
			String queryAudioCanonicalPath = givenHandle.getCanonicalPath();
			System.out.println("New Query Source: " + queryAudioCanonicalPath);
			
			// for a naive mp3 format check
			final String MP3MASK = ".*\\." + "mp3";
			File[] list = null;
			
			if (givenHandle.isFile())
			{
				list = new File[1];
				list[0] = givenHandle;
			}
			if (givenHandle.isDirectory())
			{
				list = givenHandle.listFiles();
				
				if (list.length == 0)
				{
					System.out.println("No files in directory");
					return;
				}
			}
			
			MusicURIQuery newQuery;
			File mp3File;
			String parentDirectory;
			
			for (int i = 0; i < list.length; i++)
			{
				mp3File = list[i];
				parentDirectory = mp3File.getParent();
				try
				{
					if (mp3File.getName().matches(MP3MASK))
					{
						byte[] bytes = Toolset.createMD5Hash(mp3File);
						String md5 = Toolset.toHexString(bytes);
						
						if ((new File(parentDirectory + "/" + md5 + ".xml")).exists())
						{
							System.out.println("File : " + md5 + ".xml" + " already exists on disk");
						}
						else
						{
							System.out.print("MusicURIQuery for '" + mp3File.getName() + "' gets serialized to '" + md5 + ".xml' -- ");
							newQuery = new MusicURIQuery(mp3File);
							serializeToXmlFile(newQuery, parentDirectory + "/" + md5 + ".xml");
						}
					}
					else
					{
						if (mp3File.isDirectory())
						{
							String[] recursiveArgs = {mp3File.getCanonicalPath()};
							QueryObjectDbPopulator.main(recursiveArgs);
						}
					}
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}			
		}
		else System.out.println("file or dir does not exist");
		if (args.length==0) System.out.println("no input given");
		
	}
	
	public static void serializeToXmlFile(Object object, String filename)
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
	
}
