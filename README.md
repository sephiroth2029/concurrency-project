# **Concurrency (CSC-564)**
## **Distributed SLIQ - Final Project**
### **Introduction**
In the past couple of decades, many software developers realized that hardware manufacturers are approaching the physical limits of computational components [[1]]. That realization increased the interest in obtaining the maximum benefit out of those components, and alternatives to the synchronous programming model gained the developer's community attention. Software developers indeed gained a performance boost by better utilizing their resources available, but it came with a cost: the complexity of the software increased and the debugging process became more challenging.

Data science is one of the fields where performance is paramount, since large volumes of data need to be processed in the shortest time frame possible, for the results to be relevant in their own context. This project presents a partially distributed implementation of SLIQ [2] aiming to improve the performance and the memory restrictions imposed by the algorithm.

### **Description of the problem**
SLIQ is a data mining classification algorithm which improves previous algorithms such as ID3 and C4.5 where all of the data needs to reside in memory and needs to be sorted on every iteration [3]. The core contribution of SLIQ is the introduction of two new data structures -class list and attribute list- which allow to persist to hard disk the attributes, sorting the instances only once and maintaining in memory only the class list. This overcame two problems:
1. The memory requirements were lowered from MxN, where M are the number of attributes and N the number of instances, to just N.
2. The processing was simplified by sorting at the beginning of the algorithm.

However, there are two possible improvements that can be done to SLIQ: distributing the class list and distributing the attribute processing. Both of these changes are explored in this project.

Firstly, the amount of information currently available can easily reach the terabytes order, which might require a vast amount of main memory for huge datasets. Other areas of software engineering moved from vertical scalability to horizontal, exploiting modern resources such as cloud computing. This project aims to overcome the main memory requirement by moving the class list to a distributed set, where it can grow horizontally as needed. 

Secondly, processing sequentially the attributes' metrics is unnecesary as they do not depend on each other. This project explores if the performance can be improved by executing these computations in asynchronously. The project does not implement distributed calls for the computations, but that would be an alternative to obtain a better performance for very large data sets. While it may be argued that hard disk communication is faster than the networked one, recent improvements in networking components are making that assumption irrelevant [4].

### **Background**
Newer data science tools and frameworks exploit the advancements both in modern hardware components, in optimized runtime environments and in the parallelization of algorithms.

Regarding hardware components, Horovod [[5]] is a framework to deploy single-GPU TensorFlow, Keras and PyTorch programs into multiple GPUs, hence obtaining a performance boost through the utilization of multiple fast-processing pieces of hardware. The restriction of this approach is that not all algorithms are compatible nor suitable with the aforementioned packages; their main focus is in neural networks which is a subset of the algorithms available.

As for optimized runtime environments, Intel released the *Deep Learning Deployment Toolkit*, a set of tools whose purpose is to process a trained model in order to improve it, through tasks such as the fusion of network layers and branch prunning, and to produce an optimized runtime interface for the specific hardware that will use the model. Similarly to Harovod, the Deep Learning Deployment Toolkit limits its usage to neural networks.

Shashikumar et al. [6] explored the parallelization of SLIQ, and followed a similar approach to the one applied in this project. They describe how SLIQ was an improvement to the existing tree-based algorithms for classification, as it only requires the class list to be in main memory while the attribute lists can be persisted in hard disk as long as they are ordered. However, the parallelization of SLIQ does not remove the memory requirements for large datasets and the access to disk could slow down the algorithm's processing.

Mohammed Zaki [7] describes in greater detail the mechanisms in which classification algorithms' performance can be improved. In specific, for SLIQ, he describes SLIQ/R which uses a replicated class list and SLIQ/D which uses a distributed class list. He points out that SLIQ/D had a better usage of memory -which is reasonable, as it is as big as the size of the partitions- and that SLIQ/R had a better performance. However, he does not provide metrics that allow to see how the second case reflects against the synchronous algorithm.

### **Implementation**
The implementation is composed of a standalone Atomix project and a Spring Boot project, composed of three submodules:

1. `sliq-impl` contains the generic parts of the SLIQ implementation. The construction of the algorithm is not complete, as the main focus of this project is to analyze the feasiblity of the implementation and to compare equivalent implementations of the algorithm.
2. `sync-impl` is the synchronous implementation of the `AttributeFileProcessors`, which makes the algorithm to execute as the original SLIQ implementation.
3. `distributed-impl` is the implementation using Atomix [[8]].

The following diagram shows the design of the modules.

![Missing graph][101]

