package dev.rbruno.kafka.consumer.basic;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerDemo {
    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(ConsumerDemo.class.getName());
        final String topic= "first_topic";
        boolean loop= true;
        boolean stopLoop= false;
        final int partitionNumber= 0;
        // create consumer config
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "group-consumer-2");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        //properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        //properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // latest: read since last offset
        //properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,"1");

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

        // subscribe consumer to our topic(s)
        consumer.subscribe(Collections.singleton(topic));

        // poll for new data
        while(loop) {
            if(stopLoop){
                loop = false;
            }
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100)); // new in Kafka 2.0.0+

            for (ConsumerRecord<String, String> record : records) {
                //if(record.partition()==partitionNumber){
                    logger.info("Key: " + record.key() + "; Value: " + record.value());
                    logger.info("Partition: " + record.partition() + "; Offset: " + record.offset());
                    logger.info("Partition: " + record.partition() + "; Timestamp: " + record.timestamp());
                //}
            }
        }
    }
}
