package it.univpm.deit.semedia.musicuri.utils.misc;

import it.univpm.deit.semedia.musicuri.core.MusicURIDatabase;
import it.univpm.deit.semedia.musicuri.core.MusicURIQuery;
import it.univpm.deit.semedia.musicuri.core.MusicURIReference;
import it.univpm.deit.semedia.musicuri.core.MusicURISearch;
import it.univpm.deit.semedia.musicuri.core.Result;
import it.univpm.deit.semedia.musicuri.core.ResultRankingList;
import it.univpm.deit.semedia.musicuri.core.Toolset;
import it.univpm.deit.semedia.musicuri.webservice.client.MusicURIWebSearchServiceLocator;
import it.univpm.deit.semedia.musicuri.webservice.client.MusicURIWebSearchSoapBindingStub;

import java.io.File;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import jargs.gnu.CmdLineParser;

/**
* @author Dimitrios Kourtesis
*/
public class MusicURILibraryDemo
{


	/**
	 * 
	 * This application intends to practically demonstrate the basic
	 * funcionality that MusicURI offers, which is to enable the mapping between
	 * a piece of music and a unique URI.
	 * 
	 * The demo application currently offers the following operations:
	 * 
	 * a) Query a MusicURI local database with a music item, and retrieve a URI
	 * b) Add a reference music item to a local MusicURI database 
	 * c) List all reference music items inside a local MusicURI database
	 * 
	 * The main method in this class enables a user to:
	 * 
	 * a) Query the local MusicURI database file at the specified path, with the
	 * given audio file, to retrieve a URI. The -q switch is accompanied by the
	 * path to the local database, and the -f flag (optional) signals if the
	 * filename should be utilized as a reliable hint within search. Format:
	 * java -jar MusicURI.jar [<audiofile>] [<-q> <DBfile>] [<-f>] example:
	 * "c:\test.wav" -q "C:\WINDOWS\system32\MusicURIReferences.db" -f
	 * 
	 * b) Add, to the local MusicURI database file at the specified path, the
	 * given audio file. The -a switch is accompanied by the path to the local
	 * database, into which the music item will be indexed. Format: java -jar
	 * MusicURI.jar [<audiofile>] [<-a> <DBfile>] example: "c:\test.wav" -a
	 * "C:\WINDOWS\system32\MusicURIReferences.db"
	 * 
	 * c) List the URIs of all music items indexed in the local MusicURI
	 * database file, at the specified path. Format: java -jar MusicURI.jar [<-l>
	 * <databasefile>] Example: -l "C:\WINDOWS\system32\MusicURIReferences.db"
	 * 
	 * Before proceeding with any of the aforementioned operations, the
	 * application verifies that any files that have been specified exist, and
	 * are valid.
	 * 
	 * For query operations, the user has to specify Where, What and How to
	 * identify. I.e. the user must specify the URL where the MusicURI data
	 * source resides, the audio file containing the unknown piece of music, and
	 * whether the system should utilize the provided filename as a reliable
	 * hint within the search, or not (default is false).
	 * 
	 * The filename should be taken into consideration only if the user is
	 * absolutely confident that it does not contain any misleading information
	 * about the artist and title of the respective piece of music. For example,
	 * this option could be safely used for a filename of the form <ARTIST>
	 * <SEPARATOR> <TITLE>.<EXTENSION>
	 * 
	 * Where: The URL specifying the location of the MusicURI data source 
	 * What : The audio file containing the piece of music to use as a query 
	 * How  : A flag determining whether to utilize the filename in search
	 */
	
	
	static MusicURIWebSearchSoapBindingStub stub;
	
