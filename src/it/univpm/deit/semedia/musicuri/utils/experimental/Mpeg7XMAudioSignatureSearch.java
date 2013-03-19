//////////////////////////////////////////////////////////////////////////////
//
// This software module was originally developed by
//
// Thorsten Heinz
// Fraunhofer IIS
//
// in the course of development of the MPEG-7 Experimentation Model.
//
// This software module is an implementation of a part of one or more MPEG-7
// Experimentation Model tools as specified by the MPEG-7 Requirements.
//
// ISO/IEC gives users of MPEG-7 free license to this software module or
// modifications thereof for use in hardware or software products claiming
// conformance to MPEG-7.
//
// Those intending to use this software module in hardware or software products
// are advised that its use may infringe existing patents. The original
// developer of this software module and his/her company, the subsequent
// editors and their companies, and ISO/IEC have no liability for use of this
// software module or modifications thereof in an implementation.
//
// Copyright is not released for non MPEG-7 conforming products. The
// organizations named above retain full right to use the code for their own
// purpose, assign or donate the code to a third party and inhibit third
// parties from using the code for non MPEG-7 conforming products.
//
// Copyright (c) 1998-1999.
//
// This notice must be included in all copies or derivative works.
//
// Applicable File Name:  AudioSignatureSearch.cpp
//

package it.univpm.deit.semedia.musicuri.utils.experimental;

public class Mpeg7XMAudioSignatureSearch
{
	
	// Weighting vector for the mean vectors (unit variance)
	public static double meanWeight[] =
	{
		11.933077, 11.103332, 10.464845, 10.231990, 9.743346, 9.645541, 9.644496, 9.672296,
		8.791485, 8.741418, 8.613710, 8.741487, 8.126337, 8.257461, 8.053868, 8.387219
	};

	// Weighting vector for the variance vectors (unit variance)
	public static double varWeight[] =
	{
		69.486160, 69.157463, 73.134132, 73.182343,	76.521751, 77.552032, 81.201492, 85.420212,
		74.791885, 76.823540, 79.335228, 82.347801,	65.784485, 69.698914, 67.417160, 67.184540
	};
	
	// determines the weighted Euclidian distance between a reference and a query audio signature
	public static double WeightedEuclidianDistance ( double[] refMean,
														double[] refVar,
														int numRefVec,
														double[] queryMean,
														double[] queryVar,
														int numQueryVec,
														int vectorDim)
	{
		//--- Check whether the vector dimensions are compatible
		if(meanWeight.length != vectorDim)
		{
			System.out.println("Vector dimensions not identical");
			return -1.0f;
		}
		
		int i, j, k;
		double distance = 0.0f;
		
		//--- Loop over all query vectors (scalable series)
		for(i = 0; i < numQueryVec; ++i)
		{
			//System.out.println("query i: " + i);
			
			double minDistance = 9999.999;
			
			//--- Loop over reference fingerprint vectors (16-dimensional fingerprint vectors)
			for(j = 0; j < numRefVec; ++j) //eg 136 times for 136 seconds of query audio
			{
				//System.out.println("reference j: " + j);
				double tmpDistance = 0.0f;
				//--- Calculate distance of mean vector
				for(k = 0; k < vectorDim; ++k) //16 times == freq bands
				{
					//System.out.println("mean k: " + k);
					double v1 = queryMean[vectorDim*i + k] * meanWeight[k];
					double v2 = refMean[vectorDim*j + k] * meanWeight[k];
					tmpDistance += (v1-v2)*(v1-v2);
				}
				
				//--- Calculate distance of variance vector
				for(k = 0; k < vectorDim; ++k) //16 times == freq bands
				{
					//System.out.println("variance k: " + k);
					double v1 = queryVar[vectorDim*i + k] * varWeight[k];
					double v2 = refVar[vectorDim*j + k] * varWeight[k];
					tmpDistance += (v1-v2)*(v1-v2);
				}
				
				if(tmpDistance < minDistance) {minDistance = tmpDistance;}
			}
			
			//--- Cumulate overall distance for all query vectors
			distance += minDistance;
		}
		
		return Math.sqrt(distance/(vectorDim*numQueryVec));
	}
	
}//end class
