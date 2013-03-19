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

/**
* @author Dimitrios Kourtesis
*/
public class Result
{
	
	/**
	 * A generic double value denoting the distance of the specific result from the query used in comparison
	 */
	public double distance;
	
	/**
	 * The MD5 key contained in the MusicURIReference this result corresponds to
	 */
	public String md5;
	
	
	
	
	/**
	 * Constructs a Result object by setting its private attributes to the ones specified
	 * @param dist the distance of the correponding reference from the query it was compared with
	 * @param key the MD5 key contained in the MusicURIReference this result corresponds to
	 */
	public Result (double dist, String key)
	{
		this.distance = dist;
		this.md5 = key;
	}
	
	/**
	 * Compares a Result object with another by comparing their private attributes
	 * (Experimental use only)
	 * @param other the Result object to compare with
	 */
	public boolean isSameWith(Result other)
	{
		if (other != null)
		{
			
			if ((other.distance == this.distance) 
			&& other.md5.equalsIgnoreCase(this.md5))
			{
				return true;
			}
			else return false;
		}
		else 
		{
			System.out.println("other == null");
			return false;
		}

	}
	
	
}//end class
