package it.tomlolriff.jadeandroidonwifidirect.wifidirect.types;

import android.net.wifi.p2p.WifiP2pDeviceList;

/**
 * Enum che rappresenta un particolare evento WiFi Direct
 * @author riff451 - TomZ85 - Lollo
 *
 */
public enum WifiDirectEvent {
	/**
	 * Evento WiFi Direct accesso o spento
	 */
	ONOFF_EVENT,
	/**
	 * Evento del framework di gestione del WiFi Direct
	 */
	FRAMEWORK_EVENT,
	/**
	 * Evento legato al discovering dei P2P Peer
	 */
	DISCOVERING_EVENT,
	/**
	 * Evento legato alla connessione WiFi Direct
	 */
	CONNECTION_EVENT,
	/**
	 * Evento legato allo stato di questo WiFi P2P Device.
	 */
	THIS_DEVICE_EVENT,
	/**
	 * Evento legato alla lista di P2P Peers.
	 * Assieme all'evento, in caso di LIST_AVAILABLE, è resituita anche una {@link WifiP2pDeviceList}
	 */
	PEER_LIST_EVENT,
	/**
	 * Evento legato alle info sull'attuale connessione WiFi Direct
	 */
	CONNECTION_INFO_EVENT;
}
