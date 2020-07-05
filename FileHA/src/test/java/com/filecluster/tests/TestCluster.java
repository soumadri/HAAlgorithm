package com.filecluster.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.filecluster.exception.TooFewHostsException;
import com.filecluster.exception.TooManyFailedHostException;
import com.filecluster.ha.HAFileCluster;
import com.filecluster.ha.HAInfoTriplet;

@ExtendWith(TimingExtension.class)
class TestCluster {

	HAFileCluster cluster;

	@BeforeEach
	void setupCluster() {
		cluster = HAFileCluster.getCluster();

		cluster.addHost("host1");
		cluster.addHost("host2");
		cluster.addHost("host3");
		cluster.addHost("host4");

		cluster.addFile("file1", "host1");
		cluster.addFile("file2", "host1");
		cluster.addFile("file4", "host1");
		cluster.addFile("file5", "host1");

		cluster.addFile("file2", "host2");
		cluster.addFile("file3", "host2");

		cluster.addFile("file1", "host3");
		cluster.addFile("file3", "host3");
		cluster.addFile("file4", "host3");

		cluster.addFile("file5", "host4");
	}

	@AfterEach
	void clearCluster() {
		cluster.deleteCluster();
	}

	@Test
	void testSingleNodeFailure() {
		String[] failedHosts = { "host2" };
		String[] expectedHostsAfterFailure = { "host1", "host3", "host4" };

		List<HAInfoTriplet> triplets = cluster.performHA(failedHosts);

		assertEquals(true, (cluster.getHosts().containsAll(Arrays.asList(expectedHostsAfterFailure)) && // Dead host is
																										// removed from
																										// cluster
				(cluster.getHosts().size() == 3) && // Dead host is removed from cluster
				!TestHelper.hasSourceHost(triplets, "file2", "host2") && // file2 is no longer present on host2
				!TestHelper.hasSourceHost(triplets, "file3", "host2") // file3 is no longer present on host2
		));
	}

	@Test
	void testDestinationHost() {
		String[] failedHosts = { "host2" };

		List<HAInfoTriplet> triplets = cluster.performHA(failedHosts);

		assertEquals(true, (!TestHelper.hasDestinationHost(triplets, "file2", "host2") && // failed host cannot be
																							// chosen as
		// destination
				!TestHelper.hasDestinationHost(triplets, "file2", "host1") // source host cannot be the destination
		));
	}

	@Test
	void testNumberOfFileCopiesAfterHA() {
		String[] failedHosts = { "host2" };

		cluster.performHA(failedHosts);

		assertEquals(2, cluster.getHostsForFile("file2").size());
		assertEquals(2, cluster.getHostsForFile("file3").size());
	}

	@Test
	void testTooManyFailedHosts() {
		String[] failedHosts = { "host2", "host1", "host3" };

		assertThrows(TooManyFailedHostException.class, () -> {
			cluster.performHA(failedHosts);
		});
	}

	@Test
	void testTooFewHosts() {
		String[] failedHosts = { "host2", "host1" };

		assertThrows(TooFewHostsException.class, () -> {
			cluster.performHA(failedHosts);
		});
	}

	@Test
	void testInvalidHost() {
		String[] failedHosts = { "host2", "host10" }; // host10 does not exist in the cluster
		String[] expectedHostsAfterFailure = { "host1", "host3", "host4" }; // Program must log problem with host10 and
																			// continue with rest of the HA

		cluster.performHA(failedHosts);

		assertEquals(true, (cluster.getHosts().containsAll(Arrays.asList(expectedHostsAfterFailure)) && // Dead host is
																										// removed from
																										// cluster
				(cluster.getHosts().size() == 3) && // Dead host is removed from cluster
				!cluster.getHostsForFile("file2").contains("host2") && // file2 is no longer present on host2
				!cluster.getHostsForFile("file3").contains("host2") // file2 is no longer present on host2
		));
	}

	@Test
	void testMultiHostFailures() {
		String[] failedHosts = { "host2", "host3" };
		String[] expectedHostsAfterFailure = { "host1", "host5", "host4" };

		cluster.addHost("host5");

		List<HAInfoTriplet> triplets = cluster.performHA(failedHosts);

		assertEquals(true, (cluster.getHosts().containsAll(Arrays.asList(expectedHostsAfterFailure)) && // Dead host is
																										// removed from
																										// cluster
				(cluster.getHosts().size() == 3) && // Dead host is removed from cluster
				!TestHelper.hasSourceHost(triplets, "file2", "host2") && // file2 is no longer present on host2
				!TestHelper.hasSourceHost(triplets, "file3", "host2") && // file3 is no longer present on host2
				TestHelper.hasSourceHost(triplets, "file2", "host1") && // file2 is being copied from host1
				!TestHelper.hasFile(triplets, "file3") // file3 was on both failed hosts, so it would no longer be
														// present
		));
	}

	void testMultipleFailuresInSequence() {
		String[] failedHosts = { "host2" };
		String[] expectedHostsAfterFailureRound1 = { "host1", "host5", "host4", "host3" };
		String[] expectedHostsAfterFailureRound2 = { "host1", "host5", "host4" };

		cluster.addHost("host5");

		// 1st round of failure
		List<HAInfoTriplet> tripletsRound1 = cluster.performHA(failedHosts);

		assertEquals(true, (cluster.getHosts().containsAll(Arrays.asList(expectedHostsAfterFailureRound1)) && // Dead
																												// host
																												// is
		// removed from
		// cluster
				(cluster.getHosts().size() == 4) && // Dead host is removed from cluster
				!TestHelper.hasSourceHost(tripletsRound1, "file2", "host2") && // file2 is no longer present on host2
				!TestHelper.hasSourceHost(tripletsRound1, "file3", "host2") // file3 is no longer present on host2
		));

		// 2nd round of failure
		failedHosts[0] = "host3"; // Now host3 failed
		List<HAInfoTriplet> tripletsRound2 = cluster.performHA(failedHosts);

		assertEquals(true, (cluster.getHosts().containsAll(Arrays.asList(expectedHostsAfterFailureRound2)) && // Dead
																												// host
																												// is
		// removed from
		// cluster
				(cluster.getHosts().size() == 3) && // Dead host is removed from cluster
				!TestHelper.hasSourceHost(tripletsRound2, "file1", "host3") && // file1 is no longer present on host3
				!TestHelper.hasSourceHost(tripletsRound2, "file4", "host3") // file4 is no longer present on host3
		));
	}
}
