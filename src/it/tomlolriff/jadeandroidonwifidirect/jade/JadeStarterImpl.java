package it.tomlolriff.jadeandroidonwifidirect.jade;

import jade.android.AgentContainerHandler;
import jade.android.RuntimeCallback;
import jade.android.RuntimeService;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.Logger;
import jade.util.leap.Properties;

import java.util.logging.Level;

abstract class JadeStarterImpl implements JadeStarter {
	protected Logger logger = Logger.getJADELogger(this.getClass().getName());
	private RuntimeService runtimeService = new RuntimeService();
	private static final int JADE_DEFAULT_PORT = 1099;
	private static final int NUM_MAX_RETRY = 3;
	private static final int DELAY_MAX = 3000;
	private int retryCount;
	private String mainContainerHostAddress = null;

	JadeStarterImpl() {
		retryCount = 0;	// non servirebbe, ma lo mettiamo per chiarezza
	}
	
	@Override
	public void createMainAgentContainer() {
		MainContainerRuntimeCallbackHandler callbackHandler = new MainContainerRuntimeCallbackHandler();
		runtimeService.createMainAgentContainer(callbackHandler);
	}

	@Override
	public void createAgentContainer(String mainContainerHostAddress) {
		this.mainContainerHostAddress = mainContainerHostAddress;
		retryCount++;
		ContainerRuntimeCallbackHandler callbackHandler = new ContainerRuntimeCallbackHandler();
		runtimeService.createAgentContainer(getProfile(mainContainerHostAddress), callbackHandler);
	}

	private Profile getProfile(String mainContainerHostAddress) {
		Properties properties = new Properties();
		properties.setProperty(Profile.MAIN, Boolean.FALSE.toString());
		properties.setProperty(Profile.MAIN_HOST, mainContainerHostAddress);
		properties.setProperty(Profile.MAIN_PORT, String.valueOf(JADE_DEFAULT_PORT));

		return new ProfileImpl(properties);
	}

	private class MainContainerRuntimeCallbackHandler extends RuntimeCallback<AgentContainerHandler> {
		@Override
		public void onSuccess(AgentContainerHandler result) {
			JadeStarterImpl.this.onSuccess(result, runtimeService);
		}
		@Override
		public void onFailure(Throwable throwable) {
			// errore di connessione del main container
			//FIXME al momento non faccio niente
		}
	}

	private class ContainerRuntimeCallbackHandler extends RuntimeCallback<AgentContainerHandler> {
		@Override
		public void onSuccess(AgentContainerHandler result) {
			JadeStarterImpl.this.onSuccess(result, runtimeService);
		}
		@Override
		public void onFailure(Throwable throwable) {
			// errore di connessione al main container, deve ritentare
			// di avviare il container per N volte, se non ci riesce allora errore
			if(retryCount < NUM_MAX_RETRY) {
				try {
					Thread.sleep(DELAY_MAX);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					logger.log(Level.INFO, "Jade Starter Thread.sleep() error");
				} 
				// ci riprova
				JadeStarterImpl.this.createAgentContainer(mainContainerHostAddress);
			} else {
				// nada, non si collega
				JadeStarterImpl.this.onFailure(throwable);
			}
		}
	}
}
