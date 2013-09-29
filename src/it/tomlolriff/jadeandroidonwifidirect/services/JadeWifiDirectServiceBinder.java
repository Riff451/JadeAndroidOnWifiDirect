package it.tomlolriff.jadeandroidonwifidirect.services;

import it.tomlolriff.jadeandroidonwifidirect.jade.JadeListener;
import it.tomlolriff.jadeandroidonwifidirect.jade.exceptions.AgentContainerNotStartedException;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.WiFiDirectInfoListener;
import it.tomlolriff.jadeandroidonwifidirect.wifidirect.WifiDirectInfo;
import jade.wrapper.StaleProxyException;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Binder;

/**
 * Classe che espone l'interfaccia pubblica del JadeWifiDirectService
 * 
 * @author riff451 - TomZ85 - Lollo
 */
public class JadeWifiDirectServiceBinder extends Binder {
	private JadeWifiDirectService service;

	JadeWifiDirectServiceBinder(JadeWifiDirectService service) {
		this.service = service;
	}

	/**
	 * Indica se la piattaforma per eseguire le operazioni su jade è pronta
	 * @return <code>true</code> se la piattaforma per eseguire operazioni su jade è pronta, <code>false</code> altrimenti
	 */
	public boolean platformReady() {
		return service.platformReady();
	}

	/**
	 * Il servizio avvia il framework di gestione del WiFi Direct
	 */
	public void startWiFiDFramework(){
		service.startWiFiDFramework();
	}
	
	/**
	 * Il servizio avvia una nuova piattaforma Jade formata da me e da device eseguendo,
	 * in modo trasparente, la connessione WiFi Direct.
	 * In maniera random uno dei due ospiterà un Main Container e l'altro un Container collegato.
	 * @param device WiFiP2PDevice con cui voglio formare la piattaforma Jade.
	 */
	public void startNewJadePlatform(WifiP2pDevice device){
		service.startNewJadePlatform(device);
	}
	/**
	 * Aggiunge un WiFiDirectInfoListener
	 * @param l WiFiDirectInfoListener
	 */
	public void addWiFiDirectInfoListener(WiFiDirectInfoListener l){
		service.addWiFiDirectInfoListener(l);
	}
	/**
	 * Rimuove un WiFiDirectInfoListener
	 * @param l WiFiDirectInfoListener
	 */
	public void removeWiFiDirectFrameworkListener(WiFiDirectInfoListener l){
		service.removeWiFiDirectInfoListener(l);
	}
	/**
	 * Aggiunge un JadeListener
	 * @param l JadeListener
	 */
	public void addJadeListener(JadeListener l){
		service.addJadeListener(l);
	}
	/**
	 * Rimuove un JadeListener
	 * @param l JadeListener
	 */
	public void removeJadeListener(JadeListener l){
		service.removeJadeListener(l);
	}
	/**
	 * Restituisce le info sullo stato attuale del WiFi Direct
	 * @return WifiDirectInfo info sullo stato attuale del WiFi Direct
	 */
	public WifiDirectInfo getWiFiDirectInfo(){
		return service.getWiFiDirectInfo();
	}
	/**
	 * Restituisce la lista attuale di WiFi Direct Peers raggiungibili
	 * @return WifiP2pDeviceList lista di Peers
	 */
	public WifiP2pDeviceList getWifiDPeerList(){
		return service.getWifiDPeerList();
	}
	/**
	 * Restituisce questo dispositivo WiFi Direct
	 * @return WifiP2pDevice questo dispositivo
	 */
	public WifiP2pDevice getMyWiFiDirectDevice(){
		return service.getMyWiFiDirectDevice();
	}
	/**
	 * Crea un nuovo agente sul container ospitato da questo device
	 * @param agentName String - nome del nuovo agente
	 * @param className String - nome della classe che implementa l'agente
	 * @param args Object[] - array di parametri ulteriori
	 */
	public void createAgent(String agentName, String className, Object[] args) {
		service.createAgent(agentName, className, args);
	}

	/**
	 * Permette di recuperare per l'agente <code>agentName</code>, l'interfaccia <code>theInterface</code>
	 * @param agentName il nome dell'agente
	 * @param theInterface l'interfaccia richiesta
	 * @return l'interfaccia <code>theInterface</code> per l'agente <code>agentName</code>
	 * @throws StaleProxyException
	 * @throws AgentContainerNotStartedException
	 */
	public <T> T getO2AInterface(String agentName, Class<T> theInterface) throws StaleProxyException, AgentContainerNotStartedException {
		return service.getO2AInterface(agentName, theInterface);
	}
}
