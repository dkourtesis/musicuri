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


import it.univpm.deit.semedia.musicuri.core.MusicURIDatabase;
import it.univpm.deit.semedia.musicuri.core.MusicURIReference;
import it.univpm.deit.semedia.musicuri.core.Toolset;

import java.io.File;


/**
 * Utility class used for adding audio files to the db
 */
public class DbPopulator
{

	public static void main(String[] args) throws Exception
	{
		if ((args.length == 1) && (new File (args[0]).exists())) 
		{
			// get the file's canonical path
			File givenHandle = new File(args[0]);
			String queryAudioCanonicalPath = givenHandle.getCanonicalPath();
			System.out.println("New Reference Source: " + queryAudioCanonicalPath);
			
			// create reference db
			String databasePath = "D:/";
			//String databasePath = "C:/temp/";
			String databaseFileName = "MusicURIReferences.db";
			MusicURIDatabase db = new MusicURIDatabase(databasePath, databaseFileName);
			
			// for a naive mp3 format check
			final String MP3MASK = ".*\\." + "mp3";
			final String WAVMASK = ".*\\." + "wav";
			
			if (givenHandle.isDirectory())
			{
				File[] list = givenHandle.listFiles();
				if (list.length == 0)
				{
					System.out.println("no files in directory");
					return;
				}
				else
				{
					for (int i = 0; i < list.length; i++)
					{
						File file = list[i];
						try
						{
							if (file.getName().matches(MP3MASK) || file.getName().matches(WAVMASK))
							{
								byte[] bytes = Toolset.createMD5Hash(file);
								String md5 = Toolset.toHexString(bytes);
								System.out.println(md5 + " - " + file.getName());
								
//								if (md5.equalsIgnoreCase("d910f7ee9a7a0b23edeaaac20368eb1d"))
//								{
//									System.out.print("special add (d910f7ee9a7a0b23edeaaac20368eb1d) : " + file.getName());
//									MusicURIReference newref = new MusicURIReference(file);
//									db.addMusicURIReference(newref);
//								}

								File serializedMusicURIObject = new File(databasePath + md5 + ".xml");
								if (serializedMusicURIObject.exists())
								{
									System.out.print("the file " + databasePath + md5 + ".xml" + " exists on disk, ");
									if ((MusicURIReference) db.getMusicURIReference(md5)!=null)
										System.out.println("and in the db as well");
									else
										System.out.println("but not in the db");
								}
								else 
								{
									
									System.out.print("the file " + databasePath + md5 + ".xml" + " gets added ");
									MusicURIReference newref = new MusicURIReference(file);
									db.addMusicURIReference(newref);
									
								}
								
							}
						} 
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}
			if (givenHandle.isFile())
			{
				if (givenHandle.getName().matches(MP3MASK)|| givenHandle.getName().matches(WAVMASK))
				{
					MusicURIReference newref = new MusicURIReference(givenHandle);
					db.addMusicURIReference(newref);
				}
			}
				
		}
		else System.out.println("file or dir does not exist");
		if (args.length==0) System.out.println("no param");
		

	}

}
