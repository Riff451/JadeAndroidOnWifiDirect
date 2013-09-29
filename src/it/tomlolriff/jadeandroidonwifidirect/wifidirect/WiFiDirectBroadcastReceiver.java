package it.tomlolriff.jadeandroidonwifidirect.wifidirect;

import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectState;
import jade.util.Logger;

import java.util.logging.Level;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * 
 * {@link http://www.vogella.com/articles/AndroidBroadcastReceiver/article.html}
 * @author tomz
 *
 */
class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
	private Logger logger = Logger.getJADELogger(this.getClass().getName());
    private WiFiDFrameworkManager wifiDFrameManager;

	WiFiDirectBroadcastReceiver(WiFiDFrameworkManager wifiDConnManager) {
		super();
		this.wifiDFrameManager = wifiDConnManager;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
        String action = intent.getAction(); // prendiamo l'azione dell'Intent che ci è arrivato

        // se lo stato del wi-fi p2p è cambiato
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // UI update to indicate wifi p2p status.
        	// verifichiamo se il wi-fi p2p è abilitato o disabilitato
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            logger.log(Level.INFO, "WIFI P2P state changed - " + state);

            // Wifi Direct mode is enabled/disabled
            //activity.setWifiDirectState(WifiDirectState.fromBoolean(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED));
            // verifica l'evento e che il wifi direct state non sia già on
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
            	wifiDFrameManager.onWiFiDStateChanged(WifiDirectState.fromBoolean(true));
            }
            else{
            	wifiDFrameManager.onWiFiDStateChanged(WifiDirectState.fromBoolean(false));
            }
        } else if(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
        	// Sull' LG l5 questa action non viene mai lanciata; sul nexus 7 invece sì
        	// e comunque non è un'info molto affidabile, perché a quanto pare
        	// notifica un WIFI_P2P_DISCOVERY_STARTED anche quando il WiFi Direct è spento!
        	logger.log(Level.INFO, "WIFI Discovery P2P state changed");
        	int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
        	if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED){
        		wifiDFrameManager.onWiFiDDiscoveryChanged(true);
        	}
        	else if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED){
        		wifiDFrameManager.onWiFiDDiscoveryChanged(false);
        	}
        // se la lista di peers raggiungibili è cambiata
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
        	logger.log(Level.INFO, "WIFI P2P peers changed");
        	wifiDFrameManager.onAvailablePeerListChanged();
        // se lo stato della connessione wi-fi p2p è cambiato
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
        	logger.log(Level.INFO, "WIFI P2P connection changed");
        	NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        	wifiDFrameManager.onWiFiDConnectionChanged(networkInfo);
        // se i dettagli di questo dispositivo sono cambiati
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
        	logger.log(Level.INFO, "WIFI P2P this device changed");
        	wifiDFrameManager.onPersonalInfoChanged((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
	}
}
