package com.filecluster.tests;

import java.util.List;

import com.filecluster.ha.HAInfoTriplet;

public class TestHelper {
	
	public static boolean hasSourceHost(List<HAInfoTriplet> triplets, String file, String host) {
		boolean result = false;

		for (HAInfoTriplet triplet : triplets) {
			if (triplet.getFileToBeCopied().equals(file) && triplet.getSourceHost().equals(host)) {
				result = true;
				break;
			}
		}

		return result;
	}

	public static boolean hasDestinationHost(List<HAInfoTriplet> triplets, String file, String host) {
		boolean result = false;

		for (HAInfoTriplet triplet : triplets) {
			if (triplet.getFileToBeCopied().equals(file) && triplet.getDestinationHost().equals(host)) {
				result = true;
				break;
			}
		}

		return result;
	}

	public static boolean hasFile(List<HAInfoTriplet> triplets, String file) {
		boolean result = false;

		for (HAInfoTriplet triplet : triplets) {
			if (triplet.getFileToBeCopied().equals(file)) {
				result = true;
				break;
			}
		}

		return result;
	}
}
