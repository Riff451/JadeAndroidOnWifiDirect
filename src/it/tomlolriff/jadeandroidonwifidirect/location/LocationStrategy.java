package it.tomlolriff.jadeandroidonwifidirect.location;

import it.tomlolriff.jadeandroidonwifidirect.location.exceptions.LocationStrategyException;
import it.tomlolriff.jadeandroidonwifidirect.location.exceptions.LocationStrategyModeNotActivatedException;

import java.util.ArrayList;
import java.util.List;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;

/**
 * Classe che rappresenta una strategia di geolocalizzazione
 * @author riff451 - TomZ85 - Lollo
 */
public abstract class LocationStrategy {
	
	private LocationManager locManager = null;
	private Location bestLocation = null;
	private byte modeMask = 0x0;
	private ListenersHandler listenersHandler = null;
	private List<LocationStrategy.Listener> listeners = null;
	private MockProvidersList mockProviders = null;
	
	/**
	 * 
	 * @param locManager
	 * @param mode
	 * @param listener
	 */
	protected LocationStrategy(LocationManager locManager, byte mode, LocationStrategy.Listener listener){
		this.locManager = locManager;
		this.modeMask = mode;
		listeners = new ArrayList<LocationStrategy.Listener>();
		addListener(listener);
		listenersHandler = new ListenersHandler("LocationStrategy Listeners Handler");
		mockProviders = new MockProvidersList();
		listenersHandler.start();
	}
	
	/**
	 * 
	 * @param listener
	 */
	protected final void addListener(LocationStrategy.Listener listener){
		listeners.add(listener);
	}
	
	/**
	 * 
	 * @param listener
	 */
	protected final void removeListener(LocationStrategy.Listener listener){
		listeners.remove(listener);
	}
	
	/**
	 * 
	 */
	protected final void notifyCurrentBestLocationChange(){
		for(LocationStrategy.Listener listener : listeners){
			listener.onCurrentBestLocationChange(bestLocation);
		}
	}
	
	/**
	 * 
	 */
	private void removeAllListeners(){
		listeners.clear();
	}
	
	/**
	 * Ritorna il MockProvider di nome name della LocationStrategy
	 * @param name {@link String} - nome del MockProvider
	 * @return {@link MockProvider} - torna il MockProvider o null se non è stato trovato
	 * @throws LocationStrategyModeNotActivatedException - modalità necessaria non attivata per la LocationStrategy
	 */
	protected final MockProvider getMockProvider(String name) throws LocationStrategyModeNotActivatedException{
		if(LocationStrategyMode.isModeStatusSet(modeMask, LocationStrategyMode.MOCK)){
			return mockProviders.getMockByName(name);
		}
		else{
			throw new LocationStrategyModeNotActivatedException("LocationStrategyMode.MOCK non attivata per la LocationStrategy");
		}
	}
	
	/**
	 * 
	 * @param mockProvider
	 * @throws LocationStrategyModeNotActivatedException
	 */
	protected final void setMockProvider(MockProvider mockProvider) throws LocationStrategyModeNotActivatedException{
		if(LocationStrategyMode.isModeStatusSet(modeMask, LocationStrategyMode.MOCK)){
			// non permetto mockProvider con nomi uguali
			if( (mockProviders.getMockByName(mockProvider.getName())) == null ){
				mockProviders.add(mockProvider);
			}
			else{ // FIXME forse dovrei fare qualcosa
				
			}
		}
		else{
			throw new LocationStrategyModeNotActivatedException("LocationStrategyMode.MOCK non attivata per la LocationStrategy");
		}
	}
	
	public final void addMockProvider(MockProvider mockProvider)
			throws LocationStrategyException {
		try {
			setMockProvider(mockProvider);
			
		} catch (LocationStrategyModeNotActivatedException e) {
			throw new LocationStrategyException("LocationStrategy non abilitato a ricevere Mock Location");
		}
	}
	
	/**
	 * 
	 * @param mockProvider
	 * @throws LocationStrategyModeNotActivatedException
	 */
	protected final void removeMockProvider(MockProvider mockProvider) throws LocationStrategyModeNotActivatedException{
		if(LocationStrategyMode.isModeStatusSet(modeMask, LocationStrategyMode.MOCK)){
			mockProviders.remove(mockProvider);
		}
		else{
			throw new LocationStrategyModeNotActivatedException("LocationStrategyMode.MOCK non attivata per la LocationStrategy");
		}
	}
	
	/**
	 * 
	 * @param mockLocation
	 * @param mockProviderName
	 * @throws LocationStrategyModeNotActivatedException
	 */
	protected final void setMockLocation(Location mockLocation, String mockProviderName)
			throws LocationStrategyModeNotActivatedException {
		
		if(LocationStrategyMode.isModeStatusSet(modeMask, LocationStrategyMode.MOCK)){
			
			mockLocation.setProvider(mockProviderName);
			locManager.setTestProviderLocation(mockProviderName, mockLocation);
		}
		else{
			throw new LocationStrategyModeNotActivatedException("LocationStrategyMode.MOCK non attivata per la LocationStrategy");
		}
	}
	