	public static void main(String[] args)
	{
		if (args.length == 0)
			printUsage();
		else
		{
			/****************** ARGUMENT SETTING **************/
			// A command line parser to handle all options and arguments.
			CmdLineParser parser = new CmdLineParser();
			
			// The -f option is a flag determining whether to use or not use the filename 
			// within the search. No value follows, the flag is either present, or not.
			CmdLineParser.Option usefilename = parser.addBooleanOption('f', "usefilename");
			
			// The -q option is followed by a String value specifying the path 
			// of the database file to be queried.
			CmdLineParser.Option queryOption = parser.addStringOption('q', "query");
			
			// The -a switch is followed by a String value specifying the path to the 
			// local database, into which a given audio file will be indexed.
			CmdLineParser.Option addReferenceOption = parser.addStringOption('a', "addReference");
			
			// The -l switch is followed by a String value specifying the path to the 
			// local database, the contents of which will be listed.
			CmdLineParser.Option listReferencesOption = parser.addStringOption('l', "listReferences");
			
			
			
			/****************** ARGUMENT PARSING **************/
			
			// Parse the user-provided command line arguments, and catch any errors
			try
			{
				parser.parse(args);
			} 
			catch (CmdLineParser.OptionException e)
			{
				System.err.println(e.getMessage());
				printUsage();
				System.exit(1);
			}
			
			
			
			// The location of the MusicURI data source defaults to the loopback address.
			//String queryValue = (String) parser.getOptionValue(queryWS, "http://localhost:8080/axis/MusicURIWebSearch.jws?wsdl");
			
			String queryValue = (String) parser.getOptionValue(queryOption);
			String addReferenceValue = (String) parser.getOptionValue(addReferenceOption);
			String listReferencesValue = (String) parser.getOptionValue(listReferencesOption);
			
			
			
			
			/****************** PERFORM OPERATION **************/
			String audiofileValue = null;
			
			// At any time only one option value should be selected, and therefore be non-null, 
			// while the rest must be null. If any two option values are found to be non-null, 
			// at the same time, print usage and abort.
			if (	((queryValue != null) && (addReferenceValue != null)) || 
					((queryValue != null) && (listReferencesValue != null)) ||
					((addReferenceValue != null) && (listReferencesValue != null)) )
			{
				printUsage();
				System.exit(1);
			}
			else
			{
				/****************** QUERY OPERATION **************/
				// The requested operation was a query to a Web Service or local database file
				if (queryValue != null) 
				{
					// The flag determining whether to use the filename within search, defaults to false.
					Boolean usefilenameValue = (Boolean) parser.getOptionValue(usefilename, Boolean.FALSE);
					
					// There should remain only one free argument (not preceeded by a switch); 
					// the one specifying the filename of the audio file.
					if (parser.getRemainingArgs().length == 1) 
					{
						audiofileValue = parser.getRemainingArgs()[0];
					}
					if (!isValidAudioFile(audiofileValue))
					{
						printUsage();
						System.exit(1);
					}
					
					boolean done  = false;
					String musicURIDataSource = queryValue;
					
					// if the given MusicURI data source is a valid web service URL, query it.
					if (isValidWebServiceURL(musicURIDataSource))
					{
						// Set the done flag, to signal that the MusicURI data source 
						// has been identified as a Web Service
						done = true;
						try
						{
							queryWS(new URL(musicURIDataSource), new File(audiofileValue), usefilenameValue);
						} 
						catch (MalformedURLException e)
						{
							//impossible to reach here, the URL has already been checked
						}
					}
					
					// if the given MusicURI data source is a valid local database file, query it.
					if (!done && isValidDatabaseFile(musicURIDataSource))
					{
						// Set the done flag, to signal that the MusicURI data source 
						// has been identified as a local database file
						done = true;
						queryDB(new File (musicURIDataSource), new File(audiofileValue), usefilenameValue);
					}
					// if the given MusicURI data source is non of the above, abort.
					if (!done)
					{
						printUsage();
						System.exit(1);
					}
				}
				
				/****************** ADDITION OPERATION **************/
				//	The requested operation was a MusicURI reference addition to a 
				// Web Service or local database file
				if (addReferenceValue != null) 
				{			
					// There should remain only one free argument (not preceded by a switch); 
					// the one specifying the filename of the audio file.
					if (parser.getRemainingArgs().length == 1) 
					{
						audiofileValue = parser.getRemainingArgs()[0];
					}
					
					if (!isValidAudioFile(audiofileValue))
					{
						printUsage();
						System.exit(1);
					}
					
					boolean done  = false;
					String musicURIDataSource = addReferenceValue;
					
					// if the given MusicURI data source is a valid web service URL, query it.
					if (isValidWebServiceURL(musicURIDataSource))
					{
						// Set the done flag, to signal that the MusicURI data source 
						// has been identified as a Web Service
						done = true;
						try
						{
							addReferenceToWS(new URL(musicURIDataSource), new File(audiofileValue));
						} 
						catch (MalformedURLException e)
						{
							//impossible to reach here, the URL has already been checked
						}
					}
					
					
					// if the given MusicURI data source is a valid local database file, query it.
					if (!done && isValidDatabaseFile(musicURIDataSource))
					{
						// Set the done flag, to signal that the MusicURI data source 
						// has been identified as a local database file
						done = true;
						addReferenceToDB(new File (musicURIDataSource), new File(audiofileValue));
					}
					// if the given MusicURI data source is non of the above, abort.
					if (!done)
					{
						printUsage();
						System.exit(1);
					}
				}
				
				/****************** LIST OPERATION **************/
				//	The requested operation was a listing of all the MusicURI references 
				// that reside on a Web Service or local database file
				if (listReferencesValue != null) 
				{
					boolean done  = false;
					String musicURIDataSource = listReferencesValue;
					
					// if the given MusicURI data source is a valid web service URL, query it.
					if (isValidWebServiceURL(musicURIDataSource))
					{
						// Set the done flag, to signal that the MusicURI data source 
						// has been identified as a Web Service
						done = true;
						try
						{
							listWSReferences(new URL(musicURIDataSource));
						} 
						catch (MalformedURLException e)
						{
							//impossible to reach here, the URL has already been checked
						}
					}
										
					// if the given MusicURI data source is a valid local database file, query it.
					if (!done && isValidDatabaseFile(musicURIDataSource))
					{
						// Set the done flag, to signal that the MusicURI data source 
						// has been identified as a local database file
						done = true;
						listDBReferences(new File (musicURIDataSource));
					}
					// if the given MusicURI data source is non of the above, abort.
					if (!done)
					{
						printUsage();
						System.exit(1);
					}
				}
			}
	
		}
	}// end main method
	
