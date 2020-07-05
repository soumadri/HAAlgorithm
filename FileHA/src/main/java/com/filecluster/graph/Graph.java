package com.filecluster.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph {
	Map<String, HashSet<String>> graph;

	public Graph() {
		graph = new HashMap<String, HashSet<String>>();
	}

	public void addNode(String node) {
		if (!hasNode(node))
			graph.put(node, new HashSet<String>());
	}

	public void addEdge(String source, String destination) {
		if (!hasNode(source))
			addNode(source);

		if (!hasNode(destination))
			addNode(destination);

		// We are using an undirected graph hence edges need to be bidirectional
		graph.get(source).add(destination);
		graph.get(destination).add(source);
	}

	public boolean hasNode(String node) {
		return graph.containsKey(node);
	}

	public boolean hasEdge(String source, String destination) {
		if (graph.get(source) != null) {
			return graph.get(source).contains(destination) && graph.get(destination).contains(source);
		} else {
			return false;
		}
	}
	
	public int getNodeCount() {
		return graph.size();
	}

	public void removeNode(String node) {
		HashSet<String> connectedNodes = graph.get(node);

		// Remove the given node from the adj. list of it's connected nodes
		for (String connectedNode : connectedNodes) {
			graph.get(connectedNode).remove(node);
		}

		graph.remove(node);
	}

	public void removeAll() {
		graph.keySet().removeAll(graph.keySet());
	}

	public HashSet<String> getConnectedNodes(String source) {
		if (hasNode(source))
			return graph.get(source);
		else
			return null;
	}

	public HashSet<String> getConnectedNodes(String source, String filterNode) {
		HashSet<String> filteredNodes = new HashSet<String>();
		HashSet<String> allConnectedNodes = getConnectedNodes(source);

		if (allConnectedNodes != null) {
			for (String node : allConnectedNodes) {
				if (!node.equals(filterNode))
					filteredNodes.add(node);
			}

			return filteredNodes;
		} else {
			return null;
		}

	}

	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();

		for (String node : graph.keySet()) {
			output.append(node + " => ");

			HashSet<String> connectedNodes = graph.get(node);
			for (String connectedNode : connectedNodes) {
				output.append(connectedNode + " | ");
			}

			output.append("\r\n");
		}

		return output.toString();
	}
}
