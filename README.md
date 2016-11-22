
# Scala BigActors #
 
Computers are becoming interactive, ubiquitous, and mobile. Smartphones and autonomous vehicles interact with their environment, by means of sensors and actuators, 
and with other devices using a network infrastructure.
Computation running over these systems often depends upon the location of the physical platform. 
For example, a smartphone provides information about the place where it is located. 
Moreover, computation might even have effects on the mobility of the machine, e.g., a drone searching and tracking a target on the ground needs to dynamically change its location.
While moving, these systems also often change their connectivity.
For example, a smartphone moving from one location to another can detect and connect to different wifi networks.
This mobility and dynamic connectivity exhibited by the computing machinery can make programming such systems a hard task. 
Scala BigActors addresses these issues by making location and connectivity first-class citizens that can be handled by the programmer.

Scala BigActors is an implementation of the BigActor Model as a Scala Domain-Specific Language.
The BigActor model (Pereira et al., 2013) is a model of computation that combines the Actor model of concurrency (Agha, 1986) with Bigraphs (Milner, 2009) for specifying computation 
embedded in mobile, networked computing systems. 
The location and connectivity of machines are abstracted as Bigraphs, which entails two graphical structures: a forest and a hypergraph.
The forest models nested location, e.g., a smartphone is inside a room, the room is inside a building, and the building is inside a city. 
The hypergraph models connectivity, e.g., some computers inside a room are connected to a given wifi network while others are connected to another. 

A BigActor is an Actor that is hosted by a bigraph node denoting the computing machine where it is executing. 
BigActors can perform the regular actor computation, i.e., compute locally, communicate through asynchronous message passing, and spawn new bigActors.  
Moreover, BigActors are equipped with means for observing and controlling their location and connectivity.
A bigActor can observe the structure of the world, compute upon that information, and manipulate the structure by requesting control actions to change it, 
such as requesting its hosts to move to another location or connect to another network.   

The following figure depicts an example of a bigActor controlling an Unmanned Air Vehicle (UAV) searching and tracking for an oil-spill in the ocean.
![alt text](https://github.com/eloipereira/bigactors/raw/master/img/oilSpillWebSite.png "Oil-spill search and track")
First the UAV, named `uav0`, connects to a ground station, named `gcs1`, located at a vessel in the vicinities of the search area, named `searchArea0`.
After connecting, `uav0` moves to `searchArea0` and starts observing for the oil-spill, named `oilSpill0`.
After observing `oilSpill0`, `uav0` moves to its location.
Note that there are three bigraphical commands. `CONNECT_HOST_TO gcs` denotes a bigraph reaction rule that connects the host to `gcs` (this action models the hand-over of control authority of the host, i.e., the UAV, to the ground control station denoted by `gcs`).
`MOVE_HOST_TO loc` denotes a bigraph reaction rule that moves the host of the bigActor to `loc`.
`observe CHILDREN(PARENT(HOST))` is a query that retrieves a list of bigraph nodes containing the children of the parent of the host. The list is delivered as an actor message captured by the `react` body (`loop` and `react` are constructs from the Scala Actors library). 

The scenario described in the example was implemented in 2013, at Algarve, Portugal, using vehicles and means from the Portuguese Air Force and the Portuguese Navy. For more information see the video at [http://www.eloipereira.com/videos]([http://www.eloipereira.com/videos) or consult (Pereira et al., 2015), (Pereira et al., 2013), and (Pereira, 2015).

For the formal specification of the BigActor model see (Pereira, 2015) and (Pereira et al., 2013).
For examples of applications in the area of mobile robotics and implementation details of Scala BigActors see (Pereira, 2015), (Pereira et al., 2015), and (Pereira et al., 2013).
For an independent implementation and application of the BigActor Model for programming Crowd Evacuation Systems see (Raj & Kar, 2015).

## Install ##

1. Download and install the SBT build tool [http://www.scala-sbt.org](http://www.scala-sbt.org)
    * [Follow the instructions here](http://www.scala-sbt.org/0.13/tutorial/Manual-Installation.html)

2. Clone the bigactors repository from one of the following locations:
    * Github: [https://github.com/eloipereira/bigactors](https://github.com/eloipereira/bigactors)
    * Bitbucket: [https://bitbucket.org/eloipereira/bigactors](https://bitbucket.org/eloipereira/bigactors)
    
3. Scala BigActors use the Bigraph model checker BigMC as a bigraph execution engine. Follow the instructions below to install BigMC:
    * Install autoconf (on Gnu/Linux): `sudo apt-get install autoconf`
    
    * Install libtool: `sudo apt-get install libtool`
    
    * Install bison: `sudo apt-get install bison`
    
    * Install flex: `sudo apt-get install flex`
    
    * Install yacc: `sudo apt-get install byacc`
  
    * `cd bigactors/bigmc`
  
    * `chmod +x build.sh`
  
    * `./build.sh`
  
    * `export bigmc.env` - In order to avoid exporting `bigmc.env` each time you may want to copy bigmc binary from `bigmc/bin/bigmc` to a standard location such as `/usr/local/bin` or include the location of the binary to your `PATH` variable

    * Test BigMC by typing `bigmc` in your bash terminal

4. Navigate to `cd bigactors`
5. Type `sbt compile`. The tool should download all the dependencies and compile the code.
6. Type `sbt test`. If tests pass run an example.
7. Type `sbt run` and choose one of the rendezvous examples, e.g. `bigactors.ExampleRendezvous4`

## References ##

E. Pereira. Mobile Reactive Systems over Bigraphical Machines - A Programming Model and its Implementation. PhD thesis, University of California at Berkeley, 2015.

E. Pereira, C. Krainer, P. Marques Da Silva, C.M. Kirsch, and R. Sengupta. A runtime system for logical-space programming. In Proc. Workshop on the Swarm at the Edge of the Cloud (SWEC), April 2015.

E. Pereira, P. Marques, C. Krainer, C. M. Kirsch, J. Morgado, and R. Sengupta. A Networked Robotic System and its Use in an Oil Spill Monitoring Exercise. In Swarm at the Edge of the Cloud Workshop (ESWeek'13), volume 2, pages 1-2, Montreal, QC, Canada, 2013.

E. Pereira, C. M. Kirsch, R. Sengupta, and J. B. de Sousa. Bigactors - A Model for Structure-aware Computation. In ACM/IEEE 4th International Conference on Cyber-Physical Systems, pages 199-208, Philadelphia, PA, USA, 2013. ACM/IEEE.

P. G. Raj and S. Kar, "Design and Development of a Distributed Mobile Sensing Based Crowd Evacuation System: A Big Actor Approach," Computer Software and Applications Conference (COMPSAC), 2015 IEEE 39th Annual, Taichung, 2015, pp. 355-360.

G. Agha, Actors: a model of concurrent computation in distributed systems. Cambridge, MA, USA: MIT Press, 1986.

R. Milner, The Space and Motion of Communicating Agents. Cambridge University Press, 2009.
