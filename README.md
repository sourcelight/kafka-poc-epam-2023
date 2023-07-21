# Kafka-basics-poc  
A bit of History  
**Kafka**  
Kafka was originally developed at LinkedIn, and was subsequently open sourced in early 2011.  
Graduation from the Apache Incubator occurred on 23 October 2012.  
Jay Kreps(CEO of Confluent) chose to name the software after the author Franz Kafka  
because it is "a system optimized for writing", and he liked Kafka's work.

**Confluent**
Confluent is an American big data company found by three LinkedIn Engineers led by Jay Kreps.  
It is focused on the open source Apache Kafka real-time messaging technology 
that Kreps, Neha, and Jun created and developed.

simple Kafka producer and consumer, topic creation on CP control-center
Steps:
#clean everything dirty
docker system prune 
#run docker composer(with all services included control center from confluent platform)
docker-compose up -d
#is everything ok ? or check docker desktop
docker-compose ps
#run the  producer and make a test with "get"(synchronous) and remove flush(without get, the message is not saved in the topic) because of asynchronous call of send
#observe the topic created and the message in offset explorer and control center
#note that the topic is really canceled if you cancel the associated consumer

#close the trial with, to start a new test
docker-compose down --remove-orphans













### References
[Githu Actions](https://docs.gradle.org/current/userguide/github-actions.html#view_the_details_for_jobs_and_steps_in_the_workflow)

<a href="http://example.com/" target="_blank">example</a>

set a variable in powershell(for local tests about GitHub Actions)
- $env:CI = 'true
- echo $env:CI