The `sliq-impl` module is contained inside each of the implementations. This allows to execute the same algorithm and switch only the parts of interest for the analysis. `sync-impl` and `distributed-impl` provide the implementations of `AttributeFileProcessor` which decides how and where to store the values of each attribute. `AttributeFileProcessorHdd` persists the attribute list to hard disk and then sorts it by loading all the attribute instances in memory and executing Java's standard sorting algorithm. `AttributeFileProcessorAtomix` keeps a reference to an Atomix client, which will connect to the cluster to interact with the distributed primitives.

![Missing graph][102]

The synchronous implementation is straightforward, therefore it will not be further discussed. The distributed implementation, on the other hand, is the project's core contribution.

Before the `SliqImpl` instance can make use of the `AttributeFileProcessor`s, it needs to initialize them. The distributed implementation creates an Atomix Client in this step:

```java
atomix = Atomix.builder()
	.withMemberId("client1")
	.withAddress("localhost:" + RandomUtils.nextInt(6000, 7000))
	.withMembershipProvider(BootstrapDiscoveryProvider.builder()
		.withNodes(
			Node.builder()
				.withId("member-1")
				.withAddress("localhost:5000")
				.build(),
			Node.builder()
				.withId("member-2")
				.withAddress("localhost:5001")
				.build())
		.build())
	.withPartitionGroups(RaftPartitionGroup.builder("data")
		.withMembers("member-1", "member-2")
		.build())
	.build();
atomix.start().join();
```

Atomix allows to build clients programatically through a fluent API, such as in the previous code extract. It is important to note that:
- Each client specifies a port that will be attached to the client process. It assigns a random port, since the design considers one client per attribute.
- It specifies the nodes of the cluster to which the client will connect. After testing, the minimum number of nodes to declare is two. A single node is unable to carry out the election phase and never completes its start.
- The client builder requires a partition group. There can be multiple partition groups with different implementations. At the moment of writing the possible implementations are `RaftPartitionGroup` and `PrimaryBackupPartitionGroup`. The parameter passed to the builder determines which partition group will be used.
- Finally, the client is started. This occurs asynchronously, and the `join()` method returns after the client is ready to interact with the cluster.

The following log extract shows the election phase of the Raft implementation:

```
[raft-server-data-partition-6] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-6} - Transitioning to FOLLOWER
[raft-client-system-partition-1-3] INFO io.atomix.protocols.raft.partition.impl.RaftPartitionServer - Starting server for partition PartitionId{id=1, group=data}
[raft-server-data-partition-1] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-1} - Transitioning to FOLLOWER
[raft-client-system-partition-1-3] INFO io.atomix.protocols.raft.partition.impl.RaftPartitionServer - Starting server for partition PartitionId{id=2, group=data}
[raft-server-data-partition-5] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-5} - Transitioning to CANDIDATE
[raft-server-data-partition-5] INFO io.atomix.protocols.raft.roles.CandidateRole - RaftServer{data-partition-5}{role=CANDIDATE} - Starting election
[raft-server-data-partition-1] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-1} - Transitioning to CANDIDATE
[raft-server-data-partition-1] INFO io.atomix.protocols.raft.roles.CandidateRole - RaftServer{data-partition-1}{role=CANDIDATE} - Starting election
[raft-server-data-partition-5] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-5} - Transitioning to LEADER
[raft-server-data-partition-5] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-5} - Found leader member-2
[raft-server-data-partition-2] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-2} - Transitioning to FOLLOWER
[raft-client-system-partition-1-3] INFO io.atomix.protocols.raft.partition.impl.RaftPartitionServer - Starting server for partition PartitionId{id=7, group=data}
[raft-server-data-partition-3] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-3} - Found leader member-1
[raft-server-data-partition-4] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-4} - Found leader member-1
[raft-server-data-partition-6] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-6} - Found leader member-1
[raft-server-data-partition-1] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-1} - Transitioning to LEADER
[raft-server-data-partition-1] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-1} - Found leader member-2
[raft-server-data-partition-2] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-2} - Transitioning to CANDIDATE
[raft-server-data-partition-2] INFO io.atomix.protocols.raft.roles.CandidateRole - RaftServer{data-partition-2}{role=CANDIDATE} - Starting election
[raft-server-data-partition-7] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-7} - Transitioning to FOLLOWER
[raft-server-data-partition-2] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-2} - Transitioning to LEADER
[raft-server-data-partition-2] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-2} - Found leader member-2
[raft-server-data-partition-7] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-7} - Transitioning to CANDIDATE
[raft-server-data-partition-7] INFO io.atomix.protocols.raft.roles.CandidateRole - RaftServer{data-partition-7}{role=CANDIDATE} - Starting election
[raft-server-data-partition-7] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-7} - Transitioning to LEADER
[raft-server-data-partition-7] INFO io.atomix.protocols.raft.impl.RaftContext - RaftServer{data-partition-7} - Found leader member-2
[raft-client-data-partition-7-0] INFO io.atomix.protocols.raft.partition.RaftPartitionGroup - Started
[raft-client-data-partition-7-0] INFO io.atomix.primitive.partition.impl.DefaultPartitionService - Started
...
[atomix-cluster-heartbeat-sender] INFO io.atomix.cluster.impl.DefaultClusterMembershipService - client1 - Member updated: Member{id=client1, address=127.0.0.1:6000, properties={}}
```

