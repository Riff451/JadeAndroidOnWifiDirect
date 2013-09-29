package it.tomlolriff.jadeandroidonwifidirect.jade;

import it.tomlolriff.jadeandroidonwifidirect.jade.types.AgentInfo;


/**
 * Interfaccia di callback per gestire gli eventi di Jade
 * @author riff451
 *
 */
public interface JadeListener {
	/**
	 * Richiamato a seguito di una creazione di un Main Container avvenuta con successo
	 */
	void onMainContainerSuccessfullyStarted();
	/**
	 * Richiamato a seguito di una creazione di un Main Container fallita
	 * @param throwable {@link Throwable} - dettaglio dell'errore
	 */
	void onMainContainerFailureStarted(Throwable throwable);
	
	/**
	 * Richiamato a seguito di una creazione di un Container avvenuta con successo
	 */
	void onContainerSuccessfullyStarted();
	/**
	 * Richiamato a seguito di una creazione di un Container fallita
	 * @param throwable {@link Throwable} - dettaglio dell'errore
	 */
	void onContainerFailureStarted(Throwable throwable);
	
	/**
	 * Richiamato a seguito di una creazione ed avvio di un Agent avvenute con successo
	 * @param {@link AgentInfo} - info dell'agente creato
	 */
	void onAgentSuccessfullyStarted(AgentInfo agentInfo);
	/**
	 * Richiamato a seguito di una creazione ed avvio di un Agent fallite
	 * @param throwable {@link Throwable} - dettaglio dell'errore
	 */
	void onAgentFailureStarted(Throwable throwable);
}
