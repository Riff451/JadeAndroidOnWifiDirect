package it.tomlolriff.jadeandroidonwifidirect.wifidirect;

import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WiFiDirectDeviceState;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectConnectionState;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectDiscoveringState;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectFrameworkState;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectPeerListState;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectState;

/**
 * Classe che rappresenta gli stati attuali del WiFi Direct
 * @author riff451 - TomZ85 - Lollo
 */
public class WifiDirectInfo {
	
	private WifiDirectState wifiDState = WifiDirectState.OFF;
	private WifiDirectConnectionState wifiDConnState = WifiDirectConnectionState.DISCONNECTED;
	private WifiDirectPeerListState wifiDPeerListState = WifiDirectPeerListState.LIST_UNAVAILABLE;
	private WifiDirectFrameworkState wifiDFrameworkState = WifiDirectFrameworkState.STOP;
	private WifiDirectDiscoveringState wifiDDiscoverState = WifiDirectDiscoveringState.STOP;
	private WiFiDirectDeviceState wifiDDeviceState = WiFiDirectDeviceState.UNKNOWN;

	public WifiDirectState getWifiDState() {
		return wifiDState;
	}

	void setWifiDState(WifiDirectState wifiDState) {
		this.wifiDState = wifiDState;
	}

	public WifiDirectConnectionState getWifiDConnState() {
		return wifiDConnState;
	}

	void setWifiDConnState(WifiDirectConnectionState wifiDConnState) {
		this.wifiDConnState = wifiDConnState;
	}

	public WifiDirectPeerListState getWifiDPeerListState() {
		return wifiDPeerListState;
	}

	void setWifiDPeerListState(WifiDirectPeerListState wifiDPeerListState) {
		this.wifiDPeerListState = wifiDPeerListState;
	}

	public WifiDirectFrameworkState getWifiDFrameworkState() {
		return wifiDFrameworkState;
	}

	void setWifiDFrameworkState(WifiDirectFrameworkState wifiDFrameworkState) {
		this.wifiDFrameworkState = wifiDFrameworkState;
	}

	public WifiDirectDiscoveringState getWifiDDiscoverState() {
		return wifiDDiscoverState;
	}

	void setWifiDDiscoverState(WifiDirectDiscoveringState wifiDDiscoverState) {
		this.wifiDDiscoverState = wifiDDiscoverState;
	}
	
	public WiFiDirectDeviceState getWifiDDeviceState() {
		return wifiDDeviceState;
	}

	void setWifiDDeviceState(WiFiDirectDeviceState wifiDDeviceState) {
		this.wifiDDeviceState = wifiDDeviceState;
	}
	
	/**
	 * Imposta tutti gli stati al loro valore di default iniziale
	 */
	void resetAll(){
		wifiDState = WifiDirectState.OFF;
		wifiDConnState = WifiDirectConnectionState.DISCONNECTED;
		wifiDPeerListState = WifiDirectPeerListState.LIST_UNAVAILABLE;
		wifiDFrameworkState = WifiDirectFrameworkState.STOP;
		wifiDDiscoverState = WifiDirectDiscoveringState.STOP;
		wifiDDeviceState = WiFiDirectDeviceState.UNKNOWN;
	}

}
