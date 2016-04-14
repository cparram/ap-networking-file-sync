# ap-networking01
Synchronization files using custom protocol over client-server structure

## Usage
### Build
* `gradle rundDefaultServer`: To compile and run server with default options
* `gradle rundDefaultClient`: Same as above but for client
* `gradle serverJar`: Creates a jar for server source set
* `gradle clientJar`: Same as above but for client

### Run Server
* `java -jar build/libs/file-sync-server-0.1.0.jar --filename serverfile.txt --port 4000`


### Run Client
* `java -jar build/libs/file-sync-client.jar --filename clientfile.txt --port 4000 --block-size 2024 --hostname localhost`


**Note** that filename must exist


