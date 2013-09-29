package it.tomlolriff.jadeandroidonwifidirect.location;

public class LocationStrategyMode {
	
	public static final byte NETWORK = 0x1;
	
	public static final byte GPS = 0x2;
	
	public static final byte MOCK = 0xf;
	
	public static boolean isModeStatusSet(byte mask, byte mode){
		return ( (mask & mode) == mode ? true : false );
	}
}
