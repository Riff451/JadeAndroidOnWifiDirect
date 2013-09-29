package it.tomlolriff.jadeandroidonwifidirect.jade.types;

public class AgentInfo {
	
	private String agentName = null;
	
	public AgentInfo() {
		
	}
	
	public AgentInfo(String agentName) {
		this.agentName = agentName;
	}
	
	/**
	 * Torna il nome dell'agente univoco all'interno della piattaforma jade
	 * @return {@link String} - nome dell'agente
	 */
	public String getAgentName() {
		return agentName;
	}
	
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
}