Once the client and the clusters finished the synchronization process, the processor clears the set reserved for its attribute:

```java
atomix.getSortedSet(attributeName).clear();
```

This is required because the cluster will preserve the values of the last run. Then, the `SliqImpl` reads the source file and sends each instance attribute to its processor. For the distributed implementation, a distributed sorted set is used to avoid loading all of the values in memory locally at this point. Each time a value is appended to the distributed primitive, the structure maintains a lexicographical ordering. It is important to note that the asynchronous implementation of the set is used:

```java
DistributedSortedSet<String> ss = atomix.getSortedSet(attributeName);
AsyncDistributedSortedSet<String> adss = ss.async();
```

The cluster uses the other configuration method, the configuration file:

```config
cluster {

  # Configure the local node information.
  node {
    id: member-1
    address: "localhost:5000"
  }

  # Configure the node discovery protocol.
  discovery {
    type: bootstrap
    nodes.1 {
      id: member-1
      address: "localhost:5000"
    }
    nodes.2 {
      id: member-2
      address: "localhost:5001"
    }
  }
}

# Configure the system management group.
managementGroup {
  type: raft
  partitions: 1
  members: [member-1, member-2]
}
```

Each node must have its ID inside the discovery list for it to be part of the cluster. For this project, the two instances are started in the same machine. Therefore, the program configures the data directory -used to persist the state and be able to recover from a shutdown- explicitly to avoid conflicts:

```java
System.setProperty("atomix.data", "member-2-data");
```

This property can be configured as a parameter to the program in order to avoid the hard-coded line.

Finally, `CompleteProcessor` is a component that abstracts the calculations for each attribute. There are implementations for both `sync-impl` and `distributed-impl`, and the difference is that `distributed-impl` uses a list of `CompletableFuture`s to execute each `AttributeFileProcessor` asynchronously:

```java
public void process(HashMap<Integer, HashMap<String, Integer>> classCounters,
                    HashMap<String, List<String>> processedValues) {
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    for (AttributeFileProcessor processor : attributeFileProcessors) {
        if (processor.isClassAttribute()) continue;

        futures.add(CompletableFuture.supplyAsync(() -> {
            processor.process(classCounters, processedValues);
            return null;
        }));
    }

    for (CompletableFuture<Void> future : futures) {
        future.join();
    }
}
```

### Evaluation
#### Performance
The original file used in the evaluation is from the Machine Learning Repository [[9]]. It is a donation by B. Kaluza, V. Mirchevska, E. Dovgan, M. Lustrek and M. Gams [10] and contains 164.860 instances. The file contains the recordings of different sensors for different people performing a limited set of activities, such as walking, sitting and falling down.

The performance evaluation was conducted with the following criteria:

1. Processing time vs number of nodes. The tests `SliqImplTest` in `sync-impl` and `distributed-impl` parameterize the number of nodes of the tree to obtain. The test method records the results in a CSV file. The number of nodes initially considered for testing were 50, 100, 500 and 1.000 with the 164.860 instances file. However, the final test was on 1, 2, 5, 7 and 10 nodes with the 1.000 instances file.

2. Processing time vs number of instances. In this case, the test ran in multiple files and a fixed number of nodes. Each file contains an increasing number of instances and records the time needed for each implementation to obtain 5 nodes. The number of instances tested are 10, 100, 500 750 and 1.000. The distributed implementation did not perform well even with this small number of instances, and the clients and members started disconnecting when tested with the original file and could not process even one node:

