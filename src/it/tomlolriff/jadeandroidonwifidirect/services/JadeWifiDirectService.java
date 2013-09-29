package it.tomlolriff.jadeandroidonwifidirect.services;

import it.tomlolriff.jadeandroidonwifidirect.R;
import it.tomlolriff.jadeandroidonwifidirect.jade.JadeListener;
import it.tomlolriff.jadeandroidonwifidirect.jade.JadeManager;
import it.tomlolriff.jadeandroidonwifidirect.jade.exceptions.AgentContainerNotStartedException;
import it.tomlolriff.jadeandroidonwifidirect.jade.types.AgentInfo;
import it.tomlolriff.jadeandroidonwifidirect.jade.types.JadeState;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.WiFiDFrameworkManager;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.WiFiDirectInfoListener;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.WifiDirectInfo;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectEvent;
import jade.util.Logger;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

/**
 * Classe che implementa un Service che si occupa di creare una connessione WiFi
 * Direct su cui lanciare l'esecuzione di una piattaforma Jade.
 * 
 * @author riff451 - TomZ86 - Lollo
 */
public class JadeWifiDirectService extends Service implements WiFiDirectInfoListener, JadeListener{
	protected Logger logger = Logger.getJADELogger(this.getClass().getName());
	/**
	 * Binder da restituire ai client che si connettono
	 */
	private IBinder serviceBinder;
	/**
	 * Gestore del framework WiFi Direct
	 */
	private WiFiDFrameworkManager wifiDFrameManager;
	/**
	 * Gestore di Jade
	 */
	private JadeManager jadeManager;
	/**
	 * Lista di JadeListener registrati per ricevere le notifiche
	 */
	private List<JadeListener> jadeListeners;
	/**
	 * Lista di WiFiDirectInfoListener registrati per ricevere le notifiche
	 */
	private List<WiFiDirectInfoListener> wifiDirectInfolisteners;
	
	private JadeState jadeState;
	
	private final static String ACTION_SERVICE_STOP = "it.tomlolriff.wifip2pjadefwk.service.STOP";

	/**
	 * Indica se la piattaforma per eseguire le operazioni su jade è pronta
	 * @return <code>true</code> se la piattaforma per eseguire operazioni su jade è pronta, <code>false</code> altrimenti
	 */
	boolean platformReady() {
		return (jadeState == JadeState.CONTAINER_STARTED ||
				jadeState == JadeState.MAIN_CONTAINER_STARTED);
	}

	/**
	 * Il servizio avvia il framework di gestione del WiFi Direct
	 */
	void startWiFiDFramework(){
		wifiDFrameManager.startFramework();
	}
	
	/**
	 * Aggiunge un WiFiDirectInfoListener alla lista di listeners
	 * @param l WiFiDirectInfoListener - listener da aggiungere
	 */
	void addWiFiDirectInfoListener(WiFiDirectInfoListener l){
		wifiDirectInfolisteners.add(l);
	}
	
	/**
	 * Rimuove un WiFiDirectInfoListener dalla lista di listeners
	 * @param l WiFiDirectInfoListener - listener da rimuovere
	 */
	void removeWiFiDirectInfoListener(WiFiDirectInfoListener l){
		wifiDirectInfolisteners.remove(l);
	}
	
	/**
	 * Aggiunge un JadeListener
	 * @param l JadeListener
	 */
	void addJadeListener(JadeListener l) {
		jadeListeners.add(l);
	}
	
	/**
	 * Rimuove un JadeListener
	 * @param l JadeListener
	 */
	void removeJadeListener(JadeListener l) {
		jadeListeners.remove(l);
	}
	
