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


public class PerformanceStatistic
{
	private int numOfReferenceFiles;
	private int numberOfComparisonsMade;
	private int numberOfRejections;
	private int totalReferenceSeconds;
	private long pruningTime;
	private long matchingTime;
	private float rejectionPercentage;
	private float speedPerRef;
	private boolean truePositive = false;
	private boolean falsePositive = false;
	private boolean trueNegative = false;
	private boolean falseNegative = false;
	private double bestMatchDistance;
	private double secondBestMatchDistance;
	private double worstMatchDistance;
	
	/**
	 * Constructs a PerformanceStatistic object, calculates and assigns values to performance attibutes
	 * @param numOfReferenceFiles the number of MusicURIReference objects in the db
	 * @param numberOfComparisonsMade the number of comparisons made (certain MusicURIReference were rejected during db pruning)
	 * @param totalReferenceSeconds the seconds of audio referenced by the db, (ie total playtime of all referenced audio files)
	 * @param pruningTime the time spent during pruning the db
	 * @param matchingTime the time spent for completing search and finding the match 
	 * @param identificationValidity an integer ranging [0..4] indincating wether the match was a true positive, false positive, true negative, false negative
	 * @param bestMatchDistance the distance of the best match (ranked first) from the query
	 * @param secondBestMatchDistance the distance of the second-best match (ranked second) from the query
	 * @param worstMatchDistance the distance of the worst match (ranked last) from the query
	 */
	public PerformanceStatistic(int numOfReferenceFiles, 
						int numberOfComparisonsMade, 
						int totalReferenceSeconds,
						long pruningTime,
						long matchingTime,
						int identificationValidity,
						double bestMatchDistance,
						double secondBestMatchDistance,
						double worstMatchDistance)
	{
		this.numOfReferenceFiles = numOfReferenceFiles;
		this.numberOfComparisonsMade = numberOfComparisonsMade;
		this.numberOfRejections = numOfReferenceFiles - numberOfComparisonsMade;
		this.totalReferenceSeconds = totalReferenceSeconds;
		this.pruningTime = pruningTime;
		this.matchingTime = matchingTime;
		
		double execTime = pruningTime + matchingTime;
		
		// Checking if the number of reference files exceeds 0
		if (numOfReferenceFiles > 0 && numOfReferenceFiles - numberOfRejections > 0)  
		{
			rejectionPercentage = (float) (((float) 100/numOfReferenceFiles) * numberOfRejections);
			speedPerRef = (float)(((float) execTime/1000) /(numOfReferenceFiles - numberOfRejections));	
		}
		
		if (identificationValidity == 1) truePositive = true;
		if (identificationValidity == 2) falsePositive = true;
		if (identificationValidity == 3) trueNegative = true;
		if (identificationValidity == 4) falseNegative = true;
		
		this.bestMatchDistance = bestMatchDistance;
		this.secondBestMatchDistance = secondBestMatchDistance;
		this.worstMatchDistance = worstMatchDistance;
	}
	
	/**
	 * Prints some of the statistics related to speed and pruning efficiency
	 */
	public void printStatistics()
	{
		System.out.println("Reference Files in DB               : " + numOfReferenceFiles);
		System.out.println("Reference Files Processed           : " + numberOfComparisonsMade);
		System.out.println("Reference Files Rejected            : " + numberOfRejections + " (" + rejectionPercentage + " % of reference files)");			
		// TODO: there probably is some error in how "Reference Audio Processed in Total" is calculated
		System.out.println("Reference Audio Processed in Total  : " + totalReferenceSeconds + " audio sec (" + (float)totalReferenceSeconds/60 +" min / "+ (float)((float)(totalReferenceSeconds/60)/60) +" hrs)");
		System.out.println("Identification Completed in         : " + ((float) (pruningTime + matchingTime)/1000) + " seconds");  //elapsed_time
		System.out.println("Average Search Speed (Per Reference): " + speedPerRef + " seconds per reference");
	}
	
	/**
	 * @return the distance of the best match (ranked first) from the query
	 */
	public double getBestMatchDistance()
	{
		return bestMatchDistance;
	}
	
	/**
	 * @return the distance of the second-best match (ranked second) from the query
	 */
	public double getSecondBestMatchDistance()
	{
		return secondBestMatchDistance;
	}
	
	/**
	 * @return the distance of the worst match (ranked last) from the query
	 */
	public double getWorstMatchDistance()
	{
		return worstMatchDistance;
	}
	
	/**
	 * @return the time spent during pruning the db
	 */
	public long getPruningTime()
	{
		return pruningTime;
	}
	
	/**
	 * @return the time spent for completing search and finding the match 
	 */
	public long getMatchingTime()
	{
		return matchingTime;
	}
	
	/**
	 * @return true if the MusicURIReference ranked first was a True Positive, false otherwise
	 */
	public boolean isTruePositive()
	{
		return truePositive;
	}
	
	/**
	 * @return true if the MusicURIReference ranked first was a False Positive, false otherwise
	 */
	public boolean isFalsePositive()
	{
		return falsePositive;
	}
	
	/**
	 * @return true if the MusicURIReference ranked first was a True Negative, false otherwise
	 */
	public boolean isTrueNegative()
	{
		return trueNegative;
	}
	
	/**
	 * @return true if the MusicURIReference ranked first was a False Negative, false otherwise
	 */
	public boolean isFalseNegative()
	{
		return falseNegative;
	}

}//end class
