package it.univpm.deit.semedia.musicuri.webservice.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.apache.axis.AxisProperties;
import org.xml.sax.SAXException;

import it.univpm.deit.database.datatypes.AudioLLDmeta;
import it.univpm.deit.database.datatypes.Mp7ACT;
import it.univpm.deit.semedia.musicuri.core.MusicURIDatabase;
import it.univpm.deit.semedia.musicuri.core.MusicURIQuery;
import it.univpm.deit.semedia.musicuri.core.MusicURIReference;
import it.univpm.deit.semedia.musicuri.core.MusicURISearch;
import it.univpm.deit.semedia.musicuri.core.Result;
import it.univpm.deit.semedia.musicuri.core.ResultRankingList;
import it.univpm.deit.semedia.musicuri.core.Toolset;
import it.univpm.deit.semedia.musicuri.statistics.PerformanceStatistic;
import it.univpm.deit.semedia.musicuri.utils.misc.MusicURILibraryDemo;

public class MusicURIWebSearch
{

	private static MusicURIDatabase db = new MusicURIDatabase
			(getWebServiceDatabaseDirectory(), "\\MusicURIReferences.db"); // C:\WINDOWS\SYSTEM32

	private MusicURISearch engine = new MusicURISearch(db);

	public String performSearch(String xmlAudioSignature, String filename)
	{
		try
		{
			String returnString = "";

			String tempXMLFile = Toolset.getCWD() + "/temp.xml";

			BufferedWriter out = new BufferedWriter(new FileWriter(tempXMLFile));
			out.write(xmlAudioSignature);
			out.close();

			Mp7ACT act = new Mp7ACT();
			act.fromXML(tempXMLFile);
			MusicURIQuery query = new MusicURIQuery();
			query.setAudioCompactType(act);

			boolean usingPruningHeuristic;
			boolean usingCombinedDistance;
			boolean finalResortIsCombinedDistance;
			ResultRankingList finalDistanceRankingList;
			Result theBestResult = null;
			Result theSecondBestResult = null;
			float maximumThreshold = 0.9f;


			if (db == null)
			{
				returnString = "DB was not deserialized (null)";
				return returnString;
			}
			else
			{
				// if a filename has been provided, use it as a heuristic in search,
				// if not, perform a blind search, based on the audio signature only
				if (filename != null)
				{
					usingPruningHeuristic = true;
					usingCombinedDistance = true;
					finalResortIsCombinedDistance = true;
					query.setLabel(filename);

					returnString += queryDB(query, new Boolean(true));
				}
				else
				{
					usingPruningHeuristic = false;
					usingCombinedDistance = false;
					finalResortIsCombinedDistance = false;
					query.setLabel("unlabelled");

					returnString += queryDB(query, new Boolean(false));
				}

				return returnString;

			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return (e.getMessage());
		}

	}


	public int getNumOfMusicURIReferences()
	{
		if (db == null)
			return (-1);
		else
			return db.getDbSize();
	}

	public String getMusicURIReferenceList()
	{
		if (db == null || db.getDbSize() == 0)
			return ("Database object could not be deserialized from "
					+ getWebServiceDatabaseDirectory() );
		else
			return db.textFormattedSetOfMusicURIReferences();
	}
	
	private static String getWebServiceDatabaseDirectory()
	{
		// Get the "." file representing the cwd
		File cwd = new File(".");
		// Get the absolute path to the current working directory
		return cwd.getAbsolutePath();
	}

	private String queryDB(MusicURIQuery query, Boolean usefilenameValue)
	{
		String reply = "";

		ResultRankingList finalDistanceRankingList = engine.identify(query,
																	usefilenameValue.booleanValue(),
																	usefilenameValue.booleanValue(),
																	0.9f, //90% acceptable similarity rating
																	usefilenameValue.booleanValue());

		if (finalDistanceRankingList.getSize() >= 1)
		{
			Result theBestResult = finalDistanceRankingList.getResultAtIndex(0);
			double bestMatchDistance = theBestResult.distance;

			reply += ("\nMatched with        : "	+ (db.getMusicURIReference(theBestResult.md5)).getLabel());
			reply += ("\nScore               : " + (float) (100 - (100*(theBestResult.distance))) + "%");
		}
		else
		{
			if (finalDistanceRankingList == null)
				reply += ("the ranking list was returned null");
			else
				reply += ("the ranking list has " + finalDistanceRankingList.getSize() + " items");
		}
		return reply;
	}
}
