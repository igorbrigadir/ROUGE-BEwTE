package mathalgo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import foal.list.IntArrayList;
import foal.map.IntDoubleHashMap;
import foal.map.IntObjectHashMap;

/**
 * Solves weighted assignment problem
 * 
 * Needs some more description
 * 
 */
public class WeightedAssignmentSolver {
	
	public static int[][] assignment(int part1Size, 
									 int part2Size, 
									 IntObjectHashMap<IntDoubleHashMap> costMatrix)  {
		Set<Graph.Edge> flowEdges = new HashSet<Graph.Edge>();
		if(part1Size > 0) {
			int numNodes = part1Size + part2Size + 2;
			int sourceIndex = numNodes-2;
			int sinkIndex = numNodes-1;
			Graph g = new Graph(numNodes);
			IntArrayList part1Nodes = costMatrix.keys();
			int numPart1Nodes = part1Nodes.size();
			for(int p1 = 0; p1 < numPart1Nodes; p1++) {
				int i = part1Nodes.get(p1);
				IntDoubleHashMap links = costMatrix.get(i);
				IntArrayList part2Nodes = links.keys();
				int numPart2Nodes = part2Nodes.size();
				// 	Edges to source
				g.addEdge(new Graph.Edge(sourceIndex, i, 0.0));
				for(int p2 = 0; p2 < numPart2Nodes; p2++) {
					int j = part2Nodes.get(p2);
					// 	Cross edges
					double cost = links.get(j);
					if(cost > 0) {
						g.addEdge(new Graph.Edge(i, j+part1Size, -cost));
					}
				}
			}
			for(int i= 0; i < part2Size; i++) {
				// Edges to sink
				g.addEdge(new Graph.Edge(part1Size+i, sinkIndex, 0.0));
			}
		
			Graph.Edge[] backpointers = new Graph.Edge[numNodes];
			double[] weights = new double[numNodes];
			// Use BF initially because it can handle negative edge weights
			BellmanFord(g, sourceIndex, weights, backpointers);
			reweight(g, weights);
		
			int smallerPartition = Math.min(part1Size, part2Size);
			for(int i = 0; i < smallerPartition; i++) {
				Arrays.fill(weights, 0.0);
				Arrays.fill(backpointers, null);
				DjikstraFB(g, sourceIndex, weights, backpointers);
				reweight(g, weights);
			
				if(backpointers[sinkIndex] == null) {
					// Didn't find a path to the sink, must be finished
					break;
				}
			
				g.removeEdge(backpointers[sinkIndex]);
			
				Graph.Edge backpointer = backpointers[backpointers[sinkIndex].from];
				while(backpointer.from != sourceIndex) {
					if(flowEdges.contains(backpointer)) {
						flowEdges.remove(backpointer);
					}
					else {
						flowEdges.add(backpointer);
					}
				
					g.removeEdge(backpointer);
					int from = backpointer.from;
					backpointer.from = backpointer.to;
					backpointer.to = from;
					backpointer.weight = 0;
					g.addEdge(backpointer);

					backpointer = backpointers[from];
				}
				g.removeEdge(backpointer);
			}
		}
		int numSelectedEdges = flowEdges.size();
		int[][] result = new int[numSelectedEdges][2];
		int i = 0;
		for(Graph.Edge e : flowEdges) {
			result[i][0] = e.to;
			result[i][1] = e.from-part1Size;
			i++;
		}
		return result;
	}
	
	private static void reweight(Graph g, double[] weights) {
		for(Graph.Edge e : g.getAllEdges()) {
			e.weight = e.weight + weights[e.from] - weights[e.to];
		}
	}
	
	private static void DjikstraFB(Graph graphs, int sourceIndex, double[] weights, Graph.Edge[] edges) {
		final int numVertices = graphs.getNumVertices();
		Arrays.fill(weights, Double.MAX_VALUE);
		
		final FHeap heap = new FHeap(numVertices, weights);
		for(int v = 0; v < numVertices; v++) {
			heap.insert(v);			
		}
		heap.decreaseKey(sourceIndex, 0.0);
		while(!heap.isEmpty()) {
			int v = heap.extractMin();
			if(v != sourceIndex && edges[v] == null) {
				return;
			}
			List<Graph.Edge> outgoingEdges = graphs.getOutgoingEdges(v);
			if(outgoingEdges != null) {
				for(Graph.Edge e : outgoingEdges) {
					int to = e.to;
					double P = weights[v] + e.weight;
					if(P < weights[to]) {
						heap.decreaseKey(to, P);
						edges[to] = e;
					}
				}
			}
		}
	}
	
	private static void BellmanFord(Graph graph, int sourceIndex, double[] weights, Graph.Edge[] edges) {
		final int numVertices = graph.getNumVertices();
		int counter = 0;
		Arrays.fill(weights, Double.MAX_VALUE);
		final List<Integer> queue = new LinkedList<Integer>();
		weights[sourceIndex] = 0.0;
		queue.add(sourceIndex);
		queue.add(numVertices);
		while(queue.size() != 0) {
			int v;
			while((v = queue.remove(0)) == numVertices) {
				if(counter++ > numVertices) {
					return;
				}
				queue.add(numVertices);
			}
			List<Graph.Edge> outgoingEdges = graph.getOutgoingEdges(v);
			if(outgoingEdges != null) {
				for(Graph.Edge e : outgoingEdges) {
					int to = e.to;
					double P = weights[v] + e.weight;
					if(P < weights[to]) {
						weights[to] = P;
						queue.add(to);
						edges[to] = e;
					}
				}
			}
		}
	}
	
	
}