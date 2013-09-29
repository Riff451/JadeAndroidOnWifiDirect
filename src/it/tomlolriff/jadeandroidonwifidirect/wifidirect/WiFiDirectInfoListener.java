package it.tomlolriff.jadeandroidonwifidirect.wifidirect;

import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectEvent;

/**
 * Interfaccia listener per la gestione di eventi WiFi Direct
 * @author riff451 - TomZ85 - Lollo
 *
 */
public interface WiFiDirectInfoListener {
	/**
	 * Notifica un evento WiFi Direct
	 * @param event {@link WifiDirectEvent} - evento WiFi Direct accaduto
	 * @param info {@link WifiDirectInfo} - info sullo stato WiFi Direct a seguito di event
	 * @param extraInfo {@link Object} - info extra legate a particolari eventi ; può essere null
	 */
	void onWifiDInfoChange(WifiDirectEvent event, WifiDirectInfo info, Object extraInfo);
}