```
[raft-client-data-partition-7-0] INFO io.atomix.protocols.raft.partition.RaftPartitionGroup - Started
[raft-client-data-partition-7-0] INFO io.atomix.primitive.partition.impl.DefaultPartitionService - Started
[raft-client-system-partition-1-1] INFO io.atomix.core.impl.CoreTransactionService - Started
[raft-client-system-partition-1-1] INFO io.atomix.core.impl.CorePrimitivesService - Started
[atomix-cluster-heartbeat-sender] INFO io.atomix.cluster.impl.DefaultClusterMembershipService - client1 - Member unreachable: Member{id=member-2, address=127.0.0.1:5001, properties={}}
[atomix-cluster-heartbeat-sender] INFO io.atomix.cluster.impl.DefaultClusterMembershipService - client1 - Member unreachable: Member{id=member-1, address=127.0.0.1:5000, properties={}}
[atomix-cluster-heartbeat-sender] INFO io.atomix.cluster.impl.DefaultClusterMembershipService - member-2 - Member reachable: Member{id=member-2, address=127.0.0.1:5001, properties={}}
[atomix-cluster-heartbeat-sender] INFO io.atomix.cluster.impl.DefaultClusterMembershipService - member-1 - Member reachable: Member{id=member-1, address=127.0.0.1:5000, properties={}}
[atomix-cluster-heartbeat-sender] INFO io.atomix.cluster.impl.DefaultClusterMembershipService - client1 - Member unreachable: Member{id=member-2, address=127.0.0.1:5001, properties={}}
[atomix-cluster-heartbeat-sender] INFO io.atomix.cluster.impl.DefaultClusterMembershipService - client1 - Member unreachable: Member{id=member-1, address=127.0.0.1:5000, properties={}}
[atomix-cluster-heartbeat-sender] INFO io.atomix.cluster.impl.DefaultClusterMembershipService - member-1 - Member reachable: Member{id=member-1, address=127.0.0.1:5000, properties={}}
[atomix-cluster-heartbeat-sender] INFO io.atomix.cluster.impl.DefaultClusterMembershipService - member-2 - Member reachable: Member{id=member-2, address=127.0.0.1:5001, properties={}}
[atomix-cluster-heartbeat-sender] INFO io.atomix.cluster.impl.DefaultClusterMembershipService - client1 - Member unreachable: Member{id=member-2, address=127.0.0.1:5001, properties={}}
[atomix-cluster-heartbeat-sender] INFO io.atomix.cluster.impl.DefaultClusterMembershipService - client1 - Member unreachable: Member{id=member-1, address=127.0.0.1:5000, properties={}}
...
```

And eventually started to return timeouts:

```
io.atomix.primitive.PrimitiveException$Timeout
	at io.atomix.core.map.impl.BlockingDistributedMap.complete(BlockingDistributedMap.java:195)
	at io.atomix.core.map.impl.BlockingDistributedMap.clear(BlockingDistributedMap.java:130)
	at ca.uvic.concurrency.gmmurguia.project.distributedsliq.impl.ClassListDistributed.fill(ClassListDistributed.java:60)
	at ca.uvic.concurrency.gmmurguia.project.sliqimpl.SliqImpl.start(SliqImpl.java:46)
	at ca.uvic.concurrency.gmmurguia.project.distributedsliq.impl.SliqImplTest.testIt(SliqImplTest.java:69)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
	at org.junit.runners.Suite.runChild(Suite.java:128)
	at org.junit.runners.Suite.runChild(Suite.java:27)
	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
	at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)
	at com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:47)
	at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:242)
	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)
```

The program consumed almost all of the available resources:

![Missing graph][103]

The cause of the problem was the asynchronous calls to add elements to the distributed attribute lists. The cluster was not able to process all the incoming requests and eventually stopped accepting requests. Then, the program was modified to maintain a list of 1.000 requests and then wait until they completed. The program execution was again too slow and it did not finish for one node after several minutes.

Finally, the execution times for the `sync-impl` module were almost constant, while they increased exponentially for the `distributed-impl` module:

![Missing graph][104]
![Missing graph][105]

#### Comprehensibility
Atomix offers a very comprehensible API. Initially, this project constructed a proof-of-concept with version 1.0.8 of the code. It offered just a few distributed primitives which were not compatible with the standard Collections API and were completely asynchronous. The final implementation runs on the 3.0.1 version which offers 26 primitives and several configuration options. Aditionally, the API gives the developer the freedom to choose a synchronous or asynchronous API. For example:

```java
atomix.getSortedSet(attributeName);
```

Returns a collection whose methods are synchronous. Therefore it is possible to use it as any other collection:

```java
classList.clear();
```

The asynchronous collection is obtained from the synchronous instance:

```java
DistributedSortedSet<String> ss = atomix.getSortedSet(attributeName);
AsyncDistributedSortedSet<String> adss = ss.async();
```

And every method call will return a `CompletableFuture` instance:

```java
adss.add(element);
```

Note that the previous call will not execute synchronously. If the developer wanted to wait until the action is actually executed in the cluster, he or she can call the `join` method, which will block until the task is completed:

