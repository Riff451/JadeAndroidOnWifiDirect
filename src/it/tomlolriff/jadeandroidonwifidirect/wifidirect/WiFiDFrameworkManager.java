package it.tomlolriff.jadeandroidonwifidirect.wifidirect;

import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WiFiDirectDeviceState;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectConnectionState;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectDiscoveringState;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectEvent;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectFrameworkState;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectPeerListState;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectState;
import jade.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import android.content.Context;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Handler;
import android.os.Looper;

/**
 * Classe per instaurare una connessione con un Peer Jade mediante una connessione WiFi Direct.
 * La classe avvia subito la ricerca dei Peers quando il WiFi Direct è attivo
 */
public class WiFiDFrameworkManager {
	
	private Logger logger = Logger.getJADELogger(this.getClass().getName());
	
	private static WiFiDFrameworkManager instance = null;
	private BroadcastReceiverThread bRThread = null;
	
	private Context context = null;
	private WifiP2pManager manager = null;
	private Channel channel = null;
	private final WiFiDirectBroadcastReceiver receiver; // E' final perché ne registro solo uno!
	private List<WiFiDirectInfoListener> listeners;
	
	private WifiDirectInfo wifiDInfo = null;
	private WifiP2pDeviceList peerList = null;
	private WifiP2pDevice me = null;
	
	public static WiFiDFrameworkManager getInstance(Context context, WiFiDirectInfoListener listener){
		if(instance == null){
			instance = new WiFiDFrameworkManager(context, listener);
		}
		
		return instance;
	}
	
	private WiFiDFrameworkManager(Context context, WiFiDirectInfoListener listener){
		this.context = context;
		receiver = new WiFiDirectBroadcastReceiver(this);
		wifiDInfo = new WifiDirectInfo();
		listeners = new ArrayList<WiFiDirectInfoListener>();
		listeners.add(listener);
		// avvio il thread che gestirà UNO ALLA VOLTA mediante una coda i messaggi
        // inviati dal BroadcastReceiver
		bRThread = new BroadcastReceiverThread();
		bRThread.start();
	}
	
