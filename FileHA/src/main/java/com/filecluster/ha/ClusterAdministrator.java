package com.filecluster.ha;

public class ClusterAdministrator {

	public static void main(String[] args) {
		HAFileCluster cluster = HAFileCluster.getCluster();

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

		System.out.println("Cluster before HA");
		System.out.println("-----------------------------");
		System.out.println(cluster.toString());
		System.out.println("-----------------------------");

		String[] failedHosts = { "host2" };
		 
		cluster.performHA(failedHosts);		

        System.out.println("Cluster after HA");
		System.out.println("-----------------------------");
		System.out.println(cluster.toString());
		System.out.println("-----------------------------");
				
	}

}