	/**
	 * Il servizio avvia una nuova piattaforma Jade formata da me e da device;
	 * in maniera random uno dei due ospiterà un Main Container e l'altro un Container collegato
	 * @param device WiFiP2PDevice - device con cui voglio formare la piattaforma Jade.
	 */
	void startNewJadePlatform(WifiP2pDevice device){
		// prima eseguo la connessione wifi direct...
		wifiDFrameManager.connectToPeer(device);
		//...poi quando vengo notificato tramite l'onConnection() dell'avvenuta connessione avvio la piattaforma jade
	}
	
	/**
	 * Crea un nuovo agente sul container ospitato da questo device
	 * @param agentName String - nome del nuovo agente
	 * @param className String - nome della classe che implementa l'agente
	 * @param args Object[] - array di parametri ulteriori
	 */
	void createAgent(String agentName, String className, Object[] args) {
		try {
			jadeManager.createAgent(agentName, className, args);
		} catch (AgentContainerNotStartedException e) {
			logger.log(Level.SEVERE, e.getMessage());
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
	<T> T getO2AInterface(String agentName, Class<T> theInterface) throws StaleProxyException, AgentContainerNotStartedException {
		return jadeManager.getO2AInterface(agentName, theInterface);
	}

	/**
	 * Restituisce le info sullo stato attuale del WiFi Direct
	 * @return WifiDirectInfo info sullo stato attuale del WiFi Direct
	 */
	WifiDirectInfo getWiFiDirectInfo(){
		return wifiDFrameManager.getWiFiDirectInfo();
	}
	
	/**
	 * Restituisce la lista attuale di WiFi Direct Peers raggiungibili
	 * @return WifiP2pDeviceList lista di Peers
	 */
	WifiP2pDeviceList getWifiDPeerList(){
		return wifiDFrameManager.getPeerList();
	}
	
	/**
	 * Restituisce questo dispositivo WiFi Direct
	 * @return WifiP2pDevice questo dispositivo
	 */
	WifiP2pDevice getMyWiFiDirectDevice(){
		return wifiDFrameManager.getMe();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// qui non fa nient'altro che istanziare e restituire il serviceBinder
		logger.log(Level.INFO, "onBind() Service");
		serviceBinder = new JadeWifiDirectServiceBinder(this);
		return serviceBinder;
	}
	
	// Inizializzo il framework WiFi Direct e comincio il discover dei Peers
	@Override
	public void onCreate() {
		super.onCreate();
		logger.log(Level.INFO, "onCreate() Service");
		jadeState = JadeState.DISCONNECTED;
		wifiDirectInfolisteners = new ArrayList<WiFiDirectInfoListener>();
		jadeListeners = new ArrayList<JadeListener>();
		jadeManager = JadeManager.getInstance(this);
		wifiDFrameManager = WiFiDFrameworkManager.getInstance(this, this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_SERVICE_STOP);
		registerReceiver(new NotificationReceiver(), filter);
		startForeground(19666, initServiceNotification());
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		logger.log(Level.INFO, "onStartCommand() Service");
		return START_STICKY;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		super.onUnbind(intent);
		logger.log(Level.INFO, "onUnbind() Service");
		return false;
	}
	
	// Rilascio tutte le risorse che non mi servono più
	// FIXME dovrei anche terminare eventuali container Jade!
	@Override
	public void onDestroy() {
		super.onDestroy();
		logger.log(Level.INFO, "stop WiFi Direct Framework - Service");
		// attendo la terminazione di jade
		CountDownLatch latch = new CountDownLatch(1);
		jadeManager.killAgentContainer(latch);
		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wifiDFrameManager.stopFramework();
		removeAllWiFiDirectFrameworkListeners();
		removeAllJadeListeners();
		stopForeground(true);
	}
	
	// Notifico i listener e - nel caso ci sia bisogno - eseguo le opportune
	// operazioni a seguito di certi eventi
	@Override
	public void onWifiDInfoChange(WifiDirectEvent event, WifiDirectInfo info,
			Object extraInfo) {
		// l'unico evento che al momento mi interessa catturare qua è questo...
		if(event.equals(WifiDirectEvent.CONNECTION_INFO_EVENT) && jadeState == JadeState.DISCONNECTED){
			// sono il group owner avvio un main container
			if( ((WifiP2pInfo)extraInfo).isGroupOwner) {
				jadeManager.createMainAgentContainer();
			} else {
				jadeManager.createAgentContainer(((WifiP2pInfo)extraInfo).groupOwnerAddress);
			}
		}
		//...gli altri li rilancio ai listener e basta
		notifyWifiDInfoListeners(event, info, extraInfo);
	}
	
	@Override
	public void onMainContainerSuccessfullyStarted() {
		jadeState = JadeState.MAIN_CONTAINER_STARTED;
		for(JadeListener l : jadeListeners){
			l.onMainContainerSuccessfullyStarted();
		}
	}

	@Override
	public void onMainContainerFailureStarted(Throwable throwable) {
		for(JadeListener l : jadeListeners){
			l.onMainContainerFailureStarted(throwable);
		}
	}

	@Override
	public void onContainerSuccessfullyStarted() {
		jadeState = JadeState.CONTAINER_STARTED;
		for(JadeListener l : jadeListeners){
			l.onContainerSuccessfullyStarted();
		}
	}

	@Override
	public void onContainerFailureStarted(Throwable throwable) {
		for(JadeListener l : jadeListeners){
			l.onContainerFailureStarted(throwable);
		}
	}
	
	@Override
	public void onAgentSuccessfullyStarted(AgentInfo agentInfo) {
		for(JadeListener l : jadeListeners){
			l.onAgentSuccessfullyStarted(agentInfo);
		}
	}

	@Override
	public void onAgentFailureStarted(Throwable throwable) {
		for(JadeListener l : jadeListeners){
			l.onAgentFailureStarted(throwable);
		}		
	}
	
	private class NotificationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if(action.equalsIgnoreCase(ACTION_SERVICE_STOP)) {
				unregisterReceiver(this);
				stopSelf();
			}
		}
		
	}
	
