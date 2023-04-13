package dev.rbruno.kafka.producer.basic;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducerDemo {

    public static void main(String[] args) {
        // create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());


        // create the producer

        ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic", "hello world !");KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
        //ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic", "key1","hello world !");
        //ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic","key1" ,"hello world");
        //ProducerRecord<String, String> record = new ProducerRecord<String, String>("test1",1,"key1" ,"hello world modified");

        // send data (async)
        producer.send(record);

        producer.flush();
        producer.close();
        //default Duration to Long.MAX_VALUE ms, in any case when you invoke close and the program finish, it forces data flushing
        //producer.close(Duration.ofSeconds(15));
    }
}
