# High Availibility Algorithm for file cluster
* The main data structure used for the HA algorithm is **Undirected Graph**. Graph allows us optimised insertion and removal to capture relationships between files and hosts in a cluster. 
* The graph implementation has been done using *Adjacancy List* technique
* To optimize the lookup, insertion, removal of nodes & edges, the adjacancy list is implmented with HashMap of HashSet
* HashMap allows O(1) lookup of any node (file & host) in the Graph
* HashSet as list of connected edges allows O(1) lookup, insertion & removal of connection between nodes

## Conceptual representation of the cluster graph
![Conceptual representation of cluster graph](https://raw.githubusercontent.com/soumadri/HAAlgorithm/master/HAGraph.png)

## Assumptions
* Only file name and host name are stored, no other properties are considered
* Not more than 2 hosts can fail at the same time
* Exactly 2 copies of each file will be managed on the server
* The program will log & skip any file's HA for which both the source hosts have gone down at the same time
* Filename & hostname cannot be same
* Minimum 3 hosts must be there in a cluster in order to do HA
* A whitelisting of file extension has been considered (if present)

## Algorithm analysis
* The main HA function is <code>HAFileCluster.performHA(String[] failedHosts)</code>

**Following is the high level algorithm (with runtime complexity)**

1. Maintain a set of files & hosts names list, so that we can lookup respective entities (this is in addition to the cluster graph)
2. Remove all the failed hosts from hosts list (to avoid incorrect host selection)
3. For each failed host 
`O(h), where h = number of failed hosts. Assuming failed hosts are <= 2, here h=2 ` 
	
    1. Retrieve the list of files present on that host
	  2. For each file `O(f), where f = number of files present on the given host`
          1. hostWithCopies := Get connected nodes of the file from the cluster graph excluding failed host `O(1) lookup in graph`
          2. sourceHost := hostWithCopies[0] `As it is guaranteed that a file will be always present in 2 hosts exactly`
          3. possibleDestinationHosts := Get set difference of all hosts list and hostWithCopies
          4. selectedTargetHost := Make a random selection in possibleDestinationHosts list `O(n), n = no. of hosts - 2 i.e. excluding sourceHost & failedHost`
              > NOTE: The random selection could have been O(1) if the hosts was a List instead of Set, but that will also make all lookup and removal O(n). Since, there are various lookups needed for the algorithm, the random selection is done on Set
          5. Add the file to the host `O(1) insertion in the graph`
    3. Remove the failed host from the cluster (including edges from files to the host) `O(f), where f = number of files present on the given host`

**Overall runtime complexity is:** `O(h x f x n), where h <= 2, f = number of files present on the given host and n = no. of hosts - 2`
> NOTE: If we assume, multiple hosts are not going down exactly at the same time and the bounds will improve to `O(f x n)`

**Space complexity**
* For the cluster the space complexity is `O(E + V)`
    > Where E = 4f, f = no. of files managed in the cluster and V = f + h, where h = no. of hosts in the cluster
    > As there can be exact 2 copies of each file, for undirected (actually bidirectional edges) graph it will be 2x2f = 4f
* For the additional 2 lists (for files and hosts) the space complexity is `O(V)`
    > Where V = f + h, f = no. of files managed in the cluster and h = no. of hosts in the cluster
    
 **Overall space complexity is:** `O(E + V)`
 
 ## Design pattern used
 * `Singleton` pattern has been used to store the cluster graph as the cluster should not have multiple instances
 
 ## Additional notes
 * The output of the algorithm is a `List<HAInfoTriplet>`, where HAInfoTriplet is an object holding a triplet of `<file to be copied>, <from which other source host file can be copied>, <random destination host which is not source host or the host that went down>`
 
 ## System requirements
 * Java 8
 
 ## Run
 * mvn test
