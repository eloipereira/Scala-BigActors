#!/bin/bash
# get current script base dir
PREFIX_PATH="`dirname \"$0\"`"
 
# force absolute path 

PREFIX_PATH="`pwd -P`"

# the build dir  
BUILD_DIR=$PREFIX_PATH/bigmc_build
 
# remove build dir for to start a clean build
if [ -d $BUILD_DIR ]; then
   rm -rf $BUILD_DIR
fi
# get bigmc from git repo  
git clone https://github.com/bigmc/bigmc.git $BUILD_DIR
 
 
cd $BUILD_DIR/src
 
#generate bgparser.hpp
yacc --warnings=none --defines=bgparser.hpp bgparser.ypp
 
cd $BUILD_DIR
 
#generate configure
./autogen.sh
 
# generate makefiles to install in the prefix path
 
./configure --prefix=$PREFIX_PATH
 
make -j 4
 
make install
# remove build dir to clean  
rm -rf $BUILD_DIR
 
# create a environment setup file 
echo '## BigMc home environment variable' > $PREFIX_PATH/bigmc.env
echo "export BIGMC_HOME=$PREFIX_PATH" >> $PREFIX_PATH/bigmc.env
echo 'export PATH=$PATH:$BIGMC_HOME/bin' >> $PREFIX_PATH/bigmc.env
 
# warn user that is needed to source the environment to run bigmc
echo -e "\e[31m####### DO NOT FORGET ##########\e[0m"
echo -e "\e[31mFor each execution do a source of the bigmc.env: 'source $PREFIX_PATH/bigmc.env'\e[0m"
 
# source environment to become ready to execute bigmc
source $PREFIX_PATH/bigmc.env
