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

import it.univpm.deit.semedia.musicuri.core.MusicURIDatabase;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


import com.wcohen.secondstring.JaroWinkler;
import com.wcohen.secondstring.MongeElkan;
import com.wcohen.secondstring.StringDistance;
import com.wcohen.secondstring.StringWrapper;
import com.wcohen.secondstring.TFIDF;
import com.wcohen.secondstring.tokens.SimpleTokenizer;
import com.wcohen.secondstring.tokens.Token;

/**
 * Utility class used for various tests
 */
public class Test
{

	public static void main(String[] args)
	{
		
		String databasePath = "D:/1000ReferenceDB/";
		String databaseFileName = "MusicURIReferences.db";
		System.out.println("Loading Database : " + databasePath + databaseFileName);
		MusicURIDatabase db = new MusicURIDatabase(databasePath, databaseFileName);
		System.out.println("db contains: "+ db.getDbSize());

		System.out.println(db.textFormattedSetOfMusicURIReferences());

		
		//StringDistance dist = new JaroWinkler().compare("frederic", "fredrick"); 
//		MongeElkan m = new MongeElkan();
//		String exp = m.explainScore("frederic", "fredrick");
//		System.out.println(exp);
		
//		StringWrapper w1 = new StringWrapper("tribe+called");
//		StringWrapper w2 = new StringWrapper("tribe called");
//		double res = m.score(w1, w2);
//		System.out.println(res);
		
		
//		SimpleTokenizer tk = new SimpleTokenizer(false, true);
//		Token[] tokens = tk.tokenize("tribe+Called");
//		for (int i = 0; i < tokens.length; i++)
//		{
//			System.out.println(tokens[i].getValue());
//		}
		
//		TFIDF test = new TFIDF();
//		w1 = test.prepare("tribe+Called");
//		w2 = test.prepare("tribe Called d");
//		float tfidfDist = (float) test.score(w1, w2);
//		float unit = 1.0f;
//		System.out.println("Distance: " + (tfidfDist));
//		System.out.println("Reverse : " + (unit - tfidfDist));
//		

//		ResultRankingList ranking = new ResultRankingList(4);
//		System.out.println("lastNullIndex: " + ranking.lastNullIndex);
//		ranking.RankThis(new Result(2.1,"2.1"));
//		System.out.println("lastNullIndex: " + ranking.lastNullIndex);
//		ranking.printContents();
//		
//		ranking.RankThis(new Result(1.1,"1.1"));
//		System.out.println("lastNullIndex: " + ranking.lastNullIndex);
//		ranking.printContents();
//		
//		ranking.RankThis(new Result(3.1,"3.1"));
//		System.out.println("lastNullIndex: " + ranking.lastNullIndex);
//		ranking.printContents();
//		
//		ranking.RankThis(new Result(2.0,"2.0"));
//		System.out.println("lastNullIndex: " + ranking.lastNullIndex);
//		ranking.printContents();
//		
//		ranking.RankThis(new Result(0.9,"0.9"));
//		System.out.println("lastNullIndex: " + ranking.lastNullIndex);
//		ranking.printContents();
//		
//		ranking.RankThis(new Result(0.7,"0.7"));
//		System.out.println("lastNullIndex: " + ranking.lastNullIndex);
//		ranking.printContents();
//		
//		ranking.RankThis(new Result(3.7,"3.7"));
//		System.out.println("lastNullIndex: " + ranking.lastNullIndex);
//		ranking.printContents();
		
		
//		if (Toolset.getTestCaseIdentifier(currentReference.getLabel()) > 1035 )
//		{
//			int newId =0;
//			
//			if (Toolset.getTestCaseIdentifier(currentReference.getLabel()) < 1065)
//				newId = Toolset.getTestCaseIdentifier(currentReference.getLabel())-1;
//			else 
//				newId = Toolset.getTestCaseIdentifier(currentReference.getLabel())-2;
//			
//			db.removeMusicURIReference(currentMD5);
//			System.out.print("replacing :" + currentReference.getLabel() );
//			String oldFilenameWithoutIdentifier = Toolset.removeIdentifier(currentReference.getLabel());
//			//System.out.print("oldFilenameWithoutIdentifier :" + oldFilenameWithoutIdentifier );
//			String newLabel = (newId + " " + oldFilenameWithoutIdentifier);
//			currentReference.setLabel(newLabel);
//			db.addMusicURIReference(currentReference);
//			System.out.println(" with " + newLabel);
//			//String appendedNonSenseFilename = scrambledTokenFilename.concat("#" + randomInteger +" - " + GeorgeOrwell1984[randomInteger]);
//			
//		}
		
	    }

}