	private static void printUsage()
	{
		System.out.println("USAGE: This MusicURI demo currently supports the following operations: ");
		System.out.println("");
		System.out.println("1) To query a MusicURI local database with a music item, and retrieve a URI:");
		System.out.println("   The -f flag signals if the filename should be considered as a hint in search.");
		System.out.println("   The -q switch is followed by the local database file's path");
		System.out.println("");
		System.out.println("   - java -jar MusicURI.jar [audiofile] [-f] [-q localDB]");
		System.out.println("  ");
		System.out.println("2) To add a reference music item into a local MusicURI database file ");
		System.out.println("   The -a switch is followed by the local database file's path]");
		System.out.println("   ");
		System.out.println("   - java -jar MusicURI.jar [audiofile] [-a localDB]");
		System.out.println("   ");
		System.out.println("3) To list all reference music items indexed inside a local MusicURI database");
		System.out.println("   The -l switch is followed by the local database file's path]");
		System.out.println("   ");
		System.out.println("   - java -jar MusicURI.jar [-l localDB]");
	}
	
	
	
	
	
	private static void queryWS(URL webServiceURL, File audioFile, Boolean usefilenameValue)
	{
		System.out.println("Starting MusicURI Demo: Query a MusicURI Web Service Data Source");
		
		String response = null;
		try 
		{
			//the object that will be our proxy
			stub = new MusicURIWebSearchSoapBindingStub(webServiceURL, new MusicURIWebSearchServiceLocator());
			
			if (audioFile.exists()) 
			{
				System.out.println("Creating query for  : " + audioFile.getName());
				String xmlAudioSignature = Toolset.createMPEG7Description(audioFile);
								
				String filename = audioFile.getName();
				
				System.out.println("Waiting for Web Service to return results...");
				
				// If the useFileName flag has been set to true, the filename should be provided 
				// and used within the search. If not, don't the provide the filename at all.
				if (usefilenameValue.booleanValue())
				{
					response = stub.performSearch(xmlAudioSignature, filename);
				}
				else
				{
					response = stub.performSearch(xmlAudioSignature, null);
				}
				System.out.println("Web Service response: ");
				System.out.println(response);
			}
		}
		catch (Exception e)
		{
			System.err.println("An error occured while querying the Web Service");
			e.printStackTrace();
		}

	}
	
	
	private static void addReferenceToWS(URL webServiceURL, File audioFile)
	{
		System.out.println("Starting MusicURI Demo: Add a MusicURI reference to a MusicURI Web Service Data Source");
		System.out.println("Not implemented yet");
	}
	
