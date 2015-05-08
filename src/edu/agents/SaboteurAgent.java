package edu.agents;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

import apltk.interpreter.data.LogicBelief;
import apltk.interpreter.data.LogicGoal;
import eis.iilang.Action;
import eis.iilang.Percept;
import massim.javaagents.Agent;
import massim.javaagents.agents.MarsUtil;

public class SaboteurAgent extends Agent {

	public SaboteurAgent(String name, String team) {
		super(name, team);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handlePercept(Percept p) {
	}

	@Override
	public Action step() {

		Action act = null;
		
		handlePercepts();
		
		// 1. recharging
		act = planRecharge();
		if ( act != null ) return act;
		
		// 2. fight if possible
		act = planFight();
		if ( act != null ) return act;
		
		// 3. random walking
		act = planRandomWalk();
		if ( act != null ) return act;

		return MarsUtil.skipAction();
		
	}

	private void handlePercepts() {

		String position = null;
		Vector<String> neighbors = new Vector<String>();
		
		// check percepts
		Collection<Percept> percepts = getAllPercepts();
		//if ( gatherSpecimens ) processSpecimens(percepts);
		removeBeliefs("visibleEntity");
		removeBeliefs("visibleEdge");
		for ( Percept p : percepts ) {
			if ( p.getName().equals("step") ) {
				println(p);
			}
			else if ( p.getName().equals("visibleEntity") ) {
				LogicBelief b = MarsUtil.perceptToBelief(p);
				if ( containsBelief(b) == false ) {
					//println("I perceive an edge I have not known before");
					addBelief(b);
					//broadcastBelief(b);
				}
				else {
					//println("I already knew " + b);
				}
			}
			else if ( p.getName().equals("health")) {
				Integer health = new Integer(p.getParameters().get(0).toString());
				println("Mi nivel de cruda es: " +health );
				if ( health.intValue() == 0 ) {
					println("Estoy valiendo Mauser.. Ayuden cabrones");
					broadcastBelief(new LogicBelief("iAmDisabled"));
				}
			}
			else if ( p.getName().equals("position") ) {
				position = p.getParameters().get(0).toString();
				removeBeliefs("position");
				addBelief(new LogicBelief("position",position));
			}
			else if ( p.getName().equals("energy") ) {
				Integer energy = new Integer(p.getParameters().get(0).toString());
				removeBeliefs("energy");
				addBelief(new LogicBelief("energy",energy.toString()));
			}
			else if ( p.getName().equals("maxEnergy") ) {
				Integer maxEnergy = new Integer(p.getParameters().get(0).toString());
				removeBeliefs("maxEnergy");
				addBelief(new LogicBelief("maxEnergy",maxEnergy.toString()));
			}
			else if ( p.getName().equals("achievement") ) {
				println("No que no cabrones.. ya chingamos" + p);
			}
		}
		
		// again for checking neighbors
		this.removeBeliefs("neighbor");
		for ( Percept p : percepts ) {
			if ( p.getName().equals("visibleEdge") ) {
				String vertex1 = p.getParameters().get(0).toString();
				String vertex2 = p.getParameters().get(1).toString();
				if ( vertex1.equals(position) ) 
					addBelief(new LogicBelief("neighbor",vertex2));
				if ( vertex2.equals(position) ) 
					addBelief(new LogicBelief("neighbor",vertex1));
			}
		}	
	}
	
	private Action planRecharge() {

		LinkedList<LogicBelief> beliefs = null;
		
		beliefs =  getAllBeliefs("energy");
		if ( beliefs.size() == 0 ) {
				println("Semiase que ya ando pedo.. No se cuanta energia tengo");
				return MarsUtil.skipAction();
		}		
		int energy = new Integer(beliefs.getFirst().getParameters().firstElement()).intValue();

		beliefs =  getAllBeliefs("maxEnergy");
		if ( beliefs.size() == 0 ) {
				println("¿Cual es mi maximo poder??? ¿Que me hicieron putos?");
				return MarsUtil.skipAction();
		}		
		int maxEnergy = new Integer(beliefs.getFirst().getParameters().firstElement()).intValue();

		// if has the goal of being recharged...
		if ( goals.contains(new LogicGoal("beAtFullCharge")) ) {
			if ( maxEnergy == energy ) {
				println("Ya me avente un menudazo con un Clamato. Ahora si cabrones.. Agarrense..");
				removeGoals("beAtFullCharge");
			}
			else {
				println("recharging...");
				return MarsUtil.rechargeAction();
			}
		}
		// go to recharge mode if necessary
		else {
			if ( energy < maxEnergy / 3 ) {
				println("Me estan putiando.. Hagan paro.. ocupo relevo");
				goals.add(new LogicGoal("beAtFullCharge"));
				return MarsUtil.rechargeAction();
			}
		}	
		
		return null;
		
	}
	
	private Action planFight() {
		
		// get position
		LinkedList<LogicBelief> beliefs = null;
		beliefs =  getAllBeliefs("position");
		if ( beliefs.size() == 0 ) {
				println("Donde estoy??.. Me trajo el de la carretilla??");
				return MarsUtil.skipAction();
		}
		String position = beliefs.getFirst().getParameters().firstElement();

		// if there is an enemy on the current position then attack or defend
		Vector<String> enemies = new Vector<String>();
		beliefs = getAllBeliefs("visibleEntity");
		for ( LogicBelief b : beliefs ) {
			String name = b.getParameters().get(0);
			String pos = b.getParameters().get(1);
			String team = b.getParameters().get(2);
			if ( team.equals(getTeam()) ) continue;
			if ( pos.equals(position) == false ) continue;
			enemies.add(name);
		}
		if ( enemies.size() != 0 ) {
			println("Hay " + enemies.size() + " del otro bando donde estoy... No sean boleros putos!");
			if ( Math.round(Math.random()) % 2 == 0) {
				println("I will parry");
				return MarsUtil.parryAction();
			}
			else {
				Collections.shuffle(enemies);
				String enemy = enemies.firstElement();
				println("Agachate que te quedo Jabon.. Ahi te voy " + enemy);
				return MarsUtil.attackAction(enemy);
			}
		}
		
		// if there is an enemy on a neighboring vertex to there
		beliefs = getAllBeliefs("neighbor");
		Vector<String> neighbors = new Vector<String>();
		for ( LogicBelief b : beliefs ) {
			neighbors.add(b.getParameters().firstElement());
		}
		
		Vector<String> vertices = new Vector<String>();
		beliefs = getAllBeliefs("visibleEntity");
		for ( LogicBelief b : beliefs ) {
			//String name = b.getParameters().get(0);
			String pos = b.getParameters().get(1);
			String team = b.getParameters().get(2);
			if ( team.equals(getTeam()) ) continue;
			if ( neighbors.contains(pos) == false ) continue;
			vertices.add(pos);
		}
		if ( vertices.size() != 0 ) {
			println("Cuidado.. Hay " + vertices.size() + " vertices adjacentes con varios putos.. Seguro ha de ser el Jorge");
			Collections.shuffle(vertices);
			String vertex = vertices.firstElement();
			println("Caele pal vertice " + vertex);
			return MarsUtil.gotoAction(vertex);
		}
		
		return null;
	}
	
	private Action planRandomWalk() {

		LinkedList<LogicBelief> beliefs = getAllBeliefs("neighbor");
		Vector<String> neighbors = new Vector<String>();
		for ( LogicBelief b : beliefs ) {
			neighbors.add(b.getParameters().firstElement());
		}
		
		if ( neighbors.size() == 0 ) {
			println("Yo solo se que no se nada.. Saluuuud");
			return MarsUtil.skipAction();
		}
		
		// goto neighbors
		Collections.shuffle(neighbors);
		String neighbor = neighbors.firstElement();
		println("Vamos pal barrio " + neighbor);
		return MarsUtil.gotoAction(neighbor);
		
	}

}
