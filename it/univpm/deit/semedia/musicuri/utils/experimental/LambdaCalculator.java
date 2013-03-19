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
import it.univpm.deit.semedia.musicuri.core.MusicURIQuery;
import it.univpm.deit.semedia.musicuri.core.MusicURIReference;
import it.univpm.deit.semedia.musicuri.core.Toolset;
import it.univpm.deit.semedia.musicuri.statistics.PerformanceStatistic;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;


import org.apache.commons.math.stat.descriptive.SummaryStatistics;


import com.wcohen.secondstring.JaroWinkler;
import com.wcohen.secondstring.StringWrapper;

/**
 * Utility class used for calculating the optimal lambda for a linear combination of heterogeneous distance metrics
 */
public class LambdaCalculator
{
	static String databasePath = "D:/1000ReferenceDB/";
	static String databaseFileName = "MusicURIReferences.db";
	static MusicURIDatabase db = new MusicURIDatabase(databasePath, databaseFileName);

	public static void main(String[] args) throws Exception
	{
		
		//*****************************************************************************
		//*************************   F I L E   I N P U T   ***************************
		//*****************************************************************************

		if ((args.length == 1) && (new File (args[0]).exists()))
		{
			// get the file's canonical path
			File givenHandle = new File(args[0]);
			String queryAudioCanonicalPath = givenHandle.getCanonicalPath();
			System.out.println("Input: " + queryAudioCanonicalPath);
			
			//PerformanceStatistic tempStat;
			SummaryStatistics lambdaSummary = SummaryStatistics.newInstance();

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
					for (int i = 0; i < list.length; i++)
					{
						currentFile = list[i];
						try
						{
							if (Toolset.isSupportedAudioFile(currentFile))
							{
								System.out.println("\nCalculating optimal lambda : " + currentFile.getName());
								lambdaSummary.addValue(getBestLambda (new MusicURIQuery(currentFile)));
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
//					System.out.println("\n\nStatistics for Test Case: " + queryAudioCanonicalPath);
//					mergeStatistics(allStats);
				}
			}
			if (givenHandle.isFile())
			{
				if (Toolset.isSupportedAudioFile(givenHandle))
				{
//					tempStat = getBestLambda (new MusicURIQuery(givenHandle));
//					if (tempStat!=null)
//					{
//						//tempStat.printStatistics();
//						ArrayList allStats = new ArrayList();
//						allStats.add(tempStat);
//						mergeStatistics(allStats);
//					}
//					else 
//						System.out.println("Error in identification ");
				}
			}

		}//end if
		else
		{
			System.err.println("LambdaCalculator");
			System.err.println("Usage: java tester.LambdaCalculator {directory}");
		}

	}//end main method
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static double getBestLambda (MusicURIQuery query)
	{
		
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
		}
		int vectorSize = queryMean.vectorSize; // internal! stay out! read the matrix size instead
		float[][] queryMeanMatrix = queryMean.__rawVectors;
		float[][] queryVarianceMatrix = queryVariance.__rawVectors;
		int QueryVectorDim = vectorSize; // ==number of dimensions, subbands
		String queryLabelling = query.getLabel();
		int queryIdentifier = Toolset.getTestCaseIdentifier(queryLabelling);
		StringWrapper queryWrapper = null;
		JaroWinkler test = new JaroWinkler();
		queryLabelling = Toolset.removeTestCaseIdentifier(queryLabelling);
		queryWrapper = test.prepare(queryLabelling);
		
		//*****************************************************************************
		//*********   R E F E R E N C E   D A T A   P R E P A R A T I O N   ***********
		//*****************************************************************************

		int RefVectorDim = 0; 		// number of subbands, dimensions
		double combinedDistance;
		double currentLabelDistance = 0.0;
		double currentSignatureDistance = 0.0;
		double normalizedSignatureDistance = 0.0;
		double confidence = 0.0;
		Mp7ACT mp7;
		String currentMD5;
		MusicURIReference currentReference;
		AudioLLDmeta refMean;
		AudioLLDmeta refVariance;
		float[][] refMeanMatrix;
		float[][] refVarianceMatrix;
		
		StringWrapper refWrapper = null;
		float editDistance;
		int referenceIdentifier;
		String referenceLabelling;
				
		double lambdaYeldingSmallestCombinedDistance = 0.0;
		double smallestCombinedDistanceYet = 0.0;
		
		Set allMusicURIReferenceKeys = db.getSetOfMusicURIReferences();		
		
		//System.out.println("queryId: " + queryIdentifier);
		
		for (Iterator iter = allMusicURIReferenceKeys.iterator(); iter.hasNext();)
		{
			currentMD5 = (String) iter.next();
			currentReference = db.getMusicURIReference(currentMD5);
			referenceLabelling = currentReference.getLabel(); 
			referenceIdentifier = Toolset.getTestCaseIdentifier(referenceLabelling);
			
			if (referenceIdentifier == queryIdentifier)
			{
				mp7 = currentReference.getAudioCompactType();
				if (mp7 == null) 
				{
					System.out.println("Problem: No mpeg7 exists for given uri");
				}
				refMean = mp7.featureByName(Mp7ACT.FLATNESS, Mp7ACT.MEAN);
				refVariance = mp7.featureByName(Mp7ACT.FLATNESS, Mp7ACT.VARIANCE);
				if ((refMean == null) || (refVariance == null))
				{
					System.out.println("Skipping: problematic mpeg7 description!!! - "+mp7.getLabel()+")");
				}
				refMeanMatrix = refMean.__rawVectors;
				refVarianceMatrix = refVariance.__rawVectors;
				RefVectorDim = vectorSize; // number of subbands
						
				currentSignatureDistance = Toolset.getEuclidianDistance(refMeanMatrix, refVarianceMatrix, queryMeanMatrix, queryVarianceMatrix, QueryVectorDim, false);
				double theoreticalMaximum = (RefVectorDim * Math.sqrt(1)) * queryMeanMatrix.length;
				normalizedSignatureDistance = currentSignatureDistance / theoreticalMaximum; //eg (16 * sqrootof(1) ) * 10 --to scale at 0-1
				
				String refname = currentReference.getLabel();
				refname = Toolset.removeTestCaseIdentifier(refname);
				refWrapper = test.prepare(refname);
				editDistance = 1 - (float) test.score(queryWrapper, refWrapper);
				currentLabelDistance = editDistance; 
				
				System.out.println("currentLabelDistance: " + currentLabelDistance);
				System.out.println("normalizedSignatureDistance: " + normalizedSignatureDistance);
				
				//combinedDistance = (0.5 * currentLabelDistance) + (0.5 * normalizedSignatureDistance);
				//smallestCombinedDistanceYet	= (0.5 * currentLabelDistance) + (0.5 * normalizedSignatureDistance);
				for (double lambda = 0.0; lambda < 1.0; lambda += 0.01)
				{
					combinedDistance = lambda * currentLabelDistance + (1-lambda) * normalizedSignatureDistance;
					if (combinedDistance < smallestCombinedDistanceYet) 
					{
						smallestCombinedDistanceYet = combinedDistance;
						lambdaYeldingSmallestCombinedDistance = lambda;
					}
				}
				System.out.println("smallestCombinedDistanceYet: " + smallestCombinedDistanceYet);
				System.out.println("Best lambda: " + lambdaYeldingSmallestCombinedDistance);
				confidence = 100 - (100 * smallestCombinedDistanceYet);
				System.out.println("Best confidence: " + confidence); 
			}
		}
		return lambdaYeldingSmallestCombinedDistance;
	}
	
	
	
	
	
	
	
	
	
	
	public static void mergeStatistics(ArrayList allStats)
	{
		PerformanceStatistic tempStat;

		int truePositives = 0;
		int falsePositives = 0;
		int trueNegatives = 0;
		int falseNegatives = 0;
		
		SummaryStatistics TPBestMatchSummary = SummaryStatistics.newInstance();
		SummaryStatistics TPSecondBestSummary = SummaryStatistics.newInstance();
		
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
				TPSecondBestSummary.addValue(tempStat.getSecondBestMatchDistance());
			}
			