```java
adss.add(element).join();
```

The API is very intuitive. Despite the lack of proper documentation, the implementation uses some of the primitives and adjusting the configuration does not affect the business part of the code. Setting up a simple cluster is relatively trivial, as was discussed in the implementation section.

Aditionally, the asynchronous code to process the entropies per attribute failed as well. This is most likely due to an implementation error, as a subsecuent analysis showed that the component shared some data structures and when different threads tried to access them concurrently, errors appeared randomly.

### **Conclusion**
The project explored the implementation of SLIQ -a data mining algorithm for classification- in a local and distributed environment. In order to do that, the modules use a common base implementation where it abstracted only the parts related to the class list and the attributes' lists. In the distributed implementation, those data structures were implemented as distributed primitives using the Raft protocol.

The analysis concluded that the operations on the distributed primitives are much slower than expected in the Atomix framework. This prevented the evaluation from testing with a big data set, which was the original objective. Another problem of the implementation was the asynchronous processing of entropies, most likely due to a wrong design of the internal data structures.

In conclusion, the distributed implementation using Atomix did not outperform the local, synchronous implementation mostly because the distributed primitives used to test introduced an excessive overhead. Therefore, the implementation is not suitable for large datasets as the computation time increases exponentially as the data set grows bigger.

### **Future work**
There are several parts and new approaches that could be tested:

1. The interface-driven approach of the implementation allows to test with other frameworks, not only Atomix. Redis would be an interesting option, as it works entirely in memory and should be faster.
2. The implementation tries to find the lowest entropy in all attributes. This is most likely the reason of the errors obtained in the asynchronous processing. An improvement would be to obtain the best entropy per attribute and then conclude which is the lowest.
3. The implementation stops only after a fixed number of nodes have been added to the tree. A better implementation would compare the resulting entropy and if it changed only slightly, then it should prune that sub tree.
4. All the cluster nodes started in a single 8-core CPU, with 16 GB of RAM. It would be interesting to see how that would change for two independent cluster nodes and a client.

### **References**
[[1]] Sutter, Herb. "The Concurrency Revolution." [Online]. Available: http://www.drdobbs.com/the-concurrency-revolution/184401916

[2] M. Mehta, R. Agrawal & J. Rissanen. "SLIQ: A Fast Scalable Classifier for Data Mining." *In Proc. of the Fifth International Conference on Extending Database Technology (EDBT)*, 1996.

[3] B. Hssina et al. "A comparative study of decision tree ID3 and C4.5." *International Journal of Advanced Computer Science and Applications*. pp. 13-19

[4] J. Ananthanarayanan, A. Ghodsi, S. Shenker & I. Stoica. "Disk-Locality in Datacenter Computing Considered Irrelevant." University of California.

[[5]] A. Sergeev & M. Del Balso. "Horovod: fast and easy distributed deep learning in TensorFlow." [Online]. Available: https://github.com/uber/horovod

[6] S. G. Totad et al. "Scaling Data Mining Algorithms to Large and Distributed Datasets". *International Journal of Database Management Systems (IJDMS)*, Vol.2, No.4.

[7] M. J. Zaki. "Parallel and Distributed Data Mining: an introduction".

[[8]] "Atomix - A reactive Java framework for building fault-tolerant distributed systems." [Online]. Available: https://atomix.io

[[9]] "UCI Machine Learning Repository: Localization Data for Person Activity Data Set." [Online]. Available: https://archive.ics.uci.edu/ml/datasets/Localization+Data+for+Person+Activity

[10] B. Kaluza, V. Mirchevska, E. Dovgan, M. Lustrek & M. Gams. "An Agent-based Approach to Care in Independent Living." *International Joint Conference on Ambient Intelligence (AmI-10)*.


[1]: http://www.drdobbs.com/the-concurrency-revolution/184401916
[5]: https://github.com/uber/horovod
[8]: https://atomix.io
[9]: https://archive.ics.uci.edu/ml/datasets/Localization+Data+for+Person+Activity

[101]: https://github.com/sephiroth2029/concurrency-project/blob/master/diagrams/Modules.png?raw=true
[102]: https://github.com/sephiroth2029/concurrency-project/blob/master/diagrams/Components.png?raw=true
[103]: https://github.com/sephiroth2029/concurrency-project/blob/master/diagrams/CpuAndMemoryUsage.PNG?raw=true
[104]: https://github.com/sephiroth2029/concurrency-project/blob/master/diagrams/nodesVStime.png?raw=true
[105]: https://github.com/sephiroth2029/concurrency-project/blob/master/diagrams/instancesVStime.png?raw=true
