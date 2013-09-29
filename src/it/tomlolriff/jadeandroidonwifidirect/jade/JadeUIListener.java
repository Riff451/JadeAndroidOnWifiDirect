package it.tomlolriff.jadeandroidonwifidirect.jade;

import it.tomlolriff.jadeandroidonwifidirect.jade.types.AgentInfo;
import android.os.Handler;

public abstract class JadeUIListener implements JadeListener {
	
	private Handler myHandler = new Handler();
	private Throwable currentThrowable;

	@Override
	public final void onMainContainerSuccessfullyStarted(){
		myHandler.post(new Runnable() {
			
			@Override
			public void run() {
				doOnMainContainerSuccessfullyStarted();
			}
		});
	}
	@Override
	public final void onMainContainerFailureStarted(Throwable throwable){
		currentThrowable = throwable;
		myHandler.post(new Runnable() {
			
			@Override
			public void run() {
				doOnMainContainerFailureStarted(JadeUIListener.this.currentThrowable);
			}
		});
	}

	@Override
	public final void onContainerSuccessfullyStarted(){
		myHandler.post(new Runnable() {
			
			@Override
			public void run() {
				doOnContainerSuccessfullyStarted();
			}
		});
	}
	@Override
	public final void onContainerFailureStarted(Throwable throwable){
		currentThrowable = throwable;
		myHandler.post(new Runnable() {
			
			@Override
			public void run() {
				doOnContainerFailureStarted(JadeUIListener.this.currentThrowable);
			}
		});
	}

	@Override
	public final void onAgentSuccessfullyStarted(final AgentInfo agentInfo){
		myHandler.post(new Runnable() {
			
			@Override
			public void run() {
				doOnAgentSuccessfullyStarted(agentInfo);
			}
		});
	}
	@Override
	public final void onAgentFailureStarted(Throwable throwable){
		currentThrowable = throwable;
		myHandler.post(new Runnable() {
			
			@Override
			public void run() {
				doOnAgentFailureStarted(JadeUIListener.this.currentThrowable);
			}
		});
	}
	
	/**
	 * Richiamato a seguito di una creazione di un Main Container avvenuta con successo
	 */
	protected abstract void doOnMainContainerSuccessfullyStarted();
	/**
	 * Richiamato a seguito di una creazione di un Main Container fallita
	 * @param throwable {@link Throwable} - dettaglio dell'errore
	 */
	protected abstract void doOnMainContainerFailureStarted(Throwable throwable);
	
	/**
	 * Richiamato a seguito di una creazione di un Container avvenuta con successo
	 */
	protected abstract void doOnContainerSuccessfullyStarted();
	/**
	 * Richiamato a seguito di una creazione di un Container fallita
	 * @param throwable {@link Throwable} - dettaglio dell'errore
	 */
	protected abstract void doOnContainerFailureStarted(Throwable throwable);
	
	/**
	 * Richiamato a seguito di una creazione ed avvio di un Agent avvenute con successo
	 * @param {@link AgentInfo} - info dell'agente
	 */
	protected abstract void doOnAgentSuccessfullyStarted(AgentInfo agentInfo);
	/**
	 * Richiamato a seguito di una creazione ed avvio di un Agent fallite
	 * @param throwable {@link Throwable} - dettaglio dell'errore
	 */
	protected abstract void doOnAgentFailureStarted(Throwable throwable);

}
