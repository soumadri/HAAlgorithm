package com.filecluster.ha;

public class HAInfoTriplet {
	String fileToBeCopied, sourceHost, destinationHost;

	public String getFileToBeCopied() {
		return fileToBeCopied;
	}

	public void setFileToBeCopied(String fileToBeCopied) {
		this.fileToBeCopied = fileToBeCopied;
	}

	public String getSourceHost() {
		return sourceHost;
	}

	public void setSourceHost(String sourceHost) {
		this.sourceHost = sourceHost;
	}

	public String getDestinationHost() {
		return destinationHost;
	}

	public void setDestinationHost(String destinationHost) {
		this.destinationHost = destinationHost;
	}
	
	
}
