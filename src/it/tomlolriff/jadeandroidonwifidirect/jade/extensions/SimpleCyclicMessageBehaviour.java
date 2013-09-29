package it.tomlolriff.jadeandroidonwifidirect.jade.extensions;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class SimpleCyclicMessageBehaviour extends CyclicBehaviour implements ReceiveACLMessage {
	private static final long serialVersionUID = 1L;
	private MessageTemplate template;
	public SimpleCyclicMessageBehaviour(MessageTemplate template) {
		this.template = template;
	}

	@Override
	public void action() {
		/*
		 * Asynchronous, selective receive.
		 */
		ACLMessage msg = myAgent.receive(template);
		if (msg != null) {
			innerAction(msg);
		} else {
			block();
		}
	}
}
