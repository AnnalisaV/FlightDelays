package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	
	private Graph<Airport, DefaultWeightedEdge> grafo; 
	private Map<Integer, Airport> idMapAirport; 
	private ExtFlightDelaysDAO dao; 
	private Map<Airport, Airport> visita;
	
	public Model() {
		this.idMapAirport= new HashMap<>(); 
		this.dao=new ExtFlightDelaysDAO(); 
		//riempio la mappa 
		this.dao.loadAllAirports(idMapAirport); 
		
	}
	                     //minimo numero di airlines
	public void creaGrafo(int x) {
		
		this.grafo= new SimpleWeightedGraph<>(DefaultWeightedEdge.class); 
		
		//aggiunta dei vertici
		/* questo metodo e' fattibile perche' gli airport totali nella idMap non sono tantissimi*/
		for(Airport a : idMapAirport.values()) {
			if(dao.getAirlinesNum(a)> x) {
				//allora e' un vertice
				this.grafo.addVertex(a);            // altrimenti altro metodo: (scritto io quindi bo)
				                                   /* chiedo al DAO una Map<numero Airlines, idAirport>
				                                    * con query = "SELECT COUNT(DISTINCT airline_id), origin_airport_id 
                                                                   FROM flights 
                                                                   GROUP BY origin_airport_id"; 
                                                      poi la scorro e 
                                                      if(numero Airlines > x){
                                                      Airport a=idMapAirport.get(idAirport); 
                                                      grafo.addVertex(a)  ;
                                                      }           
				                                    */
			}
		}
		
		for (Rotta r : dao.getRotte(idMapAirport)) {
			//controllo che i vertici siano nel grafo visto che il dao mi da' tutte le possibili connessioni
	if(this.grafo.containsVertex(r.getPartenza()) && this.grafo.containsVertex(r.getDestinazione())) {
			DefaultWeightedEdge e= this.grafo.getEdge(r.getPartenza(), r.getDestinazione()); 
			if(e==null) {
				//non esiste ancora 
				Graphs.addEdgeWithVertices(this.grafo, r.getPartenza(), r.getDestinazione(), r.getPeso()); 
			}
			else {
				// c'e' gia' un arco fra i due vertici
				// controllo il peso
				double peso=this.grafo.getEdgeWeight(e); 
				double pesoNuovo= peso+ r.getPeso(); 
				this.grafo.setEdgeWeight(e, pesoNuovo); //aggiorno il peso di quell'arco
			}
		}
		
	}
		
	}

	public int vertexNumber() {
		return this.grafo.vertexSet().size(); 
	}
	
	public int edgeNumber() {
		return this.grafo.edgeSet().size(); 
	}
	
	/**
	 * Quali sono gli Airport presenti nel grafo
	 */
	public Collection<Airport> AirportDelGraph() {
		return this.grafo.vertexSet(); 
	}
	
	/**
	 * Lista di Airport che modella il percorso fra un {@code Airport} {@link partenza} e
	 * un {@code Airport} {@link destinazione}
	 */
	public List<Airport> trovaPercorso(Airport partenza, Airport destinazione){
		
		List<Airport> percorso= new ArrayList<>(); 
		
		//inizializzo (e pulisco per richiamarla piu' volte)
		this.visita=new HashMap<>();
		//visito il grafo e mano a mano nella visita tengo traccia del percorso
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it= new BreadthFirstIterator<>(this.grafo, partenza);  
		visita.put(partenza, null); //salvo il vertice di partenza 
		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>(){

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				
				Airport partenza= grafo.getEdgeSource(e.getEdge()); 
				Airport destinazione= grafo.getEdgeTarget(e.getEdge());
				
				if(!visita.containsKey(destinazione) && visita.containsKey(partenza)) {
					// non conosco la destinazione quindi essa e' la figlia 
					visita.put(destinazione, partenza); 
				}
				//non e' orientato quindi controllo inverso
				else if(!visita.containsKey(partenza) && visita.containsKey(destinazione)) {
					visita.put(partenza, destinazione); 
				}
				
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		//ma il grafo va visitato ugualmente 
		while(it.hasNext()) {
			it.next(); 
		}
		
		
		//controllo che gli Airport siano collegati o no
		if(!visita.containsKey(partenza) || !visita.containsKey(destinazione)) {
			//i due non sono collegati
			return null; 
		}
		//altrimenti ottengo il percorso 
		Airport step= destinazione; 
		while(!step.equals(partenza)) {
			// vado all'indietro, so che sono collegati quindi
			//posso iterare finche' non arrivo al punto di partenza
			percorso.add(step); 
			step = visita.get(step); // risalgo nella mappa prendendo il nodo padre corrispondente 
			
		}
		
		percorso.add(partenza); //altrimenti non entrando piu' nel while non viene considerata
		return percorso; 
	}
	
}


