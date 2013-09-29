package it.tomlolriff.jadeandroidonwifidirect.util;

/**
 * Classe di utilità per le stringhe
 * @author riff451 - TomZ85 - Lollo
 */
public class StringHelper {

	/**
	 * Elimina la barra che viene aggiunta all'inizio della stringa dell'indirizzo IPv4
	 * @param mainContainerHostAddressS {@link String} - stringa dell'indirizzo dal ripulire
	 * @return {@link String} - stringa dell'indirizzo ripulita
	 */
	public static String fixHostAddress(String mainContainerHostAddressS) {
		// se ci sono errori nella stringa la fixa....
		if(mainContainerHostAddressS.startsWith("/")) {
			return mainContainerHostAddressS.substring(1, mainContainerHostAddressS.length());
		}
		return mainContainerHostAddressS;
	}

}
