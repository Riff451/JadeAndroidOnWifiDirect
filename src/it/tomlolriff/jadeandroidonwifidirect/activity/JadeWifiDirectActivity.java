package it.tomlolriff.jadeandroidonwifidirect.activity;

import it.tomlolriff.jadeandroidonwifidirect.jade.JadeUIListener;
import it.tomlolriff.jadeandroidonwifidirect.jade.exceptions.AgentContainerNotStartedException;
import it.tomlolriff.jadeandroidonwifidirect.jade.types.AgentInfo;
import it.tomlolriff.jadeandroidonwifidirect.services.JadeWifiDirectService;
import it.tomlolriff.jadeandroidonwifidirect.services.JadeWifiDirectServiceBinder;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.WiFiDirectInfoUIListener;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.WifiDirectInfo;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectEvent;
import jade.util.Logger;
import jade.wrapper.StaleProxyException;

import java.util.logging.Level;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

/**
 * Attività astratta che è possibile estendere.
 * 
 * @author riff451 - TomZ85 - Lollo
 */
public abstract class JadeWifiDirectActivity extends FragmentActivity {
	protected Logger logger = Logger.getJADELogger(this.getClass().getName());
	/**
	 * Listener per aggiornare la UI all'arrivo degli eventi WiFi Direct
	 */
	private MyWiFiDirectInfoUIListener wifiDirectUIListener = null;
	/**
	 * Listener per aggiornare la UI all'arrivo degli eventi Jade
	 */
	private MyJadeUIListener jadeUIListener = null;
	/**
	 * Binder al JadeWifiDirectService
	 */
	private JadeWifiDirectServiceBinder serviceBinder;
	/**
	 * Indica se ho eseguito il bind al Service
	 */
	private boolean isBound = false;
	/**
	 * Informazioni sullo stato del WiFi Direct
	 */
	private WifiDirectInfo wifiDInfo = null;

