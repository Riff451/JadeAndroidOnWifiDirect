package it.tomlolriff.jadeandroidonwifidirect.wifidirect.types;

import android.net.wifi.p2p.WifiP2pDeviceList;

/**
 * Enum per gli stati di una lista di P2P Peers
 * @author riff451 - TomZ85 - Lollo
 *
 */
public enum WifiDirectPeerListState {
	/**
	 * Lista disponibile.
	 * E' restituita una {@link WifiP2pDeviceList}
	 */
	LIST_AVAILABLE,
	/**
	 * Lista non disponibile
	 */
	LIST_UNAVAILABLE,
	/**
	 * Lista vuota
	 */
	LIST_EMPTY;
}
