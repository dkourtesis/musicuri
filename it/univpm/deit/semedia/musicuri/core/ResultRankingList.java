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


import java.util.ArrayList;

/**
* @author Dimitrios Kourtesis
*/
public class ResultRankingList
{
	
	/**
	 * An array of Result objects
	 */
	public Result[] rankList;
	
	/**
	 * The last index in the rankList attribute, which is currently null
	 */
	int lastNullIndex;
	
	/**
	 * The maximum allowed size for the ranking list
	 */
	int maxSize;
	
	
	
	
	/**
	 * Constructs an empty ResultRankingList object having size equal to the one specified
	 * @param size the maximum allowed size of the ranking list 
	 */
	public ResultRankingList(int size)
	{
		this.rankList = new Result[size];
		this.lastNullIndex = 0;
		this.maxSize = size;
	}
	
	/**
	 * Adds the given Result object to its sorted position, according to distance, 
	 * and shifts all other results downwards. The worst result is removed, if the 
	 * maximum number of results in the list has been reached. If the given result 
	 * is not better than any other result in the list, and the max size has been 
	 * reached, the given result is ignored.
	 * @param theNewResult the Result object to add to the list
	 * @return the number of results currently in the list
	 */
	public int RankThis(Result theNewResult)
	{
		Result currentResult;
		
		if (lastNullIndex == 0)
		{
			rankList[0] = theNewResult;
			lastNullIndex++;
		}
		else
		{
			for (int i = 0; i < maxSize; i++)
			{
				currentResult = (Result) rankList[i];
				
				if ( currentResult == null)
				{
					//shift all results downwards, deleting the last one
					for (int j = lastNullIndex-1; j > i; j--)
					{
						rankList[j] = rankList[j-1];
					}
					// insert into position i
					rankList[i] = theNewResult;
					lastNullIndex++;
					return lastNullIndex;
				}
				if ( currentResult != null && theNewResult.distance <= currentResult.distance ) 
				{
					if (lastNullIndex == maxSize)
					{
						for (int j = lastNullIndex-1; j > i; j--)
						{
							rankList[j] = rankList[j-1];
						}
						// insert into position i
						rankList[i] = theNewResult;
					}
					else
					{
						for (int j = lastNullIndex; j > i; j--)
						{
							rankList[j] = rankList[j-1];
						}
						// insert into position i
						rankList[i] = theNewResult;
						lastNullIndex++;
					}
					return lastNullIndex;
				}
			}
		}
		return lastNullIndex;
	}
	
	/**
	 * Returns the ranking list
	 * @return rankList the ranking list
	 */
	public Result[] getRankList()
	{
		return rankList;
	}
	
	/**
	 * Returns the size that the ranking list has been initialized to
	 * @return the ranking list's initialization size
	 */
	public int getSize()
	{
		return rankList.length;
	}
	
	/**
	 * Returns an ArrayList object containing all MD5 keys of the 
	 * results currently contained in the ranking list
	 * @return keyList the ArrayList object containing all MD5 keys
	 */
	public ArrayList getRankListMd5Keys()
	{
		Result currentResult;
		String currentMD5;
		ArrayList keyList = new ArrayList();
		
		for (int i = 0; i < lastNullIndex; i++)
		{
			currentResult = (Result) rankList[i];
			currentMD5 = currentResult.md5; 
			keyList.add(currentMD5);
		}
		return keyList;
	}
	
	/**
	 * Returns the value of the distance atribute inside a Result object, that 
	 * has been added to the ranking list. If a Result object with the given 
	 * MD5 key does not exist in the ranking list, the return value is 1.0;
	 * @param wantedKey the MD5 hash key of the wanted Result object
	 * @return the value of the distance attribute in the Result object requested 
	 */
	public double getResultDistance(String wantedKey)
	{
		Result currentResult;
		
		for (int i = 0; i < lastNullIndex; i++)
		{
			currentResult = (Result) rankList[i];
			if (currentResult.md5.equalsIgnoreCase(wantedKey))
			return currentResult.distance;
		}
		
		return 1.0;
	}
	
	/**
	 * Returns the ranking index of a Result object that has been added to 
	 * the ranking list. If a Result object with the given MD5 key does not 
	 * exist in the ranking list, the return value is the maximum raking list size;
	 * @param wantedKey the MD5 hash key of the wanted Result object
	 * @return the ranking index of the Result object requested
	 */
	public int getRankingPositionOf(String wantedKey)
	{
		Result currentResult;
		
		for (int i = 0; i < maxSize; i++)
		{
			currentResult = (Result) rankList[i];
			if (currentResult.md5.equalsIgnoreCase(wantedKey))
			{
				return i+1;
			}
		}
		//System.out.println("returning " + position); 
		return maxSize;
	}
	
	/**
	 * Returns the Result object located in the given index. If the given index 
	 * is larger than the index of the last known valid entry, null is returned.
	 * @param index the index (starting at zero) of the wanted Result object
	 * @return the Result object stored in the ranking index specified
	 */
	public Result getResultAtIndex(int index)
	{
		if (index <= lastNullIndex) return (Result) rankList[index];
		else return null;
	}
	
	/**
	 * Prints some of the private attribute's values on screen (Experimental use only)
	 */	
	public void printContents()
	{
		for (int i = 0; i < lastNullIndex; i++)
		{
			if (rankList[i]==null) 
				System.out.println("-"); 
			else 
			{
				System.out.println(rankList[i].distance + " - " + rankList[i].md5);
			}
		}
	}
		

}//end class
