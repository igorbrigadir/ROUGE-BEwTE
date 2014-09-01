package mathalgo;

import java.util.LinkedList;
import java.util.List;

import foal.map.IntObjectHashMap;

public class Graph {
	
	public static class Edge {
		
		public double weight;
		public int from;
		public int to;
		
		public Edge(int from, int to, double weight) {
			this.from = from;
			this.to = to;
			this.weight = weight;
		}
		
		public String toString() {
			return "(" + from + ", " + to + ", " + weight + ")";
		}
		
	}
	
	private int mNumVertices;
	private IntObjectHashMap<List<Edge>> mVertexToOutgoingEdges;
	private List<Edge> mEdges = new LinkedList<Edge>();
	
	public Graph(int numVertices) {
		mVertexToOutgoingEdges = new IntObjectHashMap<List<Edge>>(numVertices);
		mNumVertices = numVertices;
	}
	
	public List<Edge> getAllEdges() {
		return mEdges;
	}
	
	public List<Edge> getOutgoingEdges(int v) {
		return mVertexToOutgoingEdges.get(v); 
	}
	
	public int getNumVertices() {
		return mNumVertices;
	}
	
	public void addEdge(Edge e) {
		List<Edge> outgoingEdges = mVertexToOutgoingEdges.get(e.from);
		if(outgoingEdges == null) {
			mVertexToOutgoingEdges.put(e.from, outgoingEdges = new LinkedList<Edge>());
		}
		outgoingEdges.add(e);
		mEdges.add(e);
		//System.err.println("Adding: " + e);
	}
	
	public void removeEdge(Edge e) {
		List<Edge> outgoingEdges = mVertexToOutgoingEdges.get(e.from);
		outgoingEdges.remove(e);
		mEdges.remove(e);
		//System.err.println("Removing: " + e);
	}
	
	public void printGraph() {
		for(int i = 0; i < mNumVertices; i++) {
			List<Edge> outgoingEdges = mVertexToOutgoingEdges.get(i);
			System.err.println("Vert: " + i);
			if(outgoingEdges != null) {
				for(Edge e : outgoingEdges) {
					System.err.println(e.toString());
				}
			}
		}
	}
	
}