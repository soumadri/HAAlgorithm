package com.filecluster.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.filecluster.ha.HAFileCluster;
import com.filecluster.ha.HAInfoTriplet;

@ExtendWith(TimingExtension.class)
class HugeClusterTests {

	HAFileCluster cluster;

	@BeforeEach
	void setupCluster() {
		cluster = HAFileCluster.getCluster();

		// Add 15k hosts. 
		for (int i = 1; i <= 15000; i++) {
			cluster.addHost("host" + i);
		}
		
		//Add 10k files. ith file added to ith & i+3rd host OR. 
		for (int i = 1; i <= 10000; i++) {						
			cluster.addFile("file"+i, "host"+i);
			cluster.addFile("file"+i, "host"+(i+3));
		}
	}

	@AfterEach
	void clearCluster() {
		cluster.deleteCluster();
	}	

	@Test
	void testHAInHugeCluster() {
		String[] failedHosts = { "host20" };		
				
		List<HAInfoTriplet> triplets = cluster.performHA(failedHosts);		
		
		assertEquals(true, (
								(cluster.getHosts().size() == 14999) && // Dead host is removed from cluster
								!TestHelper.hasSourceHost(triplets, "file20", "host20") && // file20 should no longer be present on host20
								!TestHelper.hasDestinationHost(triplets, "file20", "host23") // host23 must not be file20's destination
							));
	}
}