	/**
	 * 
	 * @param mockLocation
	 * @param mockProviderName
	 * @throws LocationStrategyException
	 */
	public final void notifyMockLocation(Location mockLocation, String mockProviderName)
		throws LocationStrategyException {
		
		try {
			setMockLocation(mockLocation, mockProviderName);
			
		} catch (LocationStrategyModeNotActivatedException e) {
			throw new LocationStrategyException("LocationStrategy non abilitato a ricevere Mock Location");
		}
	}
	
	/**
	 * 
	 * @return
	 */
	protected final Location getBestLocation() {
		return bestLocation;
	}
	
	/**
	 * 
	 * @param bestLocation
	 */
	protected final void setBestLocation(Location bestLocation) {
		this.bestLocation = bestLocation;
		notifyCurrentBestLocationChange();
	}
	
	/**
	 * 
	 */
	public final void start(){
		Looper looper = listenersHandler.getLooper();
		if(looper != null){
			
			if(LocationStrategyMode.isModeStatusSet(modeMask, LocationStrategyMode.MOCK)){
				// se non mi è stato fornito un MockProvider ne costruisco uno di default
				if(mockProviders.isEmpty()){
					MockProvider defaultMockProv = new MockProvider();
					defaultMockProv.setName("default");
					mockProviders.add(defaultMockProv);
				}
				// aggiungo tutti i MockProvider al manager e li abilito
				for(MockProvider mProv : mockProviders){
					locManager.addTestProvider(mProv.getName(), mProv.isRequiresNetwork(), 
							mProv.isRequiresSatellite(), mProv.isRequiresCell(), mProv.isHasMonetaryCost(), 
							mProv.isSupportsAltitude(), mProv.isSupportsSpeed(), 
							mProv.isSupportsBearing(), mProv.getPowerRequirement(), mProv.getAccuracy());
					locManager.setTestProviderEnabled(mProv.getName(), true);
					locManager.requestLocationUpdates(mProv.getName(), 0, 0, listenersHandler, looper);
				}
				
				return;
			}
			
			if(LocationStrategyMode.isModeStatusSet(modeMask, LocationStrategyMode.NETWORK)){
				// aggiorno l'attuale best location con l'ultima rilevata per il provider NETWORK
				bestLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				// FIXME da capire cosa sono il 2° e il 3° parametro
				locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listenersHandler, looper);
			}
			
			if(LocationStrategyMode.isModeStatusSet(modeMask, LocationStrategyMode.GPS)){
				// aggiorno l'attuale best location con l'ultima rilevata per il provider GPS
				// sovrascrivo l'eventuale valore rilevato dal provider NETWORK
				bestLocation = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				locManager.addGpsStatusListener(listenersHandler);
				// FIXME da capire cosa sono il 2° e il 3° parametro
				locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listenersHandler, looper);
			}
			if(bestLocation != null){
				// all'avvio notifico subito la migliore ultima posizione rilevata che ho in cache;
				// potrebbe essere anche null
				notifyCurrentBestLocationChange();
			}
		}
	}
	
	/**
	 * 
	 */
	public final void stop(){
		listenersHandler.quit();
		locManager.removeUpdates(listenersHandler);
		if(LocationStrategyMode.isModeStatusSet(modeMask, LocationStrategyMode.GPS)){
			locManager.removeGpsStatusListener(listenersHandler);
		}
		if(LocationStrategyMode.isModeStatusSet(modeMask, LocationStrategyMode.MOCK)){
			for(MockProvider mProv : mockProviders){
				locManager.removeTestProvider(mProv.getName());
			}
		}
		//onCurrentBestLocationChange(bestLocation);
		removeAllListeners();
	}
	
	protected void doOnGpsStatusChanged(int event) {
		
	}
	
	protected void doOnLocationChanged(Location location) {
		
	}
	
	protected void doOnStatusChanged(String provider, int status, Bundle extras) {
		
	}
	
	protected void doOnProviderEnabled(String provider) {
		
	}
	
	protected void doOnProviderDisabled(String provider) {
		
	}
	
	interface Listener {
		void onCurrentBestLocationChange(Location currentBestLocation);
	}
	
	
	
	private class ListenersHandler extends HandlerThread implements
		LocationListener, GpsStatus.Listener {
		
		private ListenersHandler(String name) {
			super(name);
		}

		@Override
		public synchronized void onGpsStatusChanged(int event) {
			doOnGpsStatusChanged(event);
		}

		@Override
		public synchronized void onLocationChanged(Location location) {
			doOnLocationChanged(location);
		}

		@Override
		public synchronized void onStatusChanged(String provider, int status, Bundle extras) {
			doOnStatusChanged(provider, status, extras);		
		}

		@Override
		public synchronized void onProviderEnabled(String provider) {
			doOnProviderEnabled(provider);
		}

		@Override
		public synchronized void onProviderDisabled(String provider) {
			doOnProviderDisabled(provider);
		}
	}
	
	private class MockProvidersList extends ArrayList<MockProvider> {
		private static final long serialVersionUID = 3650720799215585526L;

		private MockProvidersList(){
			super();
		}
		
		private MockProvider getMockByName(String name){
			MockProvider mockReturned = null;
			
			for(MockProvider mProv : this){
				if(mProv.getName().equalsIgnoreCase(name)){
					mockReturned = mProv;
				}
			}
			
			return mockReturned;
		}
	}
}