	/**
	 * Inizializza e costruisce le Notification da visualizzare nella Status Bar
	 * @return Notification
	 */
	private Notification initServiceNotification(){
		
		Intent notifyIntent = 
				new Intent(ACTION_SERVICE_STOP);
		// Creates the PendingIntent
		PendingIntent notifyPIntent =
		        PendingIntent.getBroadcast(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Notification not = null;
		NotificationCompat.Builder notBuilder = new NotificationCompat.Builder(this);
		notBuilder.setSmallIcon(R.drawable.ic_stat_notify_smile);
		notBuilder.setContentTitle("Jade WiFi Direct Service");
		notBuilder.setContentText("Jade Over WiFi Direct Service in esecuzione");
		notBuilder.addAction(R.drawable.ic_stat_notify_smile, "Stop Service", notifyPIntent);

		not = notBuilder.build();
		
		return not;
	}
	
	/**
	 * Notifica tutti i listener registrati per gli eventi WiFi Direct
	 * @param event {@link WifiDirectEvent} - tipo evento WiFi Direct da notificare
	 * @param wifiDInfo {@link WifiDirectInfo} - informazioni legate all'evento WiFi Direct verificatosi
	 * @param extraInfo Object - ulteriori informazioni da passare
	 */
	private void notifyWifiDInfoListeners(WifiDirectEvent event, WifiDirectInfo wifiDInfo, Object extraInfo){
		for(WiFiDirectInfoListener l : wifiDirectInfolisteners){
			l.onWifiDInfoChange(event, wifiDInfo, extraInfo); // notifico tutti i listeners
		}
	}
	
	/**
	 * Svuota la lista di {@link WiFiDirectInfoListener}
	 */
	private void removeAllWiFiDirectFrameworkListeners(){
		wifiDirectInfolisteners.clear();
	}
	
	/**
	 * Svuota la lista di {@link JadeListener} 
	 */
	private void removeAllJadeListeners(){
		jadeListeners.clear();
	}
}