	private static void listWSReferences(URL webServiceURL)
	{
		System.out.println("Starting MusicURI Demo: List the MusicURI references of a MusicURI Web Service Data Source");
		
		
		
		
		String response = null;
		try 
		{
			//the object that will be our proxy
			stub = new MusicURIWebSearchSoapBindingStub(webServiceURL, new MusicURIWebSearchServiceLocator());
			
			System.out.println("Waiting for Web Service to return results...");
				
			response = stub.getMusicURIReferenceList();
			System.out.println("Web Service response: ");
			System.out.println(response);
		}
		catch (Exception ex) 
		{
			ex.printStackTrace();
			response = "An error occured while querying the Web Service";
		}
		
		
		

	}
	
	

	private static void queryDB(File databaseFile, File audioFile, Boolean usefilenameValue)
	{
		System.out.println("Starting MusicURI Demo: Query a local MusicURI Data Source");
		
		String databasePath = databaseFile.getParent() + "\\";
		String databaseFileName = databaseFile.getName();
		MusicURIDatabase db = new MusicURIDatabase (databasePath, databaseFileName);
		MusicURISearch engine = new MusicURISearch (db);
		
		try
		{
			ResultRankingList finalDistanceRankingList = engine.identify(new MusicURIQuery(audioFile), 
																		usefilenameValue.booleanValue(), 
																		usefilenameValue.booleanValue(), 
																		0.9f, //90% acceptable similarity rating 
																		usefilenameValue.booleanValue());
			
			if (finalDistanceRankingList.getSize() >= 1)
			{			
				Result theBestResult = finalDistanceRankingList.getResultAtIndex(0);
				double bestMatchDistance = theBestResult.distance;
				
				System.out.println("Matched with        : "	+ (db.getMusicURIReference(theBestResult.md5)).getLabel());
				System.out.println("Score               : " + (float) (100 - (100*(theBestResult.distance))) + "%"); 
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private static void addReferenceToDB(File databaseFile, File audioFile)
	{
		System.out.println("Starting MusicURI Demo: Add a MusicURI reference to a local MusicURI Data Source");
		
		String databasePath = databaseFile.getParent() + "\\";
		String databaseFileName = databaseFile.getName();
		MusicURIDatabase db = new MusicURIDatabase (databasePath, databaseFileName);
		
		boolean success = false;
		
		MusicURIReference newref = null;
		try
		{
			newref = new MusicURIReference(audioFile);
			success = db.addMusicURIReference(newref);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		if (success)
		{
			System.out.println("The MusicURI reference was added successfully.");
			System.out.println("The local MusicURI Data Source now holds " + db.getDbSize() + " references.");
		}
		else
			System.out.println("The MusicURI reference could not be added");
	}
	
	private static void listDBReferences(File databaseFile)
	{
		System.out.println("Starting MusicURI Demo: List the MusicURI references of a local MusicURI Data Source");
		
		String databasePath = databaseFile.getParent() + "\\";
		String databaseFileName = databaseFile.getName();
		MusicURIDatabase db = new MusicURIDatabase (databasePath, databaseFileName);
		
		String list = db.textFormattedSetOfMusicURIReferences();
		System.out.println(list);
	}
	
	
	
	
	
	public static boolean isValidAudioFile(String audiofileValue)
	{
		if (audiofileValue != null)
		{
			// Verify that the specified file exists, and it is not a directory
			File audioFile = new File(audiofileValue);
			if (!audioFile.exists())
			{
				System.err.println("The specified file does not exist");
				return false;
			}

			if (!audioFile.isFile())
			{
				System.err.println("The specified file is a directory");
				return false;
			}
			
			// Verify that the specified file is of a supported type
			if (!Toolset.isSupportedAudioFile(audioFile))
			{
				System.err.println("The specified file is not a supported audio file");
				return false;
			}
			return true;
		} 
		else
		{
			System.out.println("Unspecified audiofileValue");
			return false;
		}
	}
	
	
	public static boolean isValidWebServiceURL(String queryValue)
	{
		if (queryValue != null)
		{
			// Verify that the URL is well-formed and valid (both host and
			// resource exist)
			URL webServiceURL = null;
			try
			{
				webServiceURL = new URL(queryValue);
				HttpURLConnection urlConn = (HttpURLConnection) webServiceURL
						.openConnection();
				// Try to connect. If the URL is malformed throw a
				// MalformedURLException.
				urlConn.connect();

				// To take care of the chance of having an invalid, although
				// well-formed URL, check the HTTP Status Code. If it's not
				// 200 (HTTP_OK), throw an Exception.
				if (urlConn.getResponseCode() != 200)
					throw new Exception();

			} 
			catch (MalformedURLException e)
			{
				return false;
			} 
			catch (Exception e)
			{
				return false;
			}
			return true;
		} 
		else
			return false;
	}
	
	public static boolean isValidDatabaseFile(String musicURIDataSource)
	{
		if (musicURIDataSource != null)
		{
			// Verify that the specified file exists, and it is not a directory
			File databaseFile = new File(musicURIDataSource);
			if (!databaseFile.exists() || !databaseFile.isFile())
			{
				return false;
			}

			// Verify that the specified file is a valid MusicURI Hashmap flatfile
			// (ends with .db, and can be deserialized into a Java Hashmap object)
			if (!isDBFile(databaseFile))
			{
				System.err.println("The specified file is not a valid MusicURI database file");
				return false;
			}
//			else
//				System.out.println("file ends with .db");
//			try
//			{
//				HashMap object = new HashMap();
//				ObjectInputStream in = new ObjectInputStream(new FileInputStream(musicURIDataSource));
//				object = (HashMap) in.readObject();
//				System.out.println("file can be deserialized");
//			} 
//			catch (Exception e)
//			{
//				e.toString();
//				System.out.println("Can't load database file (" + musicURIDataSource + ")");
//				return false;
//			}
			return true;
		} 
		else
		{
			System.out.println("Unspecified musicURIDataSource");
			return false;
		}
	}
	
	/**
	 * Reports if the filename of the given file ends with ".db"
	 * @param file the file to check
	 * @return true if the filename ends with a ".db" extension, false otherwise
	 */
	public static boolean isDBFile(File file)
	{
		String fname = file.getName();
		String extension = fname.substring(fname.lastIndexOf('.') + 1);
		if (extension.equalsIgnoreCase("db")) return true;
		else return false;
	}
	

}
