package it.univpm.deit.semedia.musicuri.core;

import java.io.File;
import java.net.URL;

/**
* @author Dimitrios Kourtesis
*/
public interface MusicURIDataSource
{
	public URL getDataSourceLocation();
	public void index(File musicItemToIndex);
	public void identify(File musicItemToIdentify);
	public void listMusicURIReferences();
}