	/**
	 * Classe che processa i messaggi catturati dal BroadcastReceiver in un Worker Thread.
	 * La gestione avviene mediante una coda prelevando ed eseguendo un task alla volta.
	 * {@link http://mindtherobot.com/blog/159/android-guts-intro-to-loopers-and-handlers/}
	 */
	private class BroadcastReceiverThread extends Thread implements
		ChannelListener, PeerListListener, ConnectionInfoListener {
		
		private Handler handler = null;
		
		BroadcastReceiverThread(){
			super("BroadcastReceiver Thread");
		}
		
		// Richiamato quando sono disponibili info sulla connessione stabilita
		@Override
		public synchronized void onConnectionInfoAvailable(WifiP2pInfo info) {
			
			final WifiP2pInfo infoF = info;
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					if(!infoF.groupFormed) {
						logger.log(Level.SEVERE, "Wifi direct - Group not formed!");
						return;
					}
					notifyWifiDInfoListeners(WifiDirectEvent.CONNECTION_INFO_EVENT, infoF);
				}
			});
		}
		
		// Richiamato quando è disponibile una lista di P2P Peers
		@Override
		public synchronized void onPeersAvailable(WifiP2pDeviceList peers) {
			
			final WifiP2pDeviceList peersF = peers;
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					// Se la lista dei peers è vuota
					if(peersF.getDeviceList().isEmpty()) {
						logger.log(Level.INFO, "peer list listener - EMPTY");
						WiFiDFrameworkManager.this.peerList = null;
						wifiDInfo.setWifiDPeerListState(WifiDirectPeerListState.LIST_EMPTY);
						notifyWifiDInfoListeners(WifiDirectEvent.PEER_LIST_EVENT, null);
						// FIXME per sicurezza ricomincio la ricerca dei peers perché non so
						// se in questo caso la ricerca continua o si blocca (vd. discoverPeers())
						if(wifiDInfo.getWifiDState().equals(WifiDirectState.ON) &&
								wifiDInfo.getWifiDConnState().equals(WifiDirectConnectionState.DISCONNECTED)){
							startDiscover();
						}
						return;
					}
					// FIXME la ricerca continua anche quando ha trovato una lista non vuota???
					logger.log(Level.INFO, "Peers Available");
					WiFiDFrameworkManager.this.peerList = peersF;
					wifiDInfo.setWifiDPeerListState(WifiDirectPeerListState.LIST_AVAILABLE);
					notifyWifiDInfoListeners(WifiDirectEvent.PEER_LIST_EVENT, peersF);
				}
			});
		}

		@Override
		public synchronized void onChannelDisconnected() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void run(){
			try {
			    // preparing a looper on current thread     
			    // the current thread is being detected implicitly
			    Looper.prepare();
			 
			    // now, the handler will automatically bind to the
			    // Looper that is attached to the current thread
			    // You don't need to specify the Looper explicitly
			    handler = new Handler();
			     
			    // After the following line the thread will start
			    // running the message loop and will not normally
			    // exit the loop unless a problem happens or you
			    // quit() the looper (see below)
			    Looper.loop();
			  } catch (Throwable t) {
				  logger.log(Level.SEVERE, "WIFI Direct Framework Thread Error " + t.getMessage());
			  } 
		}
		
		/**
		 * Using the handler, post a Runnable that will quit() the Looper attached 
		 * to our BroadcastReceiver Thread; obviously, all previously queued tasks will 
		 * be executed before the loop gets the quit Runnable.
		 */
		synchronized void stopBReceiverThread() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					// This is guaranteed to run on the DownloadThread
					// so we can use myLooper() to get its looper
					Looper.myLooper().quit();
				}
			});
		}

		synchronized Handler getMyHandler() {
			return handler;
		}
		
	}
	
	/**
	 * Inizializza il framework WiFi Direct e lo avvia notificando l'esito dell'operazione
	 */
	public void startFramework(){
		// se il framework non era già stato avviato, mi avvio...
		if(wifiDInfo.getWifiDFrameworkState().equals(WifiDirectFrameworkState.STOP) ||
				wifiDInfo.getWifiDFrameworkState().equals(WifiDirectFrameworkState.INIT_ERROR)){
			if( initWifiDirect() ){
				wifiDInfo.setWifiDFrameworkState(WifiDirectFrameworkState.START);
			}
			else{
				wifiDInfo.resetAll();
				wifiDInfo.setWifiDFrameworkState(WifiDirectFrameworkState.INIT_ERROR);
			}
			notifyWifiDInfoListeners(WifiDirectEvent.FRAMEWORK_EVENT, null);
		}
		else{//...altrimenti notifico che sono già pronto
			//wifiDInfo.setWifiDFrameworkState(WifiDirectFrameworkState.START);
			notifyWifiDInfoListeners(WifiDirectEvent.FRAMEWORK_EVENT, null);
		}
	}
	
	/**
	 * Stoppa il framework WiFi Direct
	 */
	public void stopFramework(){
		if(wifiDInfo.getWifiDFrameworkState().equals(WifiDirectFrameworkState.START) ||
				wifiDInfo.getWifiDFrameworkState().equals(WifiDirectFrameworkState.INIT_ERROR)){
			stopWiFiDirect();
			wifiDInfo.resetAll(); // resetto completamente lo stato del framework
		}
		notifyWifiDInfoListeners(WifiDirectEvent.FRAMEWORK_EVENT, null);
		removeAllWiFiDirectInfoListeners();
	}
	
	/**
	 * Si connette al Peer device
	 * @param device {@link WifiP2pDevice} - p2p device a cui connettersi
	 */
	public void connectToPeer(WifiP2pDevice device){
		WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
		wifiP2pConfig.deviceAddress = device.deviceAddress;
		wifiP2pConfig.wps.setup = WpsInfo.PBC; // configurazione per la sicurezza
		// mi connetto
		manager.connect(channel, wifiP2pConfig, new ActionListener() {
			@Override
			public void onSuccess() {
				// questa situazione la gestisce il BroadcastReceiver con l'action WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
				logger.log(Level.INFO, "Wifi direct - Connection initiating success!");
			}

			@Override
			public void onFailure(int reason) {
				//p2punsupported = 1 
				//busy = 2
				//error = 0
				logger.log(Level.SEVERE, "Wifi direct - Connection initiating failure! - reason: " + reason);
			}
		});
	}
	
	/**
	 * Richiamato quando lo stato del Wifi Direct cambia
	 * @param wDState {@link WifiDirectState} - stato attuale del Wifi Direct
	 */
	void onWiFiDStateChanged(WifiDirectState wDState){
		
		wifiDInfo.setWifiDState(wDState);
		
		// WiFi Direct disabilitato...
		if(wifiDInfo.getWifiDState().equals(WifiDirectState.OFF)){
			peerList = null;
			//...ma ero connesso WARNING !!!
			if(wifiDInfo.getWifiDConnState().equals(WifiDirectConnectionState.CONNECTED)){
				// FIXME notifica qualcosa
			}
			//...e non ero connesso
			else{
				// FIXME forse non devo notificare niente qua
				
				/* METODO DISPONIBILE DALLE API 16 !!!
				// se stavo facendo una ricerca di peers per sicurezza la cancello
				manager.stopPeerDiscovery(channel, new ActionListener() {
					
					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onFailure(int reason) {
						// TODO Auto-generated method stub
						
					}
				});
				*/
			}
			// in ogni caso quando disabilito il direct devo avere questi stati
			wifiDInfo.setWifiDConnState(WifiDirectConnectionState.DISCONNECTED);
			wifiDInfo.setWifiDDiscoverState(WifiDirectDiscoveringState.STOP);
			wifiDInfo.setWifiDPeerListState(WifiDirectPeerListState.LIST_UNAVAILABLE);
			wifiDInfo.setWifiDDeviceState(WiFiDirectDeviceState.UNAVAILABLE);
			// BUGFIX: a volte Android non rilancia nessun evento quando disabilito il direct
			// quindi mi prendo cura di lanciare tutte le opportune notifiche
			notifyWifiDInfoListeners(WifiDirectEvent.CONNECTION_EVENT, null);
			notifyWifiDInfoListeners(WifiDirectEvent.DISCOVERING_EVENT, null);
			notifyWifiDInfoListeners(WifiDirectEvent.PEER_LIST_EVENT, null);
			notifyWifiDInfoListeners(WifiDirectEvent.THIS_DEVICE_EVENT, null);
		}
		// WiFi Direct abilitato...
		else {
			// BUGFIX: a volte Android non rilancia nessun evento quando abilito il direct
			// quindi mi prendo cura di lanciare tutte le opportune notifiche
			wifiDInfo.setWifiDConnState(WifiDirectConnectionState.DISCONNECTED);
			wifiDInfo.setWifiDPeerListState(WifiDirectPeerListState.LIST_UNAVAILABLE);
			wifiDInfo.setWifiDDeviceState(WiFiDirectDeviceState.AVAILABLE);
			notifyWifiDInfoListeners(WifiDirectEvent.THIS_DEVICE_EVENT, null);
			notifyWifiDInfoListeners(WifiDirectEvent.CONNECTION_EVENT, null);
			notifyWifiDInfoListeners(WifiDirectEvent.PEER_LIST_EVENT, null);
			startDiscover();
		}
		notifyWifiDInfoListeners(WifiDirectEvent.ONOFF_EVENT, null);
	}

	
	
	/**
	 * Richiamato quando la ricerca dei peers è iniziata (start) o ha avuto fine (stop)
	 * @param isDicoveryStarted boolean - booelano che indica se la ricerca dei Peers è iniziata (start) o ha avuto fine (stop)
	 */
	void onWiFiDDiscoveryChanged(boolean isDicoveryStarted){
		// Sull' LG l5 questa action non viene mai lanciata; sul nexus 7 invece sì
		if(isDicoveryStarted){
			logger.log(Level.INFO, "WIFI_P2P_DISCOVERY_CHANGED_ACTION - START");
		}
		else {
			logger.log(Level.INFO, "WIFI_P2P_DISCOVERY_CHANGED_ACTION - STOP");
		}
	}
	
	/**
	 * Richiamato quando la lista dei peers disponibili è cambiata
	 */
	void onAvailablePeerListChanged(){
		if(manager != null){
			manager.requestPeers(channel, bRThread);
		}
	}
	
	/**
	 * Richiamato quando la connessione wifi direct è cambiata (connessione o disconnessione)
	 * @param netInfo {@link NetworkInfo} - info sulla rete
	 */
	void onWiFiDConnectionChanged(NetworkInfo netInfo){
		if (netInfo.isConnected()) {
        	logger.log(Level.INFO, "WIFI P2P connected");
        	// la ricerca dei peer viene automaticamente interrotta quando ci si connette
        	// vedi documentazione della discoverPeers()
        	wifiDInfo.setWifiDDiscoverState(WifiDirectDiscoveringState.STOP);
        	wifiDInfo.setWifiDDeviceState(WiFiDirectDeviceState.CONNECTED);
        	wifiDInfo.setWifiDConnState(WifiDirectConnectionState.CONNECTED);
        	notifyWifiDInfoListeners(WifiDirectEvent.DISCOVERING_EVENT, null);
        	notifyWifiDInfoListeners(WifiDirectEvent.THIS_DEVICE_EVENT, null);
        	notifyWifiDInfoListeners(WifiDirectEvent.CONNECTION_EVENT, null);
        	// we are connected with the other device, request connection
            // info to find group owner IP.
        	if(manager != null){
        		manager.requestConnectionInfo(channel, bRThread);
        	}
        } else {
            // It's a disconnect
        	// in caso di disconnessione da una precedente connessione ricomincio la ricerca
        	if(wifiDInfo.getWifiDConnState().equals(WifiDirectConnectionState.CONNECTED) &&
        			wifiDInfo.getWifiDState().equals(WifiDirectState.ON)){
        		startDiscover(); 
        	}
        	logger.log(Level.INFO, "WIFI P2P disconnected");
        	// FIXME da capire il WiFiDirectDeviceState del dispositivo in questo caso;
        	// teoricamente dovrebbe essere onPersonalInfoChanged() a comunicare lo stato
        	wifiDInfo.setWifiDConnState(WifiDirectConnectionState.DISCONNECTED);
        	notifyWifiDInfoListeners(WifiDirectEvent.CONNECTION_EVENT, null);
        	
        }
	}
	
	/**
	 * Richiamato se le informazioni su questo dispositivo sono cambiate
	 * @param me {@link WifiP2pDevice} - questo device
	 */
	void onPersonalInfoChanged(WifiP2pDevice me){
		// salvo questo dispositivo
		this.me = me;
		// salvo lo stato di questo dispositivo
		// BUGFIX: Android mi dà lo stato del dispositivo AVAIBLE anche se il direct è spento
		if(wifiDInfo.getWifiDState().equals(WifiDirectState.OFF)){
			wifiDInfo.setWifiDDeviceState(WiFiDirectDeviceState.UNAVAILABLE);
		}
		else{
			// FIXME è fantastico vedere come, pur non essendo connessi, il Direct
			// notifica lo stato CONNECTED Io ci rinuncio! Per resettare lo stato
			// bisogna spegnere e riaccendere il Direct
			wifiDInfo.setWifiDDeviceState(WiFiDirectDeviceState.fromInt(me.status));
		}
		notifyWifiDInfoListeners(WifiDirectEvent.THIS_DEVICE_EVENT, null);
	}
	
	public WifiP2pDeviceList getPeerList() {
		// FIXME lanciare un'eccezione in caso di peeList null
		return peerList;
	}

	public WifiP2pDevice getMe() {
		if(me != null){
			return me;
		}
		// FIXME lanciare un'eccezione in caso di me null
		else { // altrimenti ritorno un WifiP2pDevice fittizio
			WifiP2pDevice fakeDevice = new WifiP2pDevice();
			fakeDevice.deviceName = "Dispositivo Sconosciuto";
			return fakeDevice;
		}
	}

	public WifiDirectInfo getWiFiDirectInfo(){
		// FIXME lanciare un'eccezione in caso di wifiDInfo null
		return wifiDInfo;
	}
	
	public void addWiFiDConnectionListener(WiFiDirectInfoListener l){
		listeners.add(l);
	}
	
	public void removeWiFiDConnectionListener(WiFiDirectInfoListener l){
		listeners.remove(l);
	}
	
	private void removeAllWiFiDirectInfoListeners(){
		listeners.clear();
	}
	
	/**
	 * Inizializza le proprietà utili ad interagire con il Wifi direct
	 * @return boolean che mi indica se l'inizializzazione del framework è andata a buon fine
	 */
	private boolean initWifiDirect() {
		
		IntentFilter broadcastReceiverIntentFilter = new IntentFilter();
		String [] actions = {WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION,
				WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION,
				WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION,
				WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION,
				WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION};
		setBReceiverIntentFiltersActions(broadcastReceiverIntentFilter, actions);
		
        // il metodo getSystemServive(String name) ritorna un gestore di un servizio di sistema
        // in questo caso ci interessa un gestore per il framework del Wi-fi Direct
        if( (manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE)) == null ){
        	return false;
        }
        // il manager ci ritorna, tramite il metodo initialize(), un canale per comunicare con il framework
        if( (channel = manager.initialize(context, context.getMainLooper(), bRThread)) == null ){
        	return false;
        }
        
        if(bRThread.getMyHandler() != null){
	        // registro il BroadcastReceiver ed in particolare gli comunico l'Handler
	        // associato al Thread che riceverà i messaggi
	        if( (context.registerReceiver(receiver, broadcastReceiverIntentFilter, null, bRThread.getMyHandler())) == null ){
	        	return false;
	        }
        } else { return false; }
        
        return (true);
	}
	
	/**
	 * Inizia la ricerca dei Peer e lo notifica
	 */
	private void startDiscover() {
		if(manager != null && (wifiDInfo.getWifiDDiscoverState().equals(WifiDirectDiscoveringState.STOP) ||
				wifiDInfo.getWifiDDiscoverState().equals(WifiDirectDiscoveringState.FAILURE))){
			// devo avviare la ricerca dei peers
			manager.discoverPeers(channel, new ActionListener(){
				
				// è un feedback relativo all'esito dell'avvio del discover peers,
				// non al risultato della ricerca
				@Override
				public void onSuccess() {
					wifiDInfo.setWifiDDiscoverState(WifiDirectDiscoveringState.START);
					notifyWifiDInfoListeners(WifiDirectEvent.DISCOVERING_EVENT, null);
					logger.log(Level.INFO, "WIFI P2P Discover Peers Initiating - Success");
				}

				@Override
				public void onFailure(int reason) {
					//p2punsupported = 1 
					//busy = 2
					//error = 0
					wifiDInfo.setWifiDDiscoverState(WifiDirectDiscoveringState.FAILURE);
					notifyWifiDInfoListeners(WifiDirectEvent.DISCOVERING_EVENT, reason);
					logger.log(Level.INFO, "WIFI P2P Discover Peers Initiating - Failed reason=" + reason);
				}
			});
		}
	}
	
	/**
	 * Stoppa la gestione del WiFi Direct:
	 * cancella eventuali connessioni e deregistra il Broadcast Receiver
	 */
	private void stopWiFiDirect(){
		
		manager.removeGroup(channel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
				
			}
		});
		
		manager.cancelConnect(channel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
			}
		});
		
		context.unregisterReceiver(receiver);
		bRThread.stopBReceiverThread();
	}
	
	private void setBReceiverIntentFiltersActions(IntentFilter intentFilter, String [] actions){
		for(String action : actions){
			intentFilter.addAction(action);
		}
	}
	
	private void notifyWifiDInfoListeners(WifiDirectEvent event, Object extraInfo){
		for(WiFiDirectInfoListener l : listeners){
			l.onWifiDInfoChange(event, wifiDInfo, extraInfo); // notifico tutti i listeners
		}
	}
}
