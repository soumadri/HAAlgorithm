package com.filecluster.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvFileSource;

import com.filecluster.graph.Graph;

@ExtendWith(TimingExtension.class)
class TestGraph {
	Graph graph;

	/**
	 * Setup a base graph +---+ +---+ | 1 +------------------------+ 2 +----+ +-+-+
	 * +--+-+ | | | | | | | | | | | | | | | +---+ | +-+--+ | +-------| 5
	 * +--------------------+ 4 | | +---+ | +-+--+ | | | | | | | | | | | | +-+-+ | |
	 * | 3 +--------------------------+------+ +---+
	 */
	@BeforeEach
	void initGraph() {
		graph = new Graph();

		graph.addNode("node1");
		graph.addNode("node2");
		graph.addNode("node3");
		graph.addNode("node4");
		graph.addNode("node5");

		graph.addEdge("node1", "node2");
		graph.addEdge("node1", "node5");
		graph.addEdge("node2", "node3");
		graph.addEdge("node2", "node4");
		graph.addEdge("node3", "node4");
		graph.addEdge("node4", "node5");
	}

	@AfterEach
	void cleanupGraph() {
		graph.removeAll();
		graph = null;
	}

	/**
	 * Check added nodes exists in the graph
	 */
	@Test
	void testNodeExist() {
		assertEquals(true, (graph.hasNode("node1") && graph.hasNode("node2") && graph.hasNode("node3")
				&& graph.hasNode("node4") && graph.hasNode("node5")));
	}

	/**
	 * Check non-existent nodes
	 */
	@Test
	void testNodeDoesNotExist() {
		assertEquals(false, graph.hasNode("node10"));
	}

	/**
	 * Check existing edges
	 */
	@Test
	void testEdgeExist() {
		assertEquals(true,
				(graph.hasEdge("node1", "node2") && graph.hasEdge("node1", "node5") && graph.hasEdge("node4", "node5")
						&& graph.hasEdge("node2", "node3") && graph.hasEdge("node2", "node4")
						&& graph.hasEdge("node3", "node4")));
	}

	/**
	 * Check non-existing edges
	 */
	@Test
	void testEdgeDoesNotExist() {
		assertEquals(false, graph.hasEdge("node1", "node3"));
	}

	/**
	 * Check edge addition for non-existing node
	 */
	@Test
	void testNonExistingEdgeAdd() {
		graph.addEdge("node1", "node6");

		assertEquals(true, (graph.hasEdge("node1", "node6") && graph.hasEdge("node6", "node1")));
	}

	/**
	 * Check node removal
	 */
	@Test
	void testNodeRemoval() {
		graph.removeNode("node1");

		assertEquals(false, (graph.hasEdge("node1", "node2") && graph.hasEdge("node1", "node5")));
	}

	/**
	 * Check connected nodes
	 */
	@Test
	void testConnectedNodes() {
		HashSet<String> nodes = graph.getConnectedNodes("node1");
		String[] expectedNodes = { "node2", "node5" };
		String[] notExpectedNodes = { "node4", "node3" };

		assertEquals(true, (nodes.containsAll(Arrays.asList(expectedNodes))
				&& !nodes.containsAll(Arrays.asList(notExpectedNodes))));
	}

	/**
	 * Check connected nodes with filter node
	 */
	@Test
	void testConnectedNodesWithFilter() {
		HashSet<String> nodes = graph.getConnectedNodes("node1", "node5");
		String[] expectedNodes = { "node2" };
		String[] notExpectedNodes = { "node4", "node3", "node5" };

		assertEquals(true, (nodes.containsAll(Arrays.asList(expectedNodes))
				&& !nodes.containsAll(Arrays.asList(notExpectedNodes))));
	}

	/**
	 * Check connected nodes with filter node that does not exist
	 */
	@Test
	void testConnectedNodesWithNonExistingFilterNode() {
		HashSet<String> nodes = graph.getConnectedNodes("node1", "node6");
		String[] expectedNodes = { "node2", "node5" };
		String[] notExpectedNodes = { "node4", "node3" };

		assertEquals(true, (nodes.containsAll(Arrays.asList(expectedNodes))
				&& !nodes.containsAll(Arrays.asList(notExpectedNodes))));
	}

	/**
	 * Check connected nodes on non-existing node
	 */
	@Test
	void testConnectedNodesOfNonExistingNode() {
		HashSet<String> nodes = graph.getConnectedNodes("node6");

		assertEquals(null, nodes);
	}

	/**
	 * Check connected filter nodes with filter node that does not exist
	 */
	@Test
	void testConnectedFilteredNodesOfNonExistingNode() {
		HashSet<String> nodes = graph.getConnectedNodes("node6", "node1");

		assertEquals(null, nodes);
	}

	/**
	 * Check disconnected node
	 */
	@Test
	void testDisconnectedNode() {
		graph.addNode("node6");

		assertEquals(0, graph.getConnectedNodes("node6").size());
	}

	/**
	 * Check unicode support for filename
	 */
	@Test
	void testNodeName() {
		graph.addNode("அΨ台北.txt");

		assertEquals(true, graph.hasNode("அΨ台北.txt"));
	}

	/**
	 * Check many number of node additions
	 */
	@ParameterizedTest
	@CsvFileSource(resources = "/hugeNodeList.csv")
	void testManyNumberOfNodeAdditions(ArgumentsAccessor arguments) {
		int size = 300;
		int counter = 0;
		while (counter < size) {
			graph.addNode(arguments.getString(counter));
			counter++;
		}

		assertEquals(305, graph.getNodeCount());
	}

	/**
	 * Check many number of node additions
	 */
	@Test
	void testHugeNumberOfNodeAdditions() {
		int size = 200000;
		int counter = 6; // 5 nodes are already there in the base graph
		while (counter <= size) {
			
			graph.addNode("node" + counter);
			counter++;
		}

		// Create edges to (node number + 3) for half of the nodes in graph
		counter = 1;
		while (counter <= Math.ceil(size/2)) {
			String source = "node" + counter;
			String destination = "node" + (counter + 3);
			
			graph.addEdge(source, destination);
			counter++;
		}
		
		assertEquals(size, graph.getNodeCount());
		
		//Connection should exist from node to node number+3
		assertEquals(true, (
				graph.hasEdge("node10", "node13") &&
				graph.hasEdge("node100000", "node100003") 
				));
	}
}
