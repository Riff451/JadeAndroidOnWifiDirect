package it.tomlolriff.jadeandroidonwifidirect.wifidirect.types;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Enum per gli stati di un dispositivo WiFi Direct
 * @author riff451 - TomZ85 - Lollo
 *
 */
public enum WiFiDirectDeviceState {
	/**
	 * Dispositivo disponibile
	 */
	AVAILABLE,
	/**
	 * Dispositivo invitato a connettersi
	 */
	INVITED,
	/**
	 * Dispositivo Connesso
	 */
	CONNECTED,
	/**
	 * 
	 */
	FAILED,
	/**
	 * Dispositivo non disponibile
	 */
	UNAVAILABLE,
	/**
	 * Stato dispositivo sconosciuto
	 */
	UNKNOWN;
	
	public static WiFiDirectDeviceState fromInt(int status){
		switch (status) {
	        case WifiP2pDevice.AVAILABLE:
	            return WiFiDirectDeviceState.AVAILABLE;
	        case WifiP2pDevice.INVITED:
	            return WiFiDirectDeviceState.INVITED;
	        case WifiP2pDevice.CONNECTED:
	            return WiFiDirectDeviceState.CONNECTED;
	        case WifiP2pDevice.FAILED:
	            return WiFiDirectDeviceState.FAILED;
	        case WifiP2pDevice.UNAVAILABLE:
	            return WiFiDirectDeviceState.UNAVAILABLE;
	        default:
	            return WiFiDirectDeviceState.UNKNOWN;
		}
	}
}
