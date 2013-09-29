package it.tomlolriff.jadeandroidonwifidirect.location;

import it.tomlolriff.jadeandroidonwifidirect.location.LocationStrategy.Listener;
import android.location.Location;
import android.os.Handler;

public abstract class LocationStrategyUIListener implements Listener{
	private Handler handler = new Handler();
	
	@Override
	public void onCurrentBestLocationChange(final Location currentBestLocation){
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				doOnCurrentBestLocationChange(currentBestLocation);
			}
		});
	}
	
	public abstract void doOnCurrentBestLocationChange(Location currentBestLocation);
}
