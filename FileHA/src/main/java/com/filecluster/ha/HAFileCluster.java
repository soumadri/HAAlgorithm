package com.filecluster.ha;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import com.filecluster.exception.ExceptionMessages;
import com.filecluster.exception.HostNotFoundException;
import com.filecluster.exception.InvalidNameException;
import com.filecluster.exception.TooFewHostsException;
import com.filecluster.exception.TooManyCopiesException;
import com.filecluster.exception.TooManyFailedHostException;
import com.filecluster.graph.Graph;

public class HAFileCluster {
	Graph clusterGraph;
	Set<String> files, hosts; // To differentiate between files and hosts nodes the cluster graph

	private static final Logger logger = Logger.getLogger(HAFileCluster.class.getName());

	private static final int MIN_HOSTS_COUNT = 3;
	private static final String[] FILE_EXT_WHITELIST = { "txt", "jpg", "png", "html", "css" };

	static HAFileCluster haFileCluster = null;

	private HAFileCluster() {
		clusterGraph = new Graph();
		files = new HashSet<String>();
		hosts = new HashSet<String>();
	}

	public static HAFileCluster getCluster() {
		if (haFileCluster == null)
			haFileCluster = new HAFileCluster();

		return haFileCluster;
	}

	/**
	 * Remove all the failed nodes from hosts list (to avoid incorrect host
	 * selection) For each failed nodes Retrieve the list of files present on that
	 * host For each file hostWithCopies := Get connected nodes of the file from the
	 * cluster graph excluding failed host sourceHost := hostWithCopies[0], as it is
	 * guaranteed that a file will be always present in 2 hosts exxactly
	 * possibleDestinationHosts := Get set difference of all hosts list and
	 * hostWithCopies selectedTargetHost := Make a random selection on
	 * possibleDestinationHosts list Add the file to the host Remove the failed host
	 * from the cluster
	 */
	public List<HAInfoTriplet> performHA(String[] failedHosts) {
		List<HAInfoTriplet> haTriplets = new ArrayList<HAInfoTriplet>();
		HashSet<String> failedHostList =  new HashSet<String>(Arrays.asList(failedHosts));
		
		if (failedHostList.size() > 2) {

			throw new TooManyFailedHostException(ExceptionMessages.TOO_MANY_FAILED_HOST);

		} else if (hosts.containsAll(failedHostList)
				&& (hosts.size() - failedHostList.size()) < MIN_HOSTS_COUNT) {

			// There are not enough left over hosts to copy the file, so exit
			throw new TooFewHostsException(ExceptionMessages.TOO_FEW_HOSTS);

		} else {

			// Remove all the failed nodes from hosts list
			hosts.removeAll(failedHostList);

			for (String failedHost : failedHosts) {
				if (!clusterGraph.hasNode(failedHost)) {
					logger.severe(failedHost + " : " + ExceptionMessages.NO_HOST_FOUND + ". Skipping HA activity.");
					continue;
				}

				// Retrieve the list of files present on that host
				HashSet<String> filesOnFailedHost = clusterGraph.getConnectedNodes(failedHost);

				for (String fileToBeCopied : filesOnFailedHost) {
					// Get connected nodes of the file from the cluster graph excluding failed host
					HashSet<String> hostsWithCopies = clusterGraph.getConnectedNodes(fileToBeCopied, failedHost);
					
					// For a given file if both the hosts with it's copy fails
					// we can't copy that file. Hence log it and continue for
					// rest of the files
					if(failedHostList.containsAll(hostsWithCopies)) {
						logger.severe("Both source hosts for file: " + fileToBeCopied + " have failed. Skipping...");
						continue;
					}
					
					String sourceHost = (String) hostsWithCopies.toArray()[0];			

					/*
					 * To perform set difference and then random selection we will remove the
					 * sourceHost from the hosts list (not the cluster graph), then make selection
					 * and again add it back. This will avoid expensive set filtering, as addition
					 * and removal in HashSet is O(1) operation
					 */
					hosts.remove(sourceHost);
					String destinationHost = makeRandomSelection(hosts);
					hosts.add(sourceHost);

					// Add the file to the randomly selected host
					addFileToHost(fileToBeCopied, destinationHost);

					HAInfoTriplet triplet = new HAInfoTriplet();
					triplet.setFileToBeCopied(fileToBeCopied);
					triplet.setSourceHost(sourceHost);
					triplet.setDestinationHost(destinationHost);

					haTriplets.add(triplet);
				}

				// Finally remove the failed node from cluster graph
				clusterGraph.removeNode(failedHost);
			}
		}

		return haTriplets;
	}

	public void addHost(String host) {
		hosts.add(host);
		clusterGraph.addNode(host);
	}

	private boolean isValidFileExtension(String filename) {
		if (filename.contains(".")) {
			String extension = filename.substring(filename.lastIndexOf(".") + 1);
			return Arrays.asList(FILE_EXT_WHITELIST).contains(extension);
		} else {
			return true; // Assume this could be a hostname or file without extension
		}
	}

	private void addFileToHost(String file, String host) {

		if (file.equals(host) || !isValidFileExtension(file))
			throw new InvalidNameException(ExceptionMessages.INVALID_NAME);

		if (!hosts.contains(host))
			throw new HostNotFoundException(ExceptionMessages.NO_HOST_FOUND);

		if (hosts.size() < MIN_HOSTS_COUNT)
			throw new TooFewHostsException(ExceptionMessages.TOO_FEW_HOSTS);

		files.add(file);
		clusterGraph.addEdge(file, host);

		copy(file, host);
	}

	public void addFile(String file, String host) {
		if (clusterGraph.hasNode(file) && clusterGraph.getConnectedNodes(file).size() == 2)
			throw new TooManyCopiesException(ExceptionMessages.TOO_MANY_COPIES);
		else
			addFileToHost(file, host);
	}

	private void copy(String file, String host) {
		// Dummy function to mimic file copy operation
		logger.fine("Copying " + file + " to " + host);
	}

	public void removeHost(String host) {
		if (clusterGraph.hasNode(host))
			clusterGraph.removeNode(host);
	}

	public void deleteCluster() {
		clusterGraph.removeAll();
	}

	public Set<String> getHosts() {
		return hosts;
	}

	public Set<String> getHostsForFile(String fileName) {
		return clusterGraph.getConnectedNodes(fileName);
	}

	private String makeRandomSelection(Set<String> list) {
		Random random = new Random();
		int selectedIndex = random.nextInt(list.size());
		int counter = 0;
		String selectedItem = "";

		for (String item : list) {
			if (counter == selectedIndex)
				selectedItem = item;
			counter++;
		}

		return selectedItem;
	}

	@Override
	public String toString() {
		return clusterGraph.toString();
	}
}
