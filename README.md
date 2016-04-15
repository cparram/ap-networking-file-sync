[![Build Status](https://travis-ci.org/cparram/ap-networking01.svg?branch=staging)](https://travis-ci.org/cparram/ap-networking01)
# ap-networking01
Synchronization files using custom protocol over client-server structure. Project to academic purpose

## Usage
### Build
* `$ /.gradlew runDefaultServer`: To compile and run server with default options
* `$ /.gradlew runDefaultClient`: Same as above but for client
* `$ /.gradlew serverJar`: Creates a jar for server source set
* `$ /.gradlew clientJar`: Same as above but for client

### Run Server
* `java -jar build/libs/file-sync-server<version>.jar [options]`


### Run Client
* `java -jar build/libs/file-sync-client<version>.jar [options]`

### Options
* Server: `--filename`, `--port`
* Client: `--filename`, `--port`, `--block-size`, `--hostname`

**Note** that filename must exist


