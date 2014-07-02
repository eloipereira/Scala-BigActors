# The Scala BigActor Programming Language #

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