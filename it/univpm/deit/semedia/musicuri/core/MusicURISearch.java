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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import com.thoughtworks.xstream.XStream;
import com.wcohen.secondstring.JaroWinkler;
import com.wcohen.secondstring.StringWrapper;
import it.univpm.deit.database.datatypes.AudioLLDmeta;
import it.univpm.deit.database.datatypes.Mp7ACT;
import it.univpm.deit.semedia.musicuri.statistics.PerformanceStatistic;
import it.univpm.deit.semedia.musicuri.statistics.Stopwatch;

/**
* @author Dimitrios Kourtesis
*/
public class MusicURISearch
{
	/**
	 * The MusicURIDatabase instance serving as the db the search engine draws upon
	 */
	static private MusicURIDatabase db;
	
	private int numberOfComparisonsMade;
	private int totalReferenceSeconds;
	private long pruningTime;
	private long searchTime;
	
	
	
	
	/**
	 * Constructs a MusicURISearch engine and assigns its db attibute to the MusicURIDatabase instance given
	 */
	public MusicURISearch (MusicURIDatabase externalDB)
	{
		//System.out.println("Loading the externally assigned Database");
		db = externalDB;
	}
	
	/**
	 * Constructs a MusicURISearch engine and assigns its db attibute to a new MusicURIDatabase instance 
	 */
	public MusicURISearch (String databasePath, String databaseFileName)
	{
		//System.out.println("Loading the Database");
		db = new MusicURIDatabase(databasePath, databaseFileName);
	}
	
