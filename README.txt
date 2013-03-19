

******************************************************************************
************************ Getting started with MusicURI ***********************
******************************************************************************


I have put together some instructions to demonstrate the functionality that 
MusicURI offers at its current point of development. To get an idea of what is 
offered, please follow the instructions below.



******************************************************************************
Step 1. Install the MusicURI package
******************************************************************************

1. Extract the contents of 'dependencies.zip'. 

2. Add the .jar files to your CLASSPATH. One way to do this is to place them 
inside the extension directory of your Java Runtime Environment installation 
(JAVA_HOME/jre/lib/ext/). 

3. Place the MusicURI.jar and MusicURIReferences.db somewhere in 
your system. The MusicURI.jar contains the executable you will use in the next 
step, and the MusicURIReferences.db is the actual database flat file. 



******************************************************************************
Step 2. Use the MusicURI package
******************************************************************************

Execute the Music.jar as described below. The entry point for the MusicURI 
jar file is a demo application. It currently supports only local MusicURI data 
sources (ie working with local database files). Support for MusicURI data 
sources that are exposed as Web Services has been removed for now, but will 
be provided again shortly (i am currently doing some refactoring and redesign).

The demo application intends to practically demonstrate the basic funcionality 
that MusicURI offers, which is to enable the mapping between a piece of music 
and a unique URI. The following operations are currently offered :

a) Query a MusicURI local database with a music item, and retrieve a URI 
b) Add a reference music item to a local MusicURI database 
c) List all reference music items inside a local MusicURI database

Explanation:

a) Query the local MusicURI database file at the specified path, with the
given audio file, to retrieve a URI. The -q switch is accompanied by the path 
to the local database, and the -f flag (optional) signals if the filename 
should be utilized as a reliable hint within search. 

	Format:  java -jar MusicURI.jar [<audiofile>] [<-q> <DBfile>] [<-f>]
	example: "c:\demo\test.mp3" -q "c:\demo\MusicURIReferences.db" -f


b) Add, to the local MusicURI database file at the specified path, the given 
audio file. The -a switch is accompanied by the path to the local database, 
into which the music item will be indexed. 
	

	Format:  java -jar MusicURI.jar [<audiofile>] [<-a> <DBfile>] 
	example: "c:\demo\test.mp3" -a "c:\demo\MusicURIReferences.db"


c) List the URIs of all music items indexed in the local MusicURI database 
file, at the specified path. 

	Format:  java -jar MusicURI.jar [<-l> <databasefile>]
	Example: -l "c:\demo\MusicURIReferences.db"


Before proceeding with any of the aforementioned operations, the
application verifies that any files that have been specified exist, 
and are valid.

For query operations, the user has to specify Where, What and How to identify. 
I.e. the user must specify the URL where the MusicURI data source resides, 
the audio file containing the unknown piece of music, and whether the system 
should utilize the provided filename as a reliable hint within the search, or 
not (default is false). The filename should be taken into consideration only 
if the user is absolutely confident that it does not contain any misleading 
information about the artist and title of the respective piece of music. For 
example, this option could be safely used for a filename of the form:
<ARTIST> <SEPARATOR> <TITLE>.<EXTENSION>

@param Where: URL specifying the location of the MusicURI data source
@param What : Audio file containing the piece of music to use as a query
@param How  : Flag determining whether to utilize the filename in search


The MusicURIReferences.db database file is provided to be used as a starting 
point. It contains 14 MusicURIReference objects corresponding to 14 reference 
music items from the "Buena Vista Social Club" album that was released in 1997. 
The "test.mp3" file is a 6-second long excerpt from the album's 9th track.


******************************************************************************
November 15, 2005
Dimitrios Kourtesis 
d.kourtesis at gmail dot com
******************************************************************************