			if (tempStat.isFalsePositive())
			{
				FPBestMatchSummary.addValue(tempStat.getBestMatchDistance());
			}
			
			BothTP_FPBestMatchSummary.addValue(tempStat.getBestMatchDistance());
			
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
		System.out.println("Confidence Mean     : " + (100 - (100 * (TPBestMatchSummary.getMean())))+ " %");
		
		System.out.println("\nTrue Positive Second Best Statistics");
		System.out.println("Distance Min        : " + TPSecondBestSummary.getMin());
		System.out.println("Distance Max        : " + TPSecondBestSummary.getMax());
		System.out.println("Distance Mean       : " + TPSecondBestSummary.getMean());
		System.out.println("Confidence Mean     : " + (100 - (100 * (TPSecondBestSummary.getMean())))+ " %");
		
		System.out.println("\nFalse Positive Best Match Statistics");
		System.out.println("Distance Min        : " + FPBestMatchSummary.getMin());
		System.out.println("Distance Max        : " + FPBestMatchSummary.getMax());
		System.out.println("Distance Mean       : " + FPBestMatchSummary.getMean());
		System.out.println("Distance Variance   : " + FPBestMatchSummary.getVariance());
		System.out.println("Distance StdDev     : " + FPBestMatchSummary.getStandardDeviation());
		System.out.println("Confidence Mean     : " + (100 - (100 * (FPBestMatchSummary.getMean()))) + " %");
		
		System.out.println("\nBest Match Statistics (Regardless being False or True Positive) ");
		System.out.println("Distance Min        : " + BothTP_FPBestMatchSummary.getMin());
		System.out.println("Distance Max        : " + BothTP_FPBestMatchSummary.getMax());
		System.out.println("Distance Mean       : " + BothTP_FPBestMatchSummary.getMean());
		System.out.println("Distance Variance   : " + BothTP_FPBestMatchSummary.getVariance());
		System.out.println("Distance StdDev     : " + BothTP_FPBestMatchSummary.getStandardDeviation());
		System.out.println("Confidence Mean     : " + (100 - (100 * (BothTP_FPBestMatchSummary.getMean()))) + " %");
		
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

}