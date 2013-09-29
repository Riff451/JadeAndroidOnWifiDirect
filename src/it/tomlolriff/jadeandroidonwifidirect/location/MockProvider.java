package it.tomlolriff.jadeandroidonwifidirect.location;

import android.location.Criteria;

public class MockProvider {
		
	private String name = null;
	private boolean requiresNetwork = false;
	private boolean requiresSatellite = false;
	private boolean requiresCell = false;
	private boolean hasMonetaryCost = false;
	private boolean supportsAltitude = false;
	private boolean supportsSpeed = false;
	private boolean supportsBearing = false;
	private int powerRequirement = Criteria.NO_REQUIREMENT;
	private int accuracy = Criteria.NO_REQUIREMENT;
	
	public MockProvider(){
		
	}
	
	public MockProvider(String name, boolean requiresNetwork, boolean requiresSatellite, 
			boolean requiresCell, boolean hasMonetaryCost, boolean supportsAltitude, 
			boolean supportsSpeed, boolean supportsBearing, int powerRequirement, int accuracy){
		
		this.name = name;
		this.requiresNetwork = requiresNetwork;
		this.requiresSatellite = requiresSatellite;
		this.requiresCell = requiresCell;
		this.hasMonetaryCost = hasMonetaryCost;
		this.supportsAltitude = supportsAltitude;
		this.supportsSpeed = supportsSpeed;
		this.supportsBearing = supportsBearing;
		this.powerRequirement = powerRequirement;
		this.accuracy = accuracy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRequiresNetwork() {
		return requiresNetwork;
	}

	public void setRequiresNetwork(boolean requiresNetwork) {
		this.requiresNetwork = requiresNetwork;
	}

	public boolean isRequiresSatellite() {
		return requiresSatellite;
	}

	public void setRequiresSatellite(boolean requiresSatellite) {
		this.requiresSatellite = requiresSatellite;
	}

	public boolean isRequiresCell() {
		return requiresCell;
	}

	public void setRequiresCell(boolean requiresCell) {
		this.requiresCell = requiresCell;
	}

	public boolean isHasMonetaryCost() {
		return hasMonetaryCost;
	}

	public void setHasMonetaryCost(boolean hasMonetaryCost) {
		this.hasMonetaryCost = hasMonetaryCost;
	}

	public boolean isSupportsAltitude() {
		return supportsAltitude;
	}

	public void setSupportsAltitude(boolean supportsAltitude) {
		this.supportsAltitude = supportsAltitude;
	}

	public boolean isSupportsSpeed() {
		return supportsSpeed;
	}

	public void setSupportsSpeed(boolean supportsSpeed) {
		this.supportsSpeed = supportsSpeed;
	}

	public boolean isSupportsBearing() {
		return supportsBearing;
	}

	public void setSupportsBearing(boolean supportsBearing) {
		this.supportsBearing = supportsBearing;
	}

	public int getPowerRequirement() {
		return powerRequirement;
	}

	public void setPowerRequirement(int powerRequirement) {
		this.powerRequirement = powerRequirement;
	}

	public int getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}
}
