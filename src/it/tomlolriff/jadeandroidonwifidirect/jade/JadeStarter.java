package it.tomlolriff.jadeandroidonwifidirect.jade;

import jade.android.AgentContainerHandler;
import jade.android.RuntimeService;

/**
 * Interfaccia per l'avvio di una piattaforma Jade 
 * @author riff451 - TomZ85 - Lollo
 */
public interface JadeStarter {
	/**
	 * Crea un nuovo Main Container
	 */
	void createMainAgentContainer();
	
	/**
	 * Crea un container che si deve collegare al Main Container presente nell'host
	 * con indirizzo IP mainContainerHostAddress
	 * @param mainContainerHostAddress {@link String} - indirizzo IP dell'host che contiene il Main Container
	 */
	void createAgentContainer(String mainContainerHostAddress);
	
	/**
	 * Notifica la creazione del container avveunta con successo
	 * @param result {@link AgentContainerHandler} - oggetto che espone metodi
	 * per interagire con il Container appena creato
	 * @param runtimeService {@link RuntimeService} - oggetto che espone metodi
	 * per interagire con il Container appena creato
	 */
	void onSuccess(AgentContainerHandler result, RuntimeService runtimeService);
	
	/**
	 * Notifica un errore nella creazione del container
	 * @param error {@link Throwable} - dettagli sull'errore
	 */
	void onFailure(Throwable error);
}
