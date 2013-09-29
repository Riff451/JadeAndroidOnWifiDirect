package it.tomlolriff.jadeandroidonwifidirect.wifidirect.types;

/**
 * Enum per gli stati del WiFi Direct Discovering
 * @author riff451 - TomZ85 - Lollo
 *
 */
public enum WifiDirectDiscoveringState {
	/**
	 * Discovering in corso
	 */
	START,
	/**
	 * Discovering concluso
	 */
	STOP,
	/**
	 * Discovering interrotto per qualche problema
	 */
	FAILURE;
}
