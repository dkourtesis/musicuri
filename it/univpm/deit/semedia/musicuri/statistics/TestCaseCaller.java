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

package it.univpm.deit.semedia.musicuri.statistics;

import it.univpm.deit.semedia.musicuri.core.MusicURISearch;

import java.sql.Date;
import java.text.SimpleDateFormat;


/**
 * Utility class used for various tests
 */
public class TestCaseCaller
{

	public static void main(String[] args) throws Exception
	{
		String[] argString = new String[2];


		SimpleDateFormat df = new SimpleDateFormat ("dd-MM-yyyy HH:mm:ss");
//		long time = System.currentTimeMillis();

		//known --high quality labelling
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
//		argString[0]="D:/NewBatch/Known/128kbps-FhG/TotallyAccurateLabelling"; argString[1]= "1"; MusicURISearch.main(argString); //combined distance final resort
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
//		argString[0]="D:/NewBatch/Known/64kbps-FhG/TotallyAccurateLabelling"; argString[1]= "1"; MusicURISearch.main(argString);
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
//		argString[0]="D:/NewBatch/Known/32kbps-FhG/TotallyAccurateLabelling"; argString[1]= "1"; MusicURISearch.main(argString);
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
//		
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
//		argString[0]="D:/NewBatch/Known/128kbps-FhG/TotallyAccurateLabelling"; argString[1]= "0"; MusicURISearch.main(argString);
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
//		argString[0]="D:/NewBatch/Known/64kbps-FhG/TotallyAccurateLabelling"; argString[1]= "0"; MusicURISearch.main(argString);
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
//		argString[0]="D:/NewBatch/Known/32kbps-FhG/TotallyAccurateLabelling"; argString[1]= "0"; MusicURISearch.main(argString);
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
				
		//known --mid quality labelling
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		argString[0]="D:/NewBatch/Known/128kbps-FhG/BarelyReadable"; argString[1]= "1"; MusicURISearch.main(argString); //combined distance final resort
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		argString[0]="D:/NewBatch/Known/64kbps-FhG/BarelyReadable"; argString[1]= "1"; MusicURISearch.main(argString);
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		argString[0]="D:/NewBatch/Known/32kbps-FhG/BarelyReadable"; argString[1]= "1"; MusicURISearch.main(argString);
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
//		argString[0]="D:/NewBatch/Known/128kbps-FhG/BarelyReadable"; argString[1]= "0"; MusicURISearch.main(argString);
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
//		argString[0]="D:/NewBatch/Known/64kbps-FhG/BarelyReadable"; argString[1]= "0"; MusicURISearch.main(argString);
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
//		argString[0]="D:/NewBatch/Known/32kbps-FhG/BarelyReadable"; argString[1]= "0"; MusicURISearch.main(argString);
//		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		
		
//		//unknown
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		argString[0]="D:/NewBatch/Unknown/128kbps"; argString[1]= "1"; MusicURISearch.main(argString);
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));	
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		argString[0]="D:/NewBatch/Unknown/64kbps"; argString[1]= "1"; MusicURISearch.main(argString);
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		argString[0]="D:/NewBatch/Unknown/32kbps"; argString[1]= "1"; MusicURISearch.main(argString);
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
			
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		argString[0]="D:/NewBatch/Unknown/128kbps"; argString[1]= "0"; MusicURISearch.main(argString);
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		argString[0]="D:/NewBatch/Unknown/64kbps"; argString[1]= "0"; MusicURISearch.main(argString);
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		argString[0]="D:/NewBatch/Unknown/32kbps"; argString[1]= "0"; MusicURISearch.main(argString);
		System.out.println ("\n"); System.out.println (df.format(new Date(System.currentTimeMillis())));
		

		
		
		


	}

}