	/**
	 * ServiceConnection al JadeWifiDirectService
	 */
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			logger.log(Level.INFO, "onServiceDisconnected - Activity");
			isBound = false;
			onServiceConnectionDisconnected();
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			logger.log(Level.INFO, "onServiceConnected - Activity");
			serviceBinder = (JadeWifiDirectServiceBinder)service;
			serviceBinder.addWiFiDirectInfoListener(JadeWifiDirectActivity.this.wifiDirectUIListener);
			serviceBinder.addJadeListener(JadeWifiDirectActivity.this.jadeUIListener);
			serviceBinder.startWiFiDFramework();
			wifiDInfo = serviceBinder.getWiFiDirectInfo();
			isBound = true;
			onServiceConnectionConnected(wifiDInfo);
		}
	};
	
	// Avvio il JadeWifiDirectService
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// i listener si occupano di instanziare un Handler associato al Main Thread
		jadeUIListener = new MyJadeUIListener();
		wifiDirectUIListener = new MyWiFiDirectInfoUIListener();
	}
	
	// Eseguo il bind al JadeWifiDirectService
	@Override
	protected void onResume() {
		super.onResume();
		logger.log(Level.INFO, "startService() Activity");
		// E' necessario fare qui lo startService() altrimenti se spegnessi il Service
		// tramite la notification e poi resumassi l'app avvierei il Service solo
		// tramite binding. Quindi al successivo unBind() il Service morirebbe.
		startService(new Intent(getApplicationContext(), JadeWifiDirectService.class));
		// il bind lo faccio nella onResume() quando sono sicuro che la UI è pronta
		doBindService();
    }
	
    @Override
    protected void onPause() {
        super.onPause();
        doUnBindService();
        logger.log(Level.INFO, "onPause() Activity");
    }
    
    // Eseguo l'unbind al JadeWifiDirectService
    @Override
    protected void onStop(){ 
    	super.onStop();
        logger.log(Level.INFO, "onStop() Activity");
    }

    /**
     * Esegue il bind al JadeWifiDirectService solo se necessario
     */
    private void doBindService(){
    	if(!isBound){
    		logger.log(Level.INFO, "bindService() Activity");
			bindService(new Intent(getApplicationContext(), JadeWifiDirectService.class),
					serviceConnection, Context.BIND_AUTO_CREATE);
    	}
    }
    
    /**
     * Esegue l'unBind al JadeWifiDirectService e deregistra i listener
     */
    private void doUnBindService(){ 
    	if(isBound){
    		logger.log(Level.INFO, "unbindService() Activity");
    		serviceBinder.removeWiFiDirectFrameworkListener(this.wifiDirectUIListener);
    		serviceBinder.removeJadeListener(this.jadeUIListener);
         	unbindService(serviceConnection);
    		isBound = false;
    	}
    }
    
    @Override
    protected void onDestroy(){
    	super.onDestroy();
    }
    
    /**
     * Connessione ad un Peer su piattafomra WiFi Direct + Jade
     * @param device {@link WifiP2pDevice} - peer a cui connettersi
     */
    protected final void connectToPeer(WifiP2pDevice device) {
    	if(isBound){
    		//TODO Occhio!! Diamo per scontato che jade, in caso di piattaforma avviata, dia errore in quanto
    		// la porta è già occupata
    		serviceBinder.startNewJadePlatform(device);
    	}
    }

	/**
	 * Indica se la piattaforma per eseguire le operazioni su jade è pronta
	 * @return <code>true</code> se la piattaforma per eseguire operazioni su jade è pronta, <code>false</code> altrimenti
	 */
	protected final boolean isPlatformReady() {
		if(this.serviceBinder == null) return false;

		return this.serviceBinder.platformReady();
	}

    /**
     * Crea ed esegue un nuovo agente Jade
     * @param agentName {@link String} - nome del nuovo agente
     * @param className {@link String} - nome della classe che implementa l'agente
     * @param args {@link Object}[] - ulteriori argomenti da passare all'agente; può essere null
     */
    protected final void createAndStartAgent(String agentName, String className, Object[] args) {
    	if(isBound){
    		this.serviceBinder.createAgent(agentName, className, args);
    	}
    }
    
	/**
	 * Permette di recuperare per l'agente <code>agentName</code>, l'interfaccia <code>theInterface</code>
	 * @param agentName il nome dell'agente
	 * @param theInterface l'interfaccia richiesta
	 * @return l'interfaccia <code>theInterface</code> per l'agente <code>agentName</code>
	 * @throws StaleProxyException
	 * @throws AgentContainerNotStartedException
	 */
    protected final <T> T getO2AInterface(String agentName, Class<T> theInterface) throws StaleProxyException, AgentContainerNotStartedException {
    	if(isBound) {
    		return this.serviceBinder.getO2AInterface(agentName, theInterface);
    	} else {
    		//FIXME eccezione
    		return null;
    	}
    }

    /**
     * Classe che implementa il listener per la gestione degli eventi WiFi Direct
     * che devono modificare la UI. 
     */
    private class MyWiFiDirectInfoUIListener extends WiFiDirectInfoUIListener {

		@Override
		protected void doOnWifiDInfoChange(WifiDirectEvent event,
				WifiDirectInfo info, Object extraInfo) {
			
			JadeWifiDirectActivity.this.doOnWifiDInfoChange(event, info, extraInfo);
		}
    }

    /**
     * Classe che implementa il listener per la gestione degli eventi Jade che
     * devono modificare la UI.
     */
    private class MyJadeUIListener extends JadeUIListener{
    	
    	// Richiamato quando un Main Container Jade è stato creato con successo
		@Override
		protected void doOnMainContainerSuccessfullyStarted() {
			JadeWifiDirectActivity.this.doOnMainContainerSuccessfullyStarted();
		}
		
		// Richiamato quando un Main Container Jade non è stato creato correttamente
		@Override
		protected void doOnMainContainerFailureStarted(Throwable throwable) {
			JadeWifiDirectActivity.this.doOnMainContainerFailureStarted(throwable);
		}
		
		// Richiamato quando un Container Jade è stato creato con successo
		@Override
		protected void doOnContainerSuccessfullyStarted() {
			JadeWifiDirectActivity.this.doOnContainerSuccessfullyStarted();
		}
		
		// Richiamato quando un Container Jade non è stato creato correttamente
		@Override
		protected void doOnContainerFailureStarted(Throwable throwable) {
			JadeWifiDirectActivity.this.doOnContainerFailureStarted(throwable);
		}
		
		// Richiamato quando un Agente Jade è stato creato ed avviato con successo
		@Override
		protected void doOnAgentSuccessfullyStarted(AgentInfo agentInfo) {
			JadeWifiDirectActivity.this.doOnAgentSuccessfullyStarted(agentInfo);
		}
		
		// Richiamato quando un Agente Jade non è stato creato ed avviato correttamente
		@Override
		protected void doOnAgentFailureStarted(Throwable throwable) {
			JadeWifiDirectActivity.this.doOnAgentFailureStarted(throwable);
		}
    	
    }
    
    /**
     * Richiamato quando è avvenuto un evento WiFi Direct
     * @param event {@link WifiDirectEvent} - evento accaduto
     * @param info {@link WifiDirectInfo} - info sullo stato del direct a seguito dell'evento
     * @param extraInfo {@link Object} - ulterio dati extra legati a particolari eventi
     */
    protected void doOnWifiDInfoChange(WifiDirectEvent event,
			WifiDirectInfo info, Object extraInfo) { }
    
    /**
     * Richiamato quando un Main Container Jade è stato creato con successo.
     * Se necessario è possibile effettuarne l'override.
     */
	protected void doOnMainContainerSuccessfullyStarted() {	}
	
	/**
	 * Richiamato quando un Main Container Jade non è stato creato correttamente.
	 * Se necessario è possibile effettuarne l'override.
	 * @param throwable {@link Throwable} - dettaglio dell'errore
	 */
	protected void doOnMainContainerFailureStarted(Throwable throwable) { }
	
	/**
	 * Richiamato quando un Container Jade è stato creato con successo.
	 * Se necessario è possibile effettuarne l'override.
	 */
	protected void doOnContainerSuccessfullyStarted() { }
	
	/**
	 * Richiamato quando un Container Jade non è stato creato correttamente.
	 * Se necessario è possibile effettuarne l'override.
	 * @param throwable {@link Throwable} - dettaglio dell'errore
	 */
	protected void doOnContainerFailureStarted(Throwable throwable) { }
	
	/**
	 * Richiamato quando un Agente Jade è stato creato ed avviato con successo.
	 * Se necessario è possibile effettuarne l'override.
	 * @param {@link AgentInfo} - info dell'agente appena creato
	 */
	protected void doOnAgentSuccessfullyStarted(AgentInfo agentInfo) { }
	
	/**
	 * Richiamato quando un Agente Jade non è stato creato ed avviato correttamente.
	 * Se necessario è possibile effettuarne l'override.
	 * @param throwable {@link Throwable} - dettaglio dell'errore
	 */
	protected void doOnAgentFailureStarted(Throwable throwable) { }
	
	/**
	 * Restituisce il nome Direct di questo dispositivo
	 * @return {@link String} - nome del dispositivo
	 */
	protected final String getMyDirectName(){
		if(isBound){
			return serviceBinder.getMyWiFiDirectDevice().deviceName;
		} else {
			//FIXME Eccezione
			return null;
		}
	}
	
	/**
	 * Resituisce la lista dei P2P Device raggiungibili
	 * @return {@link WifiP2pDeviceList} - la lista dei P2P Device
	 */
	protected final WifiP2pDeviceList getCurrentWifiP2pDeviceList() {
		if(isBound){
			return serviceBinder.getWifiDPeerList();
		} else {
			//FIXME Eccezione
			return null;
		}
	}
	
	/**
	 * Richiamato quando si perde la connessione al Service
	 */
	protected void onServiceConnectionDisconnected() { }
	
	/**
	 * Richiamato quando ci si connette al Service
	 * @param wifiDirectInfo {@link WifiDirectInfo} - info sullo stato attuale del WiFi Direct
	 */
	protected void onServiceConnectionConnected(WifiDirectInfo wifiDirectInfo) { }
}
