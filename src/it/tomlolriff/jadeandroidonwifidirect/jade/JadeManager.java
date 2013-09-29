package it.tomlolriff.jadeandroidonwifidirect.jade;

import it.tomlolriff.jadeandroidonwifidirect.jade.exceptions.AgentContainerNotStartedException;
import it.tomlolriff.jadeandroidonwifidirect.jade.types.AgentInfo;
import it.tomlolriff.jadeandroidonwifidirect.util.StringHelper;
import jade.android.AgentContainerHandler;
import jade.android.AgentHandler;
import jade.android.RuntimeCallback;
import jade.android.RuntimeService;
import jade.util.Logger;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

/**
 * Classe che gestisce la creazione e l'interazione con la piattaforma Jade
 * @author riff451 - TomZ85 - Lollo
 */
public class JadeManager {
	private Logger logger = Logger.getJADELogger(this.getClass().getName());
	private static JadeManager instance = null;
	private List<JadeListener> listeners;
	private RuntimeService runtimeService;
	private AgentContainerHandler agentContainerHandler;
	private Map<String, AgentController> mapAgentController;
	
	public static JadeManager getInstance(JadeListener listener){
		if(instance == null){
			instance = new JadeManager(listener);
		}
		
		return instance;
	}
	
	private JadeManager(JadeListener listener){
		listeners = new ArrayList<JadeListener>();
		listeners.add(listener);
		mapAgentController = new Hashtable<String, AgentController>();
	}
	
	public <T> T getO2AInterface(String agentName, Class<T> theInterface) throws StaleProxyException, AgentContainerNotStartedException {
		if(runtimeService == null) {
			throw new AgentContainerNotStartedException();
		}
		AgentController agentController = mapAgentController.get(agentName);
		if(agentController == null) {
			//TODO eccezione
			throw new AgentContainerNotStartedException();
		}
		T res = null;
		do{
			/*try {
				wait(2000);	// aspetta 2 secondi, non ha ancora l'interfaccia pronta
			} catch (InterruptedException e) {
				throw new AgentContainerNotStartedException();
			}*/
			res = agentController.getO2AInterface(theInterface);
		}while(res == null);
			
		return agentController.getO2AInterface(theInterface);
		
	}
	
	/**
	 * Crea un nuovo agente
	 * @param agentName {@link String} - nome del nuovo agente
	 * @param className {@link String} - nome della classe che implementa il nuovo agente
	 * @param args {@link Object}[] - array di ulteriori argomenti da passare all'agente
	 * @throws AgentContainerNotStartedException
	 */
	public void createAgent(final String agentName, String className, Object[] args) throws AgentContainerNotStartedException {
		if(runtimeService == null) {
			throw new AgentContainerNotStartedException();
		}
		if(agentContainerHandler == null) {
			throw new AgentContainerNotStartedException();
		}

		runtimeService.createNewAgent(agentContainerHandler, agentName, className, args, new RuntimeCallback<AgentHandler>() {
			
			@Override
			public void onSuccess(final AgentHandler result) {
				result.start(new RuntimeCallback<Void>() {
					
					@Override
					public void onSuccess(Void resultVoid) {
						mapAgentController.put(agentName, result.getAgentController());
						for (JadeListener listener : listeners) {
							try {
								listener.onAgentSuccessfullyStarted(new AgentInfo(result.getAgentController().getName()));
							} catch (StaleProxyException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					
					@Override
					public void onFailure(Throwable throwable) {
						for (JadeListener listener : listeners) {
							listener.onAgentFailureStarted(throwable);
						}
					}
				});
				
			}
			
			@Override
			public void onFailure(Throwable throwable) {
				for (JadeListener listener : listeners) {
					listener.onAgentFailureStarted(throwable);
				}
			}
		});
	}

	/**
	 * Crea un Main Container di una nuova piattaforma Jade
	 */
	public void createMainAgentContainer(){
		logger.log(Level.INFO, "createAgentContainer - Inizio Creazione Main Container");
		JadeStarter jadeStarter1 = new JadeStarterImpl() {
			@Override
			public void onSuccess(AgentContainerHandler agentContainerHandler, RuntimeService runtimeService) {
				JadeManager.this.agentContainerHandler = agentContainerHandler;
				JadeManager.this.runtimeService = runtimeService;
				for (JadeListener listener : listeners) {
					listener.onMainContainerSuccessfullyStarted();
				}
			}

			@Override
			public void onFailure(Throwable throwable) {
				for (JadeListener listener : listeners) {
					listener.onMainContainerFailureStarted(throwable);
				}
			}
		};
		jadeStarter1.createMainAgentContainer();
	}

	/**
	 * Crea un container JADE
	 * @param mainContainerHostAddress {@link InetAddress} - indirizzo IP dell'host del Main Container a cui collegarsi
	 */
	public void createAgentContainer(InetAddress mainContainerHostAddress) {
		String mainContainerHostAddressS = mainContainerHostAddress.toString();
		// Deve avviare un Container che si collega al Main Container all'indirizzo mainContainerHostAddressS:JADE_DEFAULT_PORT
		logger.log(Level.INFO, "createAgentContainer - Inizio Creazione Container");
		JadeStarter jadeStarter2 = new JadeStarterImpl() {
			
			@Override
			public void onSuccess(AgentContainerHandler agentContainerHandler, RuntimeService runtimeService) {
				JadeManager.this.agentContainerHandler = agentContainerHandler;
				JadeManager.this.runtimeService = runtimeService;
				for (JadeListener listener : listeners) {
					listener.onContainerSuccessfullyStarted();
				}
			}

			@Override
			public void onFailure(Throwable throwable) {
				for (JadeListener listener : listeners) {
					listener.onContainerFailureStarted(throwable);
				}
			}
		};
		jadeStarter2.createAgentContainer(StringHelper.fixHostAddress(mainContainerHostAddressS));
	}
	
	/**
	 * Termina il container e lo notifica al thread chiamante sbloccandolo
	 * @param latch {@link CountDownLatch} - contatore da diminuire al termine dell'operazione per notifcare il thread chiamante
	 */
	public void killAgentContainer(final CountDownLatch latch){
		// se non ho container avviati non devo fare niente
		if(agentContainerHandler == null) {
			latch.countDown();
			return;
		}
		
		runtimeService.killAgentContainer(agentContainerHandler, new RuntimeCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				latch.countDown();
			}
			
			@Override
			public void onFailure(Throwable throwable) {
				// anche in caso di fallimento sblocco il thread chiamante
				latch.countDown();
			}
		});
	}
	
	public void addJadeListener(JadeListener l){
		listeners.add(l);
	}
	
	public void removeJadeListener(JadeListener l){
		listeners.remove(l);
	}
}
