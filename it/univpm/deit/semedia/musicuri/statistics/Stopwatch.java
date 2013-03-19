/*
 Copyright (c) 2005, John O'Hanley, Canada
 
 Code for this class was taken from http://www.javapractices.com/
 */

package it.univpm.deit.semedia.musicuri.statistics;




/**
 * Allows timing of the execution of any block of code.
 */

public final class Stopwatch 
{
	
	/**
	 * Starts the stopwatch.
	 * @throws IllegalStateException if the stopwatch is already running.
	 */
	public void start(){
		if ( fIsRunning ) {
			throw new IllegalStateException("Must stop before calling start again.");
		}
		//reset both start and stop
		fStart = System.currentTimeMillis();
		fStop = 0;
		fIsRunning = true;
		fHasBeenUsedOnce = true;
	}
	
	/**
	 * Stops the stopwatch.
	 * @throws IllegalStateException if the stopwatch is not already running.
	 */
	public void stop() {
		if ( !fIsRunning ) {
			throw new IllegalStateException("Cannot stop if not currently running.");
		}
		fStop = System.currentTimeMillis();
		fIsRunning = false;
	}
	
	/**
	 * Expresses the "reading" on the stopwatch.
	 * @throws IllegalStateException if the Stopwatch has never been used,
	 * or if the stopwatch is still running.
	 */
	public String toString() {
		validateIsReadable();
		StringBuffer result = new StringBuffer();
		result.append(fStop - fStart);
		result.append(" ms");
		return result.toString();
	}
	
	/**
	 * Expresses the "reading" on the stopwatch as a numeric type.
	 * @throws IllegalStateException if the Stopwatch has never been used,
	 * or if the stopwatch is still running.
	 */
	public long toValue() {
		validateIsReadable();
		return fStop - fStart;
	}
	
	// PRIVATE ////
	private long fStart;
	private long fStop;
	
	private boolean fIsRunning;
	private boolean fHasBeenUsedOnce;
	
	/**
	 * @throws IllegalStateException if the watch has never been started,
	 * or if the watch is still running. 
	 */
	private void validateIsReadable() {
		if ( fIsRunning ) {
			String message = "Cannot read a stopwatch which is still running.";
			throw new IllegalStateException(message);
		}
		if ( !fHasBeenUsedOnce ) {
			String message = "Cannot read a stopwatch which has never been started.";
			throw new IllegalStateException(message);
		}
	}
}

