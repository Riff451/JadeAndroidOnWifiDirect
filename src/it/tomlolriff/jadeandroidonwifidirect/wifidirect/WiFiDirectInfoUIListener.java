package it.tomlolriff.jadeandroidonwifidirect.wifidirect;

import it.tomlolriff.jadeandroidonwifidirect.wifidirect.types.WifiDirectEvent;
import android.os.Handler;

public abstract class WiFiDirectInfoUIListener implements
		WiFiDirectInfoListener {
	
	private Handler myHandler = new Handler();

	@Override
	public void onWifiDInfoChange(final WifiDirectEvent event, final WifiDirectInfo info,
			final Object extraInfo) {

		myHandler.post(new Runnable() {
			
			@Override
			public void run() {
				doOnWifiDInfoChange(event, info, extraInfo);
			}
		});
	}
	
	/**
	 * Notifica un evento WiFi Direct
	 * @param event {@link WifiDirectEvent} - evento WiFi Direct accaduto
	 * @param info {@link WifiDirectInfo} - info sullo stato WiFi Direct a seguito di event
	 * @param extraInfo {@link Object} - info extra legate a particolari eventi ; può essere null
	 */
	protected abstract void doOnWifiDInfoChange(WifiDirectEvent event, WifiDirectInfo info,
			Object extraInfo);

}
