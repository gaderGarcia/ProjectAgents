package edu.agents;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import massim.javaagents.agents.MarsUtil;
import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.Message;
import eis.iilang.Action;
import eis.iilang.Percept;
import massim.javaagents.Agent;


public class RepairerAgent extends Agent{
int rechargeSteps = 0;
	
	public RepairerAgent(String name, String team) {
		super(name, team);
	}

	@Override
	public void handlePercept(Percept p) {
	}

	@Override
	public Action step() {

		if ( rechargeSteps > 0 ) {
			rechargeSteps --;
			println("recargando...");
			return MarsUtil.skipAction();
		}
		
		Collection<Message> messages = getMessages();
		Vector<String> needyAgents = new Vector<String>();
		for ( Message msg : messages ) {
			if (((LogicBelief)msg.value).getPredicate().equals("iAmDisabled"))
				needyAgents.add(msg.sender);
		}
		
		if ( needyAgents.size() == 0 ) {
			println("agusto, no tengo nada que hacer");
			return MarsUtil.skipAction();
		}

		println("estas pobres almas necesitan ayuda " + needyAgents);
		
		Collection<Percept> percepts = getAllPercepts();
		String position = null;
		for ( Percept p : percepts ) {
			if ( p.getName().equals("lastActionResult") && p.getParameters().get(0).toProlog().equals("failed") ) {
				println("fuuuuck! mi accion anterior fallo. recargando...");
				rechargeSteps = 10;
				return MarsUtil.skipAction();
			} 
			if ( p.getName().equals("position") ) {
				position = p.getParameters().get(0).toString();
			}
		}
		
		// a needy one on the same vertex
		for ( Percept p : percepts ) {
			if ( p.getName().equals("visibleEntity") ) {
				String ePos = p.getParameters().get(1).toString();
				String eName = p.getParameters().get(0).toString();
				if ( ePos.equals(position) && needyAgents.contains(eName) ) {
					println("Voy a reparar  " + eName);
					MarsUtil.repairAction(eName);
				}
			}
		}
		
		// maybe on an adjacent vertex?
		Vector<String> neighbors = new Vector<String>();
		for ( Percept p : percepts ) {
			if ( p.getName().equals("visibleEdge") ) {
				String vertex1 = p.getParameters().get(0).toString();
				String vertex2 = p.getParameters().get(1).toString();
				if ( vertex1.equals(position) ) neighbors.add(vertex2);
				if ( vertex2.equals(position) ) neighbors.add(vertex1);
			}
		}
		for ( Percept p : percepts ) {
			if ( p.getName().equals("visibleEntity") ) {
				String ePos = p.getParameters().get(1).toString();
				String eName = p.getParameters().get(0).toString();
				if ( neighbors.contains(ePos) && needyAgents.contains(eName) ) {
					println("Voy a reparar " + eName + ". primero me largo a " + ePos +".");
					MarsUtil.gotoAction(ePos);
				}
			}
		}
		
		// goto neighbors
		if ( neighbors.size() == 0 ) {
			println("Ah caray!, no se quienes son mis vecinos");
			return MarsUtil.skipAction();
		}
		Collections.shuffle(neighbors);
		String neighbor = neighbors.firstElement();
		println("Ire a " + neighbor);
		return MarsUtil.gotoAction(neighbor);

	}
}
