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

package it.univpm.deit.semedia.musicuri.utils.experimental;


import it.univpm.deit.database.datatypes.AudioLLDmeta;
import it.univpm.deit.database.datatypes.Mp7ACT;
import it.univpm.deit.semedia.musicuri.core.MusicURIDatabase;
import it.univpm.deit.semedia.musicuri.core.MusicURIReference;
import it.univpm.deit.semedia.musicuri.core.Toolset;


import java.util.Iterator;
import java.util.Set;


/**
 * Utility class used for calculating the average distance of one MusicURIReference against the rest in the db
 */
public class DbAverageDistanceCalculator
{
	static String databasePath = "D:/100ReferenceDB/";
	static String databaseFileName = "MusicURIReferences.db";
	static MusicURIDatabase db = new MusicURIDatabase(databasePath, databaseFileName);
	
	public static void main(String[] args)
	{
		
		double overallAverageDistance = 0;
		double overallDistanceSum = 0;
		
		//*****************************************************************************
		//************   Q U E R Y   D A T A   P R E P A R A T I O N   ****************
		//*****************************************************************************
		
		// gets the set of keys from the db hashmap
		Set queryIterator = db.getSetOfMusicURIReferences();
		
		for (Iterator iter = queryIterator.iterator(); iter.hasNext();)
		{
			// get the next md5 key
			String currentMD5 = (String) iter.next();
			MusicURIReference qqq = db.getMusicURIReference(currentMD5);
			
			Mp7ACT queryMp7 = qqq.getAudioCompactType();
			
			// if its null then there is some problem
			if (queryMp7 == null)
				System.out.println("Problem: queryMp7 is null");
			
			// read the required data from the AudioCompactType
			AudioLLDmeta queryMean = queryMp7.featureByName(Mp7ACT.FLATNESS, Mp7ACT.MEAN);
			AudioLLDmeta queryVariance = queryMp7.featureByName(Mp7ACT.FLATNESS, Mp7ACT.VARIANCE);
			
			// is audioSignatureType included in the act file?
			if (queryMean == null || queryVariance == null )
			{
				System.out.println("Problem: AudioSignatureType is not included in ACT or cannot be extracted from audio file. Aborting.");
			}
			
			int vectorSize = queryMean.vectorSize; // internal! stay out! read the matrix size instead
			float[][] queryMeanMatrix = queryMean.__rawVectors;
			float[][] queryVarianceMatrix = queryVariance.__rawVectors;
			
			// instantiate and initialize query data
			int QueryNumOfVectors = queryMeanMatrix.length; // ==number of vectors, seconds
			int QueryVectorDim = vectorSize; // ==number of dimensions, subbands
			double [] QueryMean = new double[QueryNumOfVectors * QueryVectorDim];
			double [] QueryVar = new double[QueryNumOfVectors * QueryVectorDim];
			
			// Copy the query data from the 2-d matrix of floats to a 1-d array of doubles
			QueryMean = Toolset.copyFloatMatrixToDoubleArray(queryMeanMatrix, vectorSize);
			QueryVar = Toolset.copyFloatMatrixToDoubleArray(queryVarianceMatrix, vectorSize);
			
			
			
			//*****************************************************************************
			//********************   D I S T A N C E    S E A R C H   *********************
			//*****************************************************************************
			
			// declare here, initialize inside the for loop
			int	RefNumOfVectors = 0; 	// number of vectors, seconds
			int RefVectorDim = 0; 		// number of subbands, dimensions
			double[] RefMean;
			double[] RefVar;
			
			// something big
			double distance = 0;
			
			// flag used to skip entering the for loop
			boolean skipThis = false;
			
			
			// a counter used for display purposes
			int counter = 1;
			
			
			//System.out.print("Searching for " + numOfClosestMatches + " closest matches among " + db.getDbSize() + " reference signatures ");
			
			double currentQueryDistanceSum = 0;
			double currentQueryAverageDistance = 0;
			int numberOfComparisonsMade = 0;

			
			// gets the set of keys from the db hashmap
			Set keys = db.getSetOfMusicURIReferences();
			// beam me up scotty
			for (Iterator iter2 = keys.iterator(); iter2.hasNext();)
			{
				// get the next md5 key
				String currentMD52 = (String) iter2.next();
				
				// retrieve the MusicURIReference object corrsponding to this key
				MusicURIReference currentReference = db.getMusicURIReference(currentMD52);
				
				// retrieve the mp7act encapsulated in the MusicURIReference object
				Mp7ACT mp7 = currentReference.getAudioCompactType();
				
				// if it's null it shouldn't be
				if (mp7 == null)
					System.out.println("Problem: No mpeg7 exists for given uri");
				
				//System.out.print("[" + counter + "] "+ mp7.getLabel());
				
				// read the required data from the ACT
				AudioLLDmeta refMean = mp7.featureByName(Mp7ACT.FLATNESS, Mp7ACT.MEAN);
				AudioLLDmeta refVariance = mp7.featureByName(Mp7ACT.FLATNESS, Mp7ACT.VARIANCE);
				
				// if any of these are null there was some problem when extracting them, so skip them
				if ((refMean == null) || (refVariance == null))
				{
					System.out.println("Skipping: problematic mpeg7 description!!! - "+mp7.getLabel()+")");
					skipThis = true;
				}
				
				//System.out.println("if all goes well...");
				// if all goes well...
				if (!skipThis)
				{
					//System.out.println(" does well...");
					// instantiate and initialize reference data
					float[][] refMeanMatrix = refMean.__rawVectors;
					float[][] refVarianceMatrix = refVariance.__rawVectors;
					RefNumOfVectors = refMeanMatrix.length; // number of vectors-seconds
					RefVectorDim = vectorSize; // number of subbands
					RefMean = new double[RefNumOfVectors * RefVectorDim];
					RefVar = new double[RefNumOfVectors * RefVectorDim];
					
					// Copy the reference data from the 2-d matrix of floats to a 1-d array of doubles
					RefMean = Toolset.copyFloatMatrixToDoubleArray(refMeanMatrix, vectorSize);
					RefVar = Toolset.copyFloatMatrixToDoubleArray(refVarianceMatrix, vectorSize);
					
					// get the distance from query to reference
					distance = Mpeg7XMAudioSignatureSearch.WeightedEuclidianDistance(RefMean, RefVar, RefNumOfVectors, QueryMean, QueryVar, QueryNumOfVectors, QueryVectorDim);
					//distance = Toolset.getAlternativeDistance(refMeanMatrix, refVarianceMatrix, queryMeanMatrix, queryVarianceMatrix, QueryVectorDim);
					
					
					// print every reference in loop
					//System.out.println(distance + " \t" + currentReference.getLabel()); 
					
					currentQueryDistanceSum += distance;
					//get the average distance
					
					numberOfComparisonsMade++;
				}
				counter++;
				skipThis = false;
			}// for referenceIterator
			
			currentQueryAverageDistance = currentQueryDistanceSum / numberOfComparisonsMade;
			//System.out.println("currentQueryDistanceSum    : " + currentQueryDistanceSum);
			//System.out.println("numberOfComparisonsMade    : " + numberOfComparisonsMade);
			System.out.println(counter + "\tcurrentQueryAverageDistance: " + currentQueryAverageDistance);
			
			overallDistanceSum += currentQueryAverageDistance;
		}//for queryIterator
		
		overallAverageDistance = overallDistanceSum / db.getDbSize();
		System.out.println("overallDistanceSum    : " + overallDistanceSum);
		System.out.println("db.getDbSize()    : " + db.getDbSize());
		System.out.println("overallAverageDistance: " + overallAverageDistance);
	}
	
}
