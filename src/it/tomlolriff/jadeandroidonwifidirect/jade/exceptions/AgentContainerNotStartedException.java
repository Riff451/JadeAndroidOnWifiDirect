package it.tomlolriff.jadeandroidonwifidirect.jade.exceptions;

public class AgentContainerNotStartedException extends Exception {
	private static final long serialVersionUID = 1L;

	public AgentContainerNotStartedException() {
		super("L'agent container non è stato avviato");
	}
}
