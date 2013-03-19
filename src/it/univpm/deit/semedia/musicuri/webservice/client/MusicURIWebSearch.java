/**
 * MusicURIWebSearch.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package it.univpm.deit.semedia.musicuri.webservice.client;

public interface MusicURIWebSearch extends java.rmi.Remote
{
	public java.lang.String getMusicURIReferenceList() throws java.rmi.RemoteException;
    public int getNumOfMusicURIReferences() throws java.rmi.RemoteException;
    public java.lang.String performSearch(java.lang.String xmlAudioSignature, java.lang.String filename) throws java.rmi.RemoteException;
}
