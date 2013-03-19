/**
 * 
 */
package it.univpm.deit.semedia.musicuri.core;

/**
* @author Dimitrios Kourtesis
*/
public interface SearchStrategy
{
	/**
	 * A perfect audio fingerprinting system would correctly match a query with
	 * a known, previously indexed reference, 100% of the time, and reject an
	 * unknown query, 100% of the time. However, audio fingerprints can be
	 * extracted from a variety of audio signals (FM broadcasts, GSM telephony,
	 * internet streams, mp3 files) which are captured under uncontrollable
	 * conditions (cabling, speaker-microphone chains, AD conversions), and can
	 * therefore be of questionable perceptual audio quality. This in turn leads
	 * to matching of questionable accuracy. The overall accuracy of an audio
	 * fingerprinting system is assessed by its tendency to produce either a
	 * "false match" (false acceptance or false positive) or a "false non-match"
	 * (false rejection or false negative). The point at which these two rates
	 * intersect is called the equal error rate (EER). This indicates the rate
	 * of errors (errors/attempts) occurring when a system's decision threshold
	 * is set so that the number of false rejections will be approximately equal
	 * to the number of false acceptances. The lower the equal error rate value,
	 * the higher the accuracy of the system.
	 * 
	 * The false acceptance rate, or FAR, is the measure of the likelihood that
	 * the system will falsely accept a query as matching to a known reference,
	 * while in fact the music item that the query actually belongs to, is
	 * unknown to the system. Ie states the probability that the some irrelevant
	 * reference will be wrongly selected. For example matching a sample from a
	 * Metallica song, to a song by Madonna, while the database does not contain
	 * any songs by Metallica. A system's FIR is stated as the ratio of the
	 * number of false identifications divided by the number of identification
	 * attempts.
	 * 
	 * The false rejection rate, or FRR, is the measure of the likelihood that
	 * the system will falsely reject a query as not matching to any known
	 * reference, while the music item that the query actually belongs to, is
	 * known to the system. Ie states the probability that the actual reference
	 * will be wrongly rejected. For example refuse to match a sample from a
	 * Metallica song, while the database contains every song ever produced by
	 * Metallica. A system's FIR is stated as the ratio of the number of false
	 * identifications divided by the number of identification attempts.
	 * 
	 * This interface class is used to realize different implementations of
	 * MusicURI search, reflecting the system's policy with regard to matching.
	 * One of the questions is "what is the fine line separating acceptable from
	 * non-acceptable matches?". In general, raising the decision threshold
	 * leads to having less false acceptance or false-positive matches, but also
	 * leads to having more false rejection or false-negative matches, and vice
	 * versa. In a highly demanding application where the cost of a erroneous
	 * identification could be high, system policy might prefer false-rejections
	 * over false acceptances, and thus raise the decision threshold. In a
	 * setting where the MusicURI service is provided at a "best-effort" basis,
	 * and the cost of a erroneous identification is small, system policy might
	 * favor a false-acceptance matching, in order not to over-reject queries,
	 * and thereby under-utilize its reference database.
	 * 
	 * 
	 * To fine tune the system and obtain the ERR, the FAR and FRR must 
	 * converge to an equal value:
	 * 
	 * int numberOfQueries = 100; 
	 * float decision_threshold = 0.0f; 
	 * int falseAcceptances = 100; 
	 * int falseRejections = 0;
	 * 
	 * 
	 * while (falseAcceptances > falseRejections) 
	 * {
	 *  decision_threshold += 0.01f;
	 * 	stats = runQueriesBatch(); 
	 * 	falseAcceptances = stats.getFalseAcceptances(); 
	 * 	falseRejections = stats.getFalseRejections(); 
	 * }
	 * 
	 * float EER = falseAcceptances / numberOfQueries;
	 * 
	 * Since it is true that the lower the equal error rate value, the higher
	 * the accuracy of the system, we will use ERR as a benchmark to determine
	 * the efficiency of different search strategies.
	 * 
	 */

}
