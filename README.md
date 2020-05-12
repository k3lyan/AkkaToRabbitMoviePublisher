# AkkaToRabbitMoviePublisher
Retrieve online IMDb datasets, filter and push selected items to a RabbitMQ queue using Akka.
## Short Description    
This modules aims at retrieving [IMDb datasets](https://datasets.imdbws.com) in order to filter the data requested by the product requirements using Akka Streams and to publish these selected data to a RabbitMQ queue.
## Process description  
1. Load the targeted dataset from ImDb dataset website  
2. Stream these data to a Akka Streams Source
3. Parse tsv data from the source, map it to Map[String, String] and filter the data according to the product requirements
4. Publish the filtered data to RabbitMQ using an Akka Stream Sink
## Prerequesites  
You need to have sbt>=1.x and docker-compose>=1.xx.x installed. 
## How to use (master branch)
1. Git clone the project
2. At the root repository, make executable the file get_data.sh: ```chmod u+x get_data.sh```
3. This script is currently configured to download the title.basics.tsv dataset, you can modify it by changing the url in the bash script (make sure your product requirements will match with the tsv columns). To download it: ```./get_data.sh```
4. Once downloaded, launch locally your RabbitMQ instance: ```docker-compose up```
5. In another terminal window, run your scala App using sbt (the default log level remains at DEBUG but you canmodify it in src/main/resources/application.conf): ```sbt run```

You can check that your targeted data has been published to your local RabbitMQ instance with the [UI manager](http://localhost:15672/). If you don't need your local RabbitMQ instance anymore, don't forget to shut it down: ```docker-compose down```.
