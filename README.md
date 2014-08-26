     ____  _          _        _                 
    | __ )(_) __ _   / \   ___| |_ ___  _ __ ___ 
    |  _ \| |/ _` | / _ \ / __| __/ _ \| '__/ __|
    | |_) | | (_| |/ ___ \ (__| || (_) | |  \__ \
    |____/|_|\__, /_/   \_\___|\__\___/|_|  |___/
             |___/                               

# The Scala BigActor Programming Language #

The BigActor Programming Language is an implementation of the BigActor model [1] which brings together Hewitt and Agha's Actor model of concurrency and with Robin Milner's Bigraphs [3]. 
The BigActor model embeds Actors into a bigraphical model of the world that models the location and connectivity of the computing machines that are executing them.
BigActors are able to perform regular actor operations such as spawning and asynchronous message-passing.
BigActors are also able to observe the structure of the world, compute upon that information, and manipulate the structure by requesting control actions to change it, such as requesting their hosts to move to another location or connect to a network.   
With BigActors and the BigActor Programming Language we aim at providing a framework for modelling and program mobile computing devices that live in worlds with dynamic structure. 

## Install ##

The current implementation has been tested in Gnu/Linux distributions and Mac OSX 

1. We use a build tool named SBT [http://www.scala-sbt.org](http://www.scala-sbt.org)
    * [Follow the instructions here](http://www.scala-sbt.org/0.13/tutorial/Manual-Installation.html)

2. Check out the bitbucket bigactors repository
3. We use the Bigraph model checker BigMC as a bigraph. We provide a bash script to make BigMC installation easier. To build BigMC you need autoconf
    * Install autoconf (on Gnu/Linux): `sudo apt-get install autoconf`
  
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

[1] - Pereira, E., Kirsch, C. M., Sengupta, R., de Sousa, J. B. (2013). Bigactors - A Model for Structure-aware Computation. In ACM/IEEE 4th International Conference on Cyber-Physical Systems (pp. 199–208). Philadelphia, PA, USA: ACM/IEEE.

[2] - Agha, G. (1986). Actors: a model of concurrent computation in distributed systems. Cambridge, MA, USA: MIT Press.

[3] - Milner, R. (2009). The Space and Motion of Communicating Agents (pp. I–XXI, 1–191). Cambridge University Press.