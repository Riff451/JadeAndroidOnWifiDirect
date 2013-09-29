package it.tomlolriff.jadeandroidonwifidirect.wifidirect.types;

/**
 * Enum per gli stati del WiFi Direct
 * @author riff451 - TomZ85 - Lollo
 *
 */
public enum WifiDirectState {
	/**
	 * WiFi Direct acceso
	 */
	ON,
	/**
	 * WiFi Direct spento
	 */
	OFF;
	
	/**
	 * Restituisce un {@link WifiDirectState} da un boolean
	 * @param enabled {@link boolean} - booleano per indicare lo stato acceso (true) o spento (false)
	 * @return {@link WifiDirectState}
	 */
	public static WifiDirectState fromBoolean(boolean enabled) {
		if(enabled) {
			return WifiDirectState.ON;
		} else {
			return WifiDirectState.OFF;
		}
	}
}