		/**
	 * Performs a search in the MusicURIDatabase and returns a ranked list of Results corresponding to the 
	 * MusicURIReference objects that most closely matched the given MusicURIQuery object
	 * @param query the MusicURIQuery object to be compared agaist all MusicURIReference objects
	 * @param usingPruningHeuristic the boolean flag to determine if the pruning heuristic should be used
	 * @param usingCombinedDistance the boolean flag to determine if the distance metric should be a combined labelling + audio signature metric
	 * @param maximumThreshold the maximum distance threshold above which a match is concidered unacceptable
	 * @param finalResortIsCombinedDistance the boolean flag to determine if search should resort to a combined distancemetric in the final identification attempt
	 * @return a ResultRankingList containing a sorted ranking list of the best Results objects encountered during search
	 */
	public ResultRankingList identify(MusicURIQuery query,
												boolean usingPruningHeuristic,
												boolean usingCombinedDistance,
												float maximumThreshold,
												boolean finalResortIsCombinedDistance) //throws Exception
	{
		double lambda = 0.5;
		boolean usingKeywordHeuristic = false;
		boolean usingMetaphoneHeuristic = false;
		boolean runningAtVerboseMode = false;
		
//		System.out.println("Method identify() called with maximumThreshold " + maximumThreshold + " and set to : ");
//		if ( usingPruningHeuristic && !usingCombinedDistance) System.out.println(" Using text-based pruning, and audio signature matching");
//		if ( usingPruningHeuristic &&  usingCombinedDistance) System.out.println(" Using text-based pruning, and combined metrics matching");
//		if (!usingPruningHeuristic && !usingCombinedDistance) System.out.println(" Using exhaustive search, and audio signature matching");
//		if (!usingPruningHeuristic &&  usingCombinedDistance) System.out.println(" Using exhaustive search, and combined metrics matching");
//		if (finalResortIsCombinedDistance) System.out.println(" final resort is combined distance");
//		else System.out.println(" Final resort is audio signature distance");
		
		//*****************************************************************************
		//************   Q U E R Y   D A T A   P R E P A R A T I O N   ****************
		//*****************************************************************************
		
		// get the act object encapsulated in the MusicURIQuery object
		Mp7ACT queryMp7 = query.getAudioCompactType();
		
		// if its null then there is some problem
		if (queryMp7 == null)
			System.out.println("Problem: queryMp7 is null");
		
		// read the required data from the AudioCompactType
		AudioLLDmeta queryMean = queryMp7.featureByName(Mp7ACT.FLATNESS, Mp7ACT.MEAN);
		AudioLLDmeta queryVariance = queryMp7.featureByName(Mp7ACT.FLATNESS, Mp7ACT.VARIANCE);
		
		// are audioSignatureType data included in the act file?
		if (queryMean == null || queryVariance == null )
		{
			System.out.println("Problem: AudioSignatureType is not included in ACT or cannot be extracted from audio file. Aborting.");
			return null;
		}
		
		int vectorSize = queryMean.vectorSize; // internal! stay out! read the matrix size instead
		float[][] queryMeanMatrix = queryMean.__rawVectors;
		float[][] queryVarianceMatrix = queryVariance.__rawVectors;
		
		// instantiate and initialize query data
		int QueryNumOfVectors = queryMeanMatrix.length; // ==number of vectors, seconds
		int QueryVectorDim = vectorSize; // ==number of dimensions, subbands
//		double [] QueryMean = new double[QueryNumOfVectors * QueryVectorDim];
//		double [] QueryVar = new double[QueryNumOfVectors * QueryVectorDim];
		
		// Copy the query data from the 2-d matrix of floats to a 1-d array of doubles
//		QueryMean = Toolset.copyFloatMatrixToDoubleArray(queryMeanMatrix, vectorSize);
//		QueryVar = Toolset.copyFloatMatrixToDoubleArray(queryVarianceMatrix, vectorSize);
		
		ArrayList QueryMetaphones = query.getMetaphones();
		ArrayList QueryKeywords = query.getKeywords();
		
		
		
	
		//*****************************************************************************
		//*********   R E F E R E N C E   D A T A   P R E P A R A T I O N   ***********
		//*****************************************************************************
		
		// declare here, initialize inside the for loop
		int	RefNumOfVectors = 0; 	// number of vectors, seconds
		int RefVectorDim = 0; 		// number of subbands, dimensions
//		double[] RefMean;
//		double[] RefVar;
		// something big
		double finalDistance;// = 9999.999;
		// flag used to skip entering the for loop
		boolean skipThis = false;
		// flag used to determine if an update in the ranking list has been made
		boolean dirty = false;
		// a counter used for display purposes
		int counter = 1;
		// counters used for statistics
		int numberOfComparisonsMade = 0;
		int totalReferenceSeconds = 0;
		

		// gets the set of keys from the db hashmap
		Set allMusicURIReferenceKeys = db.getSetOfMusicURIReferences();
		String queryLabelling = query.getLabel();
		
		// the ranking lists
		ResultRankingList labelRankingList = null;
		ResultRankingList signatureRankingList = null;
		ArrayList goodKeys = null;
		

		double currentLabelDistance = 0.0;
		double currentSignatureDistance = 0.0;
		double normalizedSignatureDistance = 0.0;
		double normalizedLabelDistance = 0.0;
		double score = 0.0;
		
		int numOfClosestMatchesInArray = 0;
		Result theBestResult = null;
		Result theSecondBestResult = null;
		Result theWorstResultYet = null;
		Result theNewResult = null;
		Mp7ACT mp7;
		boolean goodCandidate = false;
		ArrayList keywords;
		String currentMD5;
		MusicURIReference currentReference;
		String currentKeyword;
		ArrayList metaphones;
		String currentMetaphone;
		AudioLLDmeta refMean;
		AudioLLDmeta refVariance;
		float[][] refMeanMatrix;
		float[][] refVarianceMatrix;
		Result tmpResult;
		ResultRankingList finalDistanceRankingList = null;
		JaroWinkler test = null;
		StringWrapper queryWrapper = null;
		StringWrapper refWrapper = null;
		float editDistance;
		
		
		long pruningStartTime;
		long pruningStopTime;
		long pruningTime = 0;
		
		
		if (usingPruningHeuristic)
		{	
			pruningStartTime = System.currentTimeMillis();
			Stopwatch stopwatch = new Stopwatch();
			stopwatch.start();
			labelRankingList = pruneDatabase(allMusicURIReferenceKeys, queryLabelling);
			goodKeys = labelRankingList.getRankListMd5Keys();
			stopwatch.stop();
			System.out.println("Pruning completed in: " + stopwatch);
			pruningStopTime = System.currentTimeMillis();
			pruningTime = pruningStopTime - pruningStartTime;
			
			finalDistanceRankingList = new ResultRankingList(labelRankingList.getSize());
			signatureRankingList = new ResultRankingList(labelRankingList.getSize());
		}
		else
		{
			test = new JaroWinkler();
			queryLabelling = Toolset.removeTestCaseIdentifier(queryLabelling);
			queryWrapper = test.prepare(queryLabelling);
			editDistance = 777.777f;
			
			finalDistanceRankingList = new ResultRankingList(db.getDbSize());
			signatureRankingList = new ResultRankingList(db.getDbSize());
		}
		
		// start the monitors
		long startTimeMillis = System.currentTimeMillis();
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.start();
		
		// beam me up scotty
		for (Iterator iter = allMusicURIReferenceKeys.iterator(); iter.hasNext();)
		{
			// get the next md5 key
			currentMD5 = (String) iter.next();
			
			// retrieve the MusicURIReference object corresponding to this key
			currentReference = db.getMusicURIReference(currentMD5);
				
			// retrieve the mp7act encapsulated in the MusicURIReference object
			mp7 = currentReference.getAudioCompactType();
			
			// if it's null it shouldn't be
			if (mp7 == null)
				System.out.println("Problem: No mpeg7 exists for given uri");
			
			// read the required data from the ACT
			refMean = mp7.featureByName(Mp7ACT.FLATNESS, Mp7ACT.MEAN);
			refVariance = mp7.featureByName(Mp7ACT.FLATNESS, Mp7ACT.VARIANCE);
			
			// if any of these are null there was some problem when extracting them, so skip them
			if ((refMean == null) || (refVariance == null))
			{
				System.out.println("Skipping: problematic mpeg7 description!!! - "+mp7.getLabel()+")");
				skipThis = true;
			}

			//*****************************************************************************
			//************************   H E U R I S T I C S   ****************************
			//*****************************************************************************
			
			if (usingPruningHeuristic)
			{
				if (!goodKeys.contains(currentMD5)) skipThis = true;
			}
			
			if (usingKeywordHeuristic)
			{
				keywords = currentReference.getKeywords();
				currentKeyword = "";
				
				if (QueryKeywords.size()==0)
				{
					//System.out.println("Query keywords list is empty");
					goodCandidate = true;
				}
				
				if (keywords.size()==0)
				{
					//System.out.println("Reference keywords list is empty");
					goodCandidate = true;
				}
				
				for (int i = 0; i < keywords.size(); i++)
				{
					currentKeyword = (String) keywords.get(i);
					if (QueryKeywords.contains(currentKeyword))
					{
						goodCandidate = true;
						//System.out.println("QueryKeywords.contains: " + currentKeyword);
					}
				}
				if (!goodCandidate) skipThis = true;
			}
			
			if (usingMetaphoneHeuristic)
			{
				metaphones = currentReference.getMetaphones();
				currentMetaphone = "";
				
				if (QueryMetaphones.size()==0)
				{
					//System.out.println("Query metaphones list is empty");
					goodCandidate = true;
				}
				
				if (metaphones.size()==0)
				{
					//System.out.println("Reference metaphones list is empty");
					goodCandidate = true;
				}
				
				for (int i = 0; i < metaphones.size(); i++)
				{
					currentMetaphone = (String) metaphones.get(i);
					if (QueryMetaphones.contains(currentMetaphone))
					{
						goodCandidate = true;
						//System.out.println("QueryMetaphones.contains: " + currentMetaphone);
					}
				}
				if (!goodCandidate) skipThis = true;
			}
			
			
			
			//*****************************************************************************
			//********************   D I S T A N C E    S E A R C H   *********************
			//*****************************************************************************
			if (!skipThis)
			{
				
				// instantiate and initialize reference data
				refMeanMatrix = refMean.__rawVectors;
				refVarianceMatrix = refVariance.__rawVectors;
				RefNumOfVectors = refMeanMatrix.length; // number of vectors-seconds
				
				RefVectorDim = vectorSize; // number of subbands
				
				// Copy the reference data from the 2-d matrix of floats to a 1-d array of doubles & get the distance from query to reference
				//RefMean = new double[RefNumOfVectors * RefVectorDim];
				//RefVar = new double[RefNumOfVectors * RefVectorDim];
				//RefMean = Toolset.copyFloatMatrixToDoubleArray(refMeanMatrix, vectorSize);
				//RefVar = Toolset.copyFloatMatrixToDoubleArray(refVarianceMatrix, vectorSize);
				//distance = Toolset.getWeightedEuclidianDistance(RefMean, RefVar, RefNumOfVectors, QueryMean, QueryVar, QueryNumOfVectors, QueryVectorDim);
				
				currentSignatureDistance = Toolset.getEuclidianDistance(refMeanMatrix, refVarianceMatrix, queryMeanMatrix, queryVarianceMatrix, QueryVectorDim, false);
				double theoreticalMaximum = (RefVectorDim * Math.sqrt(1)) * queryMeanMatrix.length;
				normalizedSignatureDistance = currentSignatureDistance / theoreticalMaximum; //eg (16 * sqrootof(1) ) * 10 --to scale at 0-1
				signatureRankingList.RankThis(new Result(normalizedSignatureDistance, currentMD5));
				
				float labelRankingPosition; 
				float labelRankingListSize;
				float rankingHint;
				
				if (usingPruningHeuristic) //ie using the ranking list produced during pruning
				{
					currentLabelDistance = labelRankingList.getResultDistance(currentMD5); //0-1
					labelRankingPosition = (float) labelRankingList.getRankingPositionOf(currentMD5);
					labelRankingListSize = (float) labelRankingList.getSize();
					rankingHint = labelRankingPosition / labelRankingListSize; //eg 13/115 = 0.113
				}
				else //no ranking list exists (no pruning took place to create it)
				{
					String refname = currentReference.getLabel();
					refname = Toolset.removeTestCaseIdentifier(refname);
					refWrapper = test.prepare(refname);
					editDistance = 1 - (float) test.score(queryWrapper, refWrapper);
					currentLabelDistance = editDistance;
					
					rankingHint = 0;
				}
				
				if (usingCombinedDistance) //using the linear metric combination
				{
					//finalDistance = (0.5 * currentLabelDistance) + (0.5 * normalizedSignatureDistance);
					//finalDistance = lambda * currentLabelDistance + (1-lambda) * normalizedSignatureDistance;
					//finalDistance = currentLabelDistance * normalizedSignatureDistance;
					//finalDistance = currentLabelDistance + normalizedSignatureDistance;
					finalDistance = currentLabelDistance + normalizedSignatureDistance + rankingHint;
					theNewResult = new Result (finalDistance, currentMD5);
					finalDistanceRankingList.RankThis(theNewResult);
				}
				else //not using the linear metric combination, but only audio signature
				{
					finalDistance = normalizedSignatureDistance;
					theNewResult = new Result (finalDistance, currentMD5);
					finalDistanceRankingList.RankThis(theNewResult);
				}
				
				numberOfComparisonsMade++;
				score = 100 - (100 * finalDistance);
				
				if (runningAtVerboseMode)
				{
					//print every reference in loop
					System.out.print(counter);
					System.out.println("\tReference               : " + currentReference.getLabel());
					System.out.println("\tLabel Distance          : " + currentLabelDistance);
					System.out.println("\tSignature Distance      : " + currentSignatureDistance);
					System.out.println("\tNorm Signature Distance : " + normalizedSignatureDistance);
					System.out.println("\tFinal Distance          : " + finalDistance);
					System.out.println("\tScore                   : " + score + " %\n");
				}
			}
			counter++;
			skipThis = false;
			totalReferenceSeconds += RefNumOfVectors;
			
		}//endfor every key in keyset

		
		// stop the monitors
		long stopTimeMillis = System.currentTimeMillis();
		long searchTime = stopTimeMillis - startTimeMillis;
		stopwatch.stop();
		System.out.print("Search completed in : " + stopwatch);
		
		if (usingKeywordHeuristic) System.out.println(" (Using the keyword heuristic)");
		if (usingMetaphoneHeuristic) System.out.println(" (Using the metaphone heuristic)");
		if (!usingKeywordHeuristic && !usingMetaphoneHeuristic)
		{
			if ( usingPruningHeuristic && !usingCombinedDistance) System.out.println(" (Using text-based pruning, and audio signature matching)");
			if ( usingPruningHeuristic &&  usingCombinedDistance) System.out.println(" (Using text-based pruning, and combined metrics matching)");
			if (!usingPruningHeuristic && !usingCombinedDistance) System.out.println(" (Using exhaustive search, and audio signature matching)");
			if (!usingPruningHeuristic &&  usingCombinedDistance) System.out.println(" (Using exhaustive search, and combined metrics matching)");
		}
		
		//System.out.println("Monitor: " + mon.toString());
		
		
		return finalDistanceRankingList;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Calls the identify() method to perform a search in the MusicURIDatabase and returns a 
	 * PerformanceStatistic object that can be used to extract useful statistics about the system's performance
	 * @param query the MusicURIQuery object to be compared agaist all MusicURIReference objects
	 * @param usingPruningHeuristic the boolean flag to determine if the pruning heuristic should be used
	 * @param usingCombinedDistance the boolean flag to determine if the distance metric should be a combined labelling + audio signature metric
	 * @param maximumThreshold the maximum distance threshold above which a match is concidered unacceptable
	 * @param finalResortIsCombinedDistance the boolean flag to determine if search should resort to a combined distancemetric in the final identification attempt
	 * @return a PerformanceStatistic containing aggregated performance stats on speed and accuracy
	 */
	public PerformanceStatistic getIdentificationPerformance (MusicURIQuery query,
																	boolean usingPruningHeuristic,
																	boolean usingCombinedDistance,
																	float maximumThreshold,
																	boolean finalResortIsCombinedDistance) throws Exception
		{
		
		PerformanceStatistic tempStat = null;
		ResultRankingList finalDistanceRankingList = null;
		Result theBestResult = null;
		Result theSecondBestResult = null;
		Result theWorstResultYet = null;
//		float maximumThreshold = 0.09f;
//		boolean finalResortIsCombinedDistance = true;
//		boolean usingPruningHeuristic = true;
		try
		{

			finalDistanceRankingList = identify(query, usingPruningHeuristic, usingCombinedDistance, maximumThreshold, finalResortIsCombinedDistance);
			
			if (finalDistanceRankingList.getSize() >= 2)
			{
				
				theBestResult = finalDistanceRankingList.getResultAtIndex(0);
				theSecondBestResult = finalDistanceRankingList.getResultAtIndex(1);
			}

			
			//String queryName = query.getLabel();
			String queryName = "";
			int queryIdentifier = Toolset.getTestCaseIdentifier(queryName);		
			// Print ranking list results
			
			// if not a single result exists (which means that no comparison has been made,
			// which means that you probably have a text-matching enabled but no query tokens
			// match any of the reference tokens in any of the references) execution shouldn't 
			// go through the following block of code
			//if (theBestResult != null)
			if (theBestResult != null && theBestResult.distance <= maximumThreshold) //the system made some match
			{
				
				//*****************************************************************************
				//*************************   S T A T I S T I C S   ***************************
				//*****************************************************************************
				
				
				double bestMatchDistance = theBestResult.distance;
				double secondBestMatchDistance = theSecondBestResult.distance;
				String bestMatchName = db.getMusicURIReference(theBestResult.md5).getLabel();
				
				int indexOfLastResult = finalDistanceRankingList.getSize()-1; //index strarts at 0
				theWorstResultYet = finalDistanceRankingList.getResultAtIndex(indexOfLastResult);
				
				double worstMatchDistance = theWorstResultYet.distance;
				
				//System.out.println("\n\nQuery               : "	+ query.getLabel());
				System.out.println("Matched with        : "	+ (db.getMusicURIReference(theBestResult.md5)).getLabel());
				System.out.println("Distance            : " + theBestResult.distance);
				System.out.println("Score               : " + (float) (100 - (100*(theBestResult.distance))) + "%"); 
				
				System.out.println("Second best match   : "	+ (db.getMusicURIReference(theSecondBestResult.md5)).getLabel());
				System.out.println("Distance            : " + theSecondBestResult.distance);
				System.out.println("Score               : " + (float) (100 - (100*(theSecondBestResult.distance))) + "%"); 
				
				
				
				int referenceIdentifier = Toolset.getTestCaseIdentifier(bestMatchName);
				int identificationValidity = 0;
				//1: TP correctly matched
				//2: FP falsely matched, the correct match is some other known song
				//3: TN correctly unmatched, the song is indeed unknown
				//4: FN falsely unmatched, the song is known
				
				// id < 9000 means the song is registered with the db
				if (queryIdentifier < 9000) 
				{
					if (queryIdentifier == referenceIdentifier) identificationValidity = 1; //true positive
					if (queryIdentifier != referenceIdentifier) 
					{
						identificationValidity = 2; //false positive
						ArrayList hack = getResultByTestCaseIdentifier(finalDistanceRankingList, queryIdentifier);
						
						if (hack != null)
						{
							Integer actualPosition = (Integer) hack.get(0);
							Result actualResult = (Result) hack.get(1);
							int pos = actualPosition.intValue() + 1; 
							System.out.println("Actual match was    : " + db.getMusicURIReference(actualResult.md5).getLabel());
							System.out.println("Actual distance was : " + actualResult.distance);
							System.out.println("Found at position   : " + pos);
							System.out.println("Score               : " + (100 - (100 * (actualResult.distance))));
						}
					}
				}
				else // id > 9000 means the song is not registered with the db
				{
					if (queryIdentifier != referenceIdentifier) identificationValidity = 2; //false positive
				}
				
				
				return new PerformanceStatistic(db.getDbSize(),
												numberOfComparisonsMade,
												totalReferenceSeconds,
												pruningTime,
												searchTime,
												identificationValidity,
												bestMatchDistance,
												secondBestMatchDistance,
												worstMatchDistance);
			}
			else //its null or yields larger distance than maximum allowed threshold --no results
			{
				// if not a single result exists (no comparison has been made), or the distance of the 
				// closest match is unacceptably big, the text-based pruning might be responsible.
				// therefore recursivelly call identify() with pruning turned off
				
				
				if (usingPruningHeuristic) 
				{
					usingPruningHeuristic = false;
					
					if (theBestResult == null)
						System.out.println("                    : No comparison has been made, now trying exhaustive search with pruning turned off");
					else
						System.out.println("                    : No match at a distance below " + maximumThreshold + ". Now trying exhaustive search with pruning turned off");
					if (finalResortIsCombinedDistance)
					{
						return getIdentificationPerformance (query, usingPruningHeuristic, true, maximumThreshold, finalResortIsCombinedDistance);
					}
					else 
						return getIdentificationPerformance (query, usingPruningHeuristic, false, maximumThreshold, finalResortIsCombinedDistance);
				}
				else //this is the end
				{
					System.out.println("                    : Search completed without finding any match at an acceptable distance (" + maximumThreshold +")");
					
					double bestMatchDistance = theBestResult.distance;
					double secondBestMatchDistance = theSecondBestResult.distance;
					String bestMatchName = db.getMusicURIReference(theBestResult.md5).getLabel();
					int indexOfLastResult = finalDistanceRankingList.getSize()-1; //index strarts at 0
					theWorstResultYet = finalDistanceRankingList.getResultAtIndex(indexOfLastResult);
					double worstMatchDistance = theWorstResultYet.distance;
					
					//System.out.println("\n\nQuery               : "	+ query.getLabel());
					System.out.println("Matched with        : "	+ (db.getMusicURIReference(theBestResult.md5)).getLabel());
					System.out.println("Distance            : " + theBestResult.distance);
					System.out.println("Score               : " + (float) (100 - (100*(theBestResult.distance))) + "%"); 
					
					System.out.println("Second best match   : "	+ (db.getMusicURIReference(theSecondBestResult.md5)).getLabel());
					System.out.println("Distance            : " + theSecondBestResult.distance);
					System.out.println("Score               : " + (float) (100 - (100*(theSecondBestResult.distance))) + "%"); 
					
					int referenceIdentifier = Toolset.getTestCaseIdentifier(bestMatchName);
					int identificationValidity = 0;
					//1: TP correctly matched
					//2: FP falsely matched, the correct match is some other known song
					//3: TN correctly unmatched, the song is indeed unknown
					//4: FN falsely unmatched, the song is known
					
					// id < 9000 means the song is registered with the db
					if (queryIdentifier < 9000) 
					{
						if (queryIdentifier == referenceIdentifier) identificationValidity = 1; //true positive
						if (queryIdentifier != referenceIdentifier) 
						{
							identificationValidity = 2; //false positive
							ArrayList hack = getResultByTestCaseIdentifier(finalDistanceRankingList, queryIdentifier);
							
							if (hack != null)
							{
								Integer actualPosition = (Integer) hack.get(0);
								Result actualResult = (Result) hack.get(1);
								int pos = actualPosition.intValue() + 1; 
								System.out.println("Actual match was    : " + db.getMusicURIReference(actualResult.md5).getLabel());
								System.out.println("Actual distance was : " + actualResult.distance);
								System.out.println("Found at position   : " + pos);
								System.out.println("Score               : " + (100 - (100 * (actualResult.distance))));
							}
						}
					}
					else // id > 9000 means the song is not registered with the db
					{
						if (queryIdentifier != referenceIdentifier) identificationValidity = 2; //false positive
					}
					
					return new PerformanceStatistic(db.getDbSize(),
													numberOfComparisonsMade,
													totalReferenceSeconds,
													pruningTime,
													searchTime,
													identificationValidity,
													bestMatchDistance,
													secondBestMatchDistance,
													worstMatchDistance);
					
				}
				

			}//end else
		} 
		catch (Exception e)
		{
		}
		return tempStat;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Performs a search in the MusicURIDatabase specified, and returns the MD5 key of the 
	 * MusicURIReference object that most closely matches the given query data
	 * @param queryMeanMatrix the 2-d array of floats containing the mean vectors of the query's AudioSignatureDS instantiation
	 * @param queryVarianceMatrix the 2-d array of floats containing the variance vectors of the query's AudioSignatureDS instantiation
	 * @param QueryVectorDim the number of dimensions that the mean and variance query vectors have
	 * @param queryLabelling the informative label describing the auery audio file (currently it is the filename)
	 * @param usingPruningHeuristic the boolean flag to determine if the pruning heuristic should be used
	 * @param usingCombinedDistance the boolean flag to determine if the distance metric should be a combined labelling + audio signature metric
	 * @param maximumThreshold the maximum distance threshold above which a match is concidered unacceptable
	 * @param finalResortIsCombinedDistance the boolean flag to determine if search should resort to a combined distancemetric in the final identification attempt
	 * @return a String containing the MD5 hash key of the closest matching MusicURIReference
	 */
	public String identifyFromWebInput( MusicURIDatabase database,
												float[][] queryMeanMatrix,
												float[][] queryVarianceMatrix,
												int QueryVectorDim,
												String queryLabelling,
												boolean usingPruningHeuristic,
												boolean usingCombinedDistance,
												float maximumThreshold,
												boolean finalResortIsCombinedDistance) throws Exception
	{
		double lambda = 0.5;
		boolean usingKeywordHeuristic = false;
		boolean usingMetaphoneHeuristic = false;
		boolean runningAtVerboseMode = false;
		
		db = database;
		//*****************************************************************************
		//************   Q U E R Y   D A T A   P R E P A R A T I O N   ****************
		//*****************************************************************************
		ArrayList QueryMetaphones = null;
		ArrayList QueryKeywords = null;
		
		//*****************************************************************************
		//*********   R E F E R E N C E   D A T A   P R E P A R A T I O N   ***********
		//*****************************************************************************
		
		// declare here, initialize inside the for loop
		int	RefNumOfVectors = 0; 	// number of vectors, seconds
		int RefVectorDim = 0; 		// number of subbands, dimensions
//		double[] RefMean;
//		double[] RefVar;
		// something big
		double finalDistance;// = 9999.999;
		// flag used to skip entering the for loop
		boolean skipThis = false;
		// flag used to determine if an update in the ranking list has been made
		boolean dirty = false;
		// a counter used for display purposes
		int counter = 1;
		// counters used for statistics
		int numberOfComparisonsMade = 0;
		int totalReferenceSeconds = 0;
			

				
		// gets the set of keys from the db hashmap
		Set allMusicURIReferenceKeys = db.getSetOfMusicURIReferences();
		
		// the ranking lists
		ResultRankingList labelRankingList = null;
		ResultRankingList signatureRankingList = null;
		ArrayList goodKeys = null;
		
		double currentLabelDistance = 0.0;
		double currentSignatureDistance = 0.0;
		double normalizedSignatureDistance = 0.0;
		double normalizedLabelDistance = 0.0;
		double score = 0.0;
		
		int numOfClosestMatchesInArray = 0;
		Result theBestResult = null;
		Result theSecondBestResult = null;
		Result theWorstResultYet = null;
		Result theNewResult = null;
		Mp7ACT mp7;
		boolean goodCandidate = false;
		ArrayList keywords;
		String currentMD5;
		MusicURIReference currentReference;
		String currentKeyword;
		ArrayList metaphones;
		String currentMetaphone;
		AudioLLDmeta refMean;
		AudioLLDmeta refVariance;
		float[][] refMeanMatrix;
		float[][] refVarianceMatrix;
		Result tmpResult;
		ResultRankingList finalDistanceRankingList = null;
		JaroWinkler test = null;
		StringWrapper queryWrapper = null;
		StringWrapper refWrapper = null;
		float editDistance;
		
		long pruningStartTime;
		long pruningStopTime;
		long pruningTime = 0;
		
		if (usingPruningHeuristic)
		{	
			pruningStartTime = System.currentTimeMillis();
			Stopwatch stopwatch = new Stopwatch();
			stopwatch.start();
			labelRankingList = pruneDatabase(allMusicURIReferenceKeys, queryLabelling);
			goodKeys = labelRankingList.getRankListMd5Keys();
			stopwatch.stop();
			System.out.println("Pruning completed in: " + stopwatch);
			pruningStopTime = System.currentTimeMillis();
			pruningTime = pruningStopTime - pruningStartTime;
			
			finalDistanceRankingList = new ResultRankingList(labelRankingList.getSize());
			signatureRankingList = new ResultRankingList(labelRankingList.getSize());
		}
		else
		{
			test = new JaroWinkler();
			queryLabelling = Toolset.removeTestCaseIdentifier(queryLabelling);
			queryWrapper = test.prepare(queryLabelling);
			editDistance = 777.777f;
			
			finalDistanceRankingList = new ResultRankingList(db.getDbSize());
			signatureRankingList = new ResultRankingList(db.getDbSize());
		}
		
		
		// start the monitors
		long startTimeMillis = System.currentTimeMillis();
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.start();
		
		// beam me up scotty
		for (Iterator iter = allMusicURIReferenceKeys.iterator(); iter.hasNext();)
		{
			// get the next md5 key
			currentMD5 = (String) iter.next();
			
			// retrieve the MusicURIReference object corresponding to this key
			currentReference = db.getMusicURIReference(currentMD5);
				
			// retrieve the mp7act encapsulated in the MusicURIReference object
			mp7 = currentReference.getAudioCompactType();
			
			// if it's null it shouldn't be
			if (mp7 == null)
				System.out.println("Problem: No mpeg7 exists for given uri");
			
			// read the required data from the ACT
			refMean = mp7.featureByName(Mp7ACT.FLATNESS, Mp7ACT.MEAN);
			refVariance = mp7.featureByName(Mp7ACT.FLATNESS, Mp7ACT.VARIANCE);
			
			// if any of these are null there was some problem when extracting them, so skip them
			if ((refMean == null) || (refVariance == null))
			{
				System.out.println("Skipping: problematic mpeg7 description!!! - "+mp7.getLabel()+")");
				skipThis = true;
			}
			
			//*****************************************************************************
			//************************   H E U R I S T I C S   ****************************
			//*****************************************************************************
			
			if (usingPruningHeuristic)
			{
				if (!goodKeys.contains(currentMD5)) skipThis = true;
			}
			
			if (usingKeywordHeuristic)
			{
				keywords = currentReference.getKeywords();
				currentKeyword = "";
				
				if (QueryKeywords.size()==0)
				{
					//System.out.println("Query keywords list is empty");
					goodCandidate = true;
				}
				
				if (keywords.size()==0)
				{
					//System.out.println("Reference keywords list is empty");
					goodCandidate = true;
				}
				
				for (int i = 0; i < keywords.size(); i++)
				{
					currentKeyword = (String) keywords.get(i);
					if (QueryKeywords.contains(currentKeyword))
					{
						goodCandidate = true;
						//System.out.println("QueryKeywords.contains: " + currentKeyword);
					}
				}
				if (!goodCandidate) skipThis = true;
			}
			
			if (usingMetaphoneHeuristic)
			{
				metaphones = currentReference.getMetaphones();
				currentMetaphone = "";
				
				if (QueryMetaphones.size()==0)
				{
					//System.out.println("Query metaphones list is empty");
					goodCandidate = true;
				}
				
				if (metaphones.size()==0)
				{
					//System.out.println("Reference metaphones list is empty");
					goodCandidate = true;
				}
				
				for (int i = 0; i < metaphones.size(); i++)
				{
					currentMetaphone = (String) metaphones.get(i);
					if (QueryMetaphones.contains(currentMetaphone))
					{
						goodCandidate = true;
						//System.out.println("QueryMetaphones.contains: " + currentMetaphone);
					}
				}
				if (!goodCandidate) skipThis = true;
			}
			
			
			
			//*****************************************************************************
			//********************   D I S T A N C E    S E A R C H   *********************
			//*****************************************************************************
			if (!skipThis)
			{
				
				// instantiate and initialize reference data
				refMeanMatrix = refMean.__rawVectors;
				refVarianceMatrix = refVariance.__rawVectors;
				RefNumOfVectors = refMeanMatrix.length; // number of vectors-seconds
				
				// TODO: assuming number of subbands in query is equal to reference
				RefVectorDim = QueryVectorDim; // number of subbands
				
				// Copy the reference data from the 2-d matrix of floats to a 1-d array of doubles & get the distance from query to reference
				//RefMean = new double[RefNumOfVectors * RefVectorDim];
				//RefVar = new double[RefNumOfVectors * RefVectorDim];
				//RefMean = Toolset.copyFloatMatrixToDoubleArray(refMeanMatrix, vectorSize);
				//RefVar = Toolset.copyFloatMatrixToDoubleArray(refVarianceMatrix, vectorSize);
				//distance = Toolset.getWeightedEuclidianDistance(RefMean, RefVar, RefNumOfVectors, QueryMean, QueryVar, QueryNumOfVectors, QueryVectorDim);
				
				currentSignatureDistance = Toolset.getEuclidianDistance(refMeanMatrix, refVarianceMatrix, queryMeanMatrix, queryVarianceMatrix, QueryVectorDim, false);
				double theoreticalMaximum = (RefVectorDim * Math.sqrt(1)) * queryMeanMatrix.length;
				normalizedSignatureDistance = currentSignatureDistance / theoreticalMaximum; //eg (16 * sqrootof(1) ) * 10 --to scale at 0-1
				signatureRankingList.RankThis(new Result(normalizedSignatureDistance, currentMD5));
				
				float labelRankingPosition; 
				float labelRankingListSize;
				float rankingHint;
				
				if (usingPruningHeuristic) //ie using the ranking list produced during pruning
				{
					currentLabelDistance = labelRankingList.getResultDistance(currentMD5); //0-1
					labelRankingPosition = (float) labelRankingList.getRankingPositionOf(currentMD5);
					labelRankingListSize = (float) labelRankingList.getSize();
					rankingHint = labelRankingPosition / labelRankingListSize; //eg 13/115 = 0.113
				}
				else //no ranking list exists (no pruning took place to create it)
				{
					String refname = currentReference.getLabel();
					refname = Toolset.removeTestCaseIdentifier(refname);
					refWrapper = test.prepare(refname);
					editDistance = 1 - (float) test.score(queryWrapper, refWrapper);
					currentLabelDistance = editDistance;
					
					rankingHint = 0;
				}
				
				if (usingCombinedDistance) //using the linear metric combination
				{
					//finalDistance = (0.5 * currentLabelDistance) + (0.5 * normalizedSignatureDistance);
					//finalDistance = lambda * currentLabelDistance + (1-lambda) * normalizedSignatureDistance;
					//finalDistance = currentLabelDistance * normalizedSignatureDistance;
					//finalDistance = currentLabelDistance + normalizedSignatureDistance;
					finalDistance = currentLabelDistance + normalizedSignatureDistance + rankingHint;
					theNewResult = new Result (finalDistance, currentMD5);
					finalDistanceRankingList.RankThis(theNewResult);
				}
				else //not using the linear metric combination, but only audio signature
				{
					finalDistance = normalizedSignatureDistance;
					theNewResult = new Result (finalDistance, currentMD5);
					finalDistanceRankingList.RankThis(theNewResult);
				}
				
				numberOfComparisonsMade++;
				score = 100 - (100 * finalDistance);
				
				if (runningAtVerboseMode)
				{
					//print every reference in loop
					System.out.print(counter);
					System.out.println("\tReference               : " + currentReference.getLabel());
					System.out.println("\tLabel Distance          : " + currentLabelDistance);
					System.out.println("\tSignature Distance      : " + currentSignatureDistance);
					System.out.println("\tNorm Signature Distance : " + normalizedSignatureDistance);
					System.out.println("\tFinal Distance          : " + finalDistance);
					System.out.println("\tScore                   : " + score + " %\n");
				}
			}
			counter++;
			skipThis = false;
			totalReferenceSeconds += RefNumOfVectors;
			
		}//endfor every key in keyset

		
		// stop the monitors
		long stopTimeMillis = System.currentTimeMillis();
		long searchTime = stopTimeMillis - startTimeMillis;
		stopwatch.stop();
		System.out.print("Search completed in : " + stopwatch);
		
		if (usingKeywordHeuristic) System.out.println(" (Using the keyword heuristic)");
		if (usingMetaphoneHeuristic) System.out.println(" (Using the metaphone heuristic)");
		if (!usingKeywordHeuristic && !usingMetaphoneHeuristic)
		{
			if ( usingPruningHeuristic && !usingCombinedDistance) System.out.println(" (Using text-based pruning, and audio signature matching)");
			if ( usingPruningHeuristic &&  usingCombinedDistance) System.out.println(" (Using text-based pruning, and combined metrics matching)");
			if (!usingPruningHeuristic && !usingCombinedDistance) System.out.println(" (Using exhaustive search, and audio signature matching)");
			if (!usingPruningHeuristic &&  usingCombinedDistance) System.out.println(" (Using exhaustive search, and combined metrics matching)");
		}
		
		//System.out.println("Monitor: " + mon.toString());
		
		if (finalDistanceRankingList.getSize() >= 2)
		{
			theBestResult = finalDistanceRankingList.getResultAtIndex(0);
			theSecondBestResult = finalDistanceRankingList.getResultAtIndex(1);
		}
		
		return theBestResult.md5;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Returns an ArrayList containing two objects: the value of the distance attribute in the Result object 
	 * created for the MusicURIReference that matches the given TestCaseIdentifier, and its position in the ranking list
	 * @param results the ResultRankingList object containing Result objects ranked according to distance
	 * @param queryIdentifier the integer identifier used to denote the specific test case
	 * @return an ArrayList containing the ranking position and distance of the desired MusicURIReference
	 */
	public ArrayList getResultByTestCaseIdentifier(ResultRankingList results, int queryIdentifier)
	{
		ArrayList returnList = null;
		
		Result tmpResult;
		String tmpLabel;
		int tmpIdentifier;
		
		for (int i = 0; i < results.getSize(); i++)
		{
			tmpResult = ((Result)results.rankList[i]);
			tmpLabel = db.getMusicURIReference(tmpResult.md5).getLabel();
			tmpIdentifier = Toolset.getTestCaseIdentifier(tmpLabel);
			
			if (tmpIdentifier == queryIdentifier) 
			{
				Integer position = new Integer(i);
				returnList = new ArrayList(2);
				returnList.add(position);
				returnList.add(tmpResult);
				return returnList;
			}
		}
				
		return returnList;
	}
	
	/**
	 * Prunes the database keeping only the 10% of all MusicURIReference objects in it. 
	 * The criterion for keeping or rejecting a MusicURIReference object is the distance its 
	 * labelling yields when compared to the query labelling. Using an approximate string matching 
	 * technique, the best MusicURiReference objects are chosen and returned.
	 * @param allMusicURIReferenceKeys a set view of the keys contained in the MusicURIDatabase table (HashMap)
	 * @param queryLabelling a String containing an informative label for the MusicURIQuery (currently it is the filename)
	 * @return a ResultRankingList containing the best MusicURIReference candidates, with respect to labelling distance
	 */
	public ResultRankingList pruneDatabase(Set allMusicURIReferenceKeys, String queryLabelling)
	{
		if (allMusicURIReferenceKeys == null) System.out.println("allMusicURIReferenceKeys null");
		if (queryLabelling == null) System.out.println("queryLabelling null");
		int numberOfGoodCandidates = allMusicURIReferenceKeys.size() / 10; //10%
		
		// the ranking list
		ResultRankingList rankingList = new ResultRankingList(numberOfGoodCandidates);
		Result theNewResult = null;
		
		/*Definition: A measure of similarity between two strings. 
		 * The Jaro measure is the weighted sum of percentage of matched characters 
		 * from each file and transposed characters. Winkler increased this measure 
		 * for matching initial characters, then rescaled it by a piecewise function, 
		 * whose intervals and weights depend on the type of string (first name, last 
		 * name, street, etc.). (http://www.nist.gov/dads/HTML/jaroWinkler.html)
		 * */
		JaroWinkler test = new JaroWinkler();
		StringWrapper queryWrapper;
		StringWrapper refWrapper;
		
		queryLabelling = Toolset.removeTestCaseIdentifier(queryLabelling);
		queryWrapper = test.prepare(queryLabelling);
		
		float unit = 1.0f;
		float editDistance = 777.777f;
		
		String currentMD5;
		MusicURIReference currentReference;
		
		
		for (Iterator iter = allMusicURIReferenceKeys.iterator(); iter.hasNext();)
		{
			currentMD5 = (String) iter.next();
			currentReference = db.getMusicURIReference(currentMD5);
			if (currentReference == null) System.out.println("31");
			String refname = currentReference.getLabel();
			refname = Toolset.removeTestCaseIdentifier(refname);
			refWrapper = test.prepare(refname);
			
			editDistance = unit - (float) test.score(queryWrapper, refWrapper);
			theNewResult = new Result (editDistance, currentMD5);
			rankingList.RankThis(theNewResult);
			
		}//endfor every key in keyset

		return rankingList;
	}
	
	/**
	 * Returns the number of True Positive identifications that have taken place during a certain batch of tests. 
	 * @param allStats an ArrayList containing PerformanceStatistic objects
	 * @return the number of True Positive identifications
	 */
	public int getNumOfTruePositives(ArrayList allStats)
	{
		PerformanceStatistic tempStat;
		int truePositives = 0;
		
		for (int i = 0; i < allStats.size(); i++)
		{
			tempStat = (PerformanceStatistic) allStats.get(i);
			if (tempStat.isTruePositive()) truePositives++;
		}
		return truePositives;
	}
	
	/**
	 * Returns the average separation (distance) between the best and second-best matches for a certain batch of tests. 
	 * @param allStats an ArrayList containing PerformanceStatistic objects
	 * @return average separation (distance) between the best and second-best matches
	 */
	public double getAvgSeparationOfBestFromSecondBestMatch(ArrayList allStats)
	{
		
		PerformanceStatistic tempStat;
		
		SummaryStatistics TPBestMatchSummary = SummaryStatistics.newInstance();
		SummaryStatistics TPSecondBestSummary = SummaryStatistics.newInstance();
		
		for (int i = 0; i < allStats.size(); i++)
		{
			tempStat = (PerformanceStatistic) allStats.get(i);
			
			if (tempStat.isTruePositive())
			{
				TPBestMatchSummary.addValue(tempStat.getBestMatchDistance());
				TPSecondBestSummary.addValue(tempStat.getSecondBestMatchDistance());
			}
		}
		
		double separation = TPSecondBestSummary.getMean() - TPBestMatchSummary.getMean();
		
		return separation;
	}
	
	/**
	 * Accumulates and prints performance statistics regarding speed and accuracy for a certain batch of tests. 
	 * @param allStats an ArrayList containing PerformanceStatistic objects
	 */
	public void mergeStatistics(ArrayList allStats)
	{
		PerformanceStatistic tempStat;
		
		int truePositives = 0;
		int falsePositives = 0;
		int trueNegatives = 0;
		int falseNegatives = 0;
		
		SummaryStatistics TPBestMatchSummary = SummaryStatistics.newInstance();
		SummaryStatistics SecondBestSummary = SummaryStatistics.newInstance();
		SummaryStatistics WorstMatchSummary = SummaryStatistics.newInstance();
		
		SummaryStatistics FPBestMatchSummary = SummaryStatistics.newInstance();
		
		SummaryStatistics BothTP_FPBestMatchSummary = SummaryStatistics.newInstance();
		
		SummaryStatistics TNSummary = SummaryStatistics.newInstance();
		SummaryStatistics FNSummary = SummaryStatistics.newInstance();
		
		SummaryStatistics pruningSpeedSummary = SummaryStatistics.newInstance();
		SummaryStatistics matchingSpeedSummary = SummaryStatistics.newInstance();
		SummaryStatistics totalSpeedSummary = SummaryStatistics.newInstance();
		
		
		for (int i = 0; i < allStats.size(); i++)
		{
			tempStat = (PerformanceStatistic) allStats.get(i);
			
			if (tempStat.isTruePositive()) truePositives++;
			if (tempStat.isFalsePositive()) falsePositives++;
			if (tempStat.isTrueNegative()) trueNegatives++;
			if (tempStat.isFalseNegative()) falseNegatives++;
			
			// accurate results only
			//if (tempStat.isTruePositive() || tempStat.isTrueNegative())
			
			pruningSpeedSummary.addValue(tempStat.getPruningTime());
			matchingSpeedSummary.addValue(tempStat.getMatchingTime());
			totalSpeedSummary.addValue(tempStat.getPruningTime() + tempStat.getMatchingTime());
			
			if (tempStat.isTruePositive())
			{
				TPBestMatchSummary.addValue(tempStat.getBestMatchDistance());
				SecondBestSummary.addValue(tempStat.getSecondBestMatchDistance());
			}
			
			if (tempStat.isFalsePositive())
			{
				FPBestMatchSummary.addValue(tempStat.getBestMatchDistance());
			}
			
			BothTP_FPBestMatchSummary.addValue(tempStat.getBestMatchDistance());
			WorstMatchSummary.addValue(tempStat.getWorstMatchDistance());
		}
		
		System.out.println("---------------------------------------------------------");
		
		System.out.println("\nTrue Positives      : " + truePositives + "/" + allStats.size());
		System.out.println("False Positives     : " + falsePositives + "/" + allStats.size());
		System.out.println("True Negatives      : " + trueNegatives + "/" + allStats.size());
		System.out.println("False Negatives     : " + falseNegatives + "/" + allStats.size());
		
		System.out.println("\nTrue Positive Best Match Statistics");
		System.out.println("Distance Min        : " + TPBestMatchSummary.getMin());
		System.out.println("Distance Max        : " + TPBestMatchSummary.getMax());
		System.out.println("Distance Mean       : " + TPBestMatchSummary.getMean());
		System.out.println("Distance Variance   : " + TPBestMatchSummary.getVariance());
		System.out.println("Distance StdDev     : " + TPBestMatchSummary.getStandardDeviation());
		System.out.println("Score Mean          : " + (100 - (100 * (TPBestMatchSummary.getMean())))+ " %");
		
		System.out.println("\n2nd Match Statistics");
		System.out.println("Distance Min        : " + SecondBestSummary.getMin());
		System.out.println("Distance Max        : " + SecondBestSummary.getMax());
		System.out.println("Distance Mean       : " + SecondBestSummary.getMean());
		System.out.println("Score Mean          : " + (100 - (100 * (SecondBestSummary.getMean())))+ " %");
		
		System.out.println("\nNth Match Statistics");
		System.out.println("Distance Min        : " + WorstMatchSummary.getMin());
		System.out.println("Distance Max        : " + WorstMatchSummary.getMax());
		System.out.println("Distance Mean       : " + WorstMatchSummary.getMean());
		System.out.println("Score Mean          : " + (100 - (100 * (WorstMatchSummary.getMean())))+ " %");
		
		System.out.println("\nFalse Positive Best Match Statistics");
		System.out.println("Distance Min        : " + FPBestMatchSummary.getMin());
		System.out.println("Distance Max        : " + FPBestMatchSummary.getMax());
		System.out.println("Distance Mean       : " + FPBestMatchSummary.getMean());
		System.out.println("Distance Variance   : " + FPBestMatchSummary.getVariance());
		System.out.println("Distance StdDev     : " + FPBestMatchSummary.getStandardDeviation());
		System.out.println("Score Mean          : " + (100 - (100 * (FPBestMatchSummary.getMean()))) + " %");
		
		System.out.println("\nBest Match Statistics (Regardless being False or True Positive) ");
		System.out.println("Distance Min        : " + BothTP_FPBestMatchSummary.getMin());
		System.out.println("Distance Max        : " + BothTP_FPBestMatchSummary.getMax());
		System.out.println("Distance Mean       : " + BothTP_FPBestMatchSummary.getMean());
		System.out.println("Distance Variance   : " + BothTP_FPBestMatchSummary.getVariance());
		System.out.println("Distance StdDev     : " + BothTP_FPBestMatchSummary.getStandardDeviation());
		System.out.println("Score Mean          : " + (100 - (100 * (BothTP_FPBestMatchSummary.getMean()))) + " %");
		
		System.out.println("\n\nPruning Speed Statistics");
		System.out.println("Speed Min           : " + (pruningSpeedSummary.getMin()/1000) + " sec" );
		System.out.println("Speed Max           : " + (pruningSpeedSummary.getMax()/1000) + " sec" );
		System.out.println("Speed Mean          : " + (pruningSpeedSummary.getMean()/1000) + " sec" );
		
		System.out.println("\nMatching Speed Statistics");
		System.out.println("Speed Min           : " + (matchingSpeedSummary.getMin()/1000) + " sec" );
		System.out.println("Speed Max           : " + (matchingSpeedSummary.getMax()/1000) + " sec" );
		System.out.println("Speed Mean          : " + (matchingSpeedSummary.getMean()/1000) + " sec" );
		
		System.out.println("\nOverall Speed Statistics");
		System.out.println("Speed Min           : " + (totalSpeedSummary.getMin()/1000) + " sec" );
		System.out.println("Speed Max           : " + (totalSpeedSummary.getMax()/1000) + " sec" );
		System.out.println("Speed Mean          : " + (totalSpeedSummary.getMean()/1000) + " sec" );
		
		
	}
		
	/**
	 * Identifies the MusicURIReference that most closely matches the given audio file
	 * @param args the audio file to identify
	 */
	public static void main(String[] args) throws Exception
	{
		MusicURISearch engine = new MusicURISearch ((Toolset.getCWD()+"db\\"), "MusicURIReferences.db");
//		MusicURIDatabase db = new MusicURIDatabase ("C:/Eclipse/workspace/MusicURI/db/", "MusicURIReferences.db");
//		MusicURISearch engine = new MusicURISearch (db);
		
		//MusicURISearch engine = new MusicURISearch ("D:/10References/", "MusicURIReferences.db");
		
		//*****************************************************************************
		//*************************   F I L E   I N P U T   ***************************
		//*****************************************************************************
		
		if ((args.length == 1) && (new File (args[0]).exists()))
		{

			// get the file's canonical path
			File givenHandle = new File(args[0]);
			
			boolean finalResortIsCombinedDistance = true;
			
			String queryAudioCanonicalPath = givenHandle.getCanonicalPath();
			System.out.println("Input: " + queryAudioCanonicalPath);
			
			PerformanceStatistic tempStat;
			
			if (givenHandle.isDirectory())
			{
				File[] list = givenHandle.listFiles();
				if (list.length == 0)
				{
					System.out.println("Directory is empty");
					return;
				}
				else
				{
					ArrayList allStats = new ArrayList();
					File currentFile;
					int truePositives = 0;
					int falsePositives = 0;
					int trueNegatives = 0;
					int falseNegatives = 0;
					
					if (finalResortIsCombinedDistance) System.out.println(" Final resort is combined distance");
					else System.out.println(" Final resort is audio signature distance");
					
					for (int i = 0; i < list.length; i++)
					{
						currentFile = list[i];
						try
						{
							if (Toolset.isSupportedAudioFile(currentFile))
							{
								System.out.println("\nIdentifying         : " + currentFile.getName());
								tempStat = engine.getIdentificationPerformance (new MusicURIQuery(givenHandle), true, true, 0.09f, finalResortIsCombinedDistance);
								//identify (new MusicURIQuery(currentFile), true, true, 0.09f, finalResortIsCombinedDistance, 1);
								if (tempStat!=null) allStats.add(tempStat);
								
								if (tempStat.isTruePositive()) truePositives++;
								if (tempStat.isFalsePositive()) falsePositives++;
								if (tempStat.isTrueNegative()) trueNegatives++;
								if (tempStat.isFalseNegative()) falseNegatives++;
								
								System.out.println("\nTrue Positives      : " + truePositives + "/" + allStats.size());
								System.out.println("False Positives     : " + falsePositives + "/" + allStats.size());
								System.out.println("True Negatives      : " + trueNegatives + "/" + allStats.size());
								System.out.println("False Negatives     : " + falseNegatives + "/" + allStats.size());
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					System.out.println("\n\nStatistics for Test Case: " + queryAudioCanonicalPath);
					engine.mergeStatistics(allStats);
				}
			}//end if givenHandle is Directory
			if (givenHandle.isFile())
			{
				if (Toolset.isSupportedAudioFile(givenHandle))
				{
					//tempStat = engine.getIdentificationPerformance (new MusicURIQuery(givenHandle), true, true, 0.09f, finalResortIsCombinedDistance);
					tempStat = engine.getIdentificationPerformance (new MusicURIQuery(givenHandle), false, false, 0.09f, false);
					//identify (new MusicURIQuery(givenHandle), true, true, 0.09f, finalResortIsCombinedDistance, 1);
					
					if (tempStat!=null)
					{
						System.out.println("\nIdentification completed");
						//tempStat.printStatistics();
						ArrayList allStats = new ArrayList();
						allStats.add(tempStat);
						engine.mergeStatistics(allStats);
					}
					else 
						System.out.println("Error in identification ");
				}
			}
		}//end if
		else
		{
			System.err.println("MusicURISearch");
			System.err.println("Usage: java it.univpm.deit.semedia.musicuri.core.MusicURISearch {unknown.mp3}");
		}
		
	}//end main method
		
	
}//end class
