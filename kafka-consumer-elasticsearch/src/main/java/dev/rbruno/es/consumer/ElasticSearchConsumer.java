package dev.rbruno.es.consumer;

import com.google.gson.JsonParser;
import dev.rbruno.es.consumer.config.ConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Component
@Slf4j
@EnableConfigurationProperties(ConfigProperties.class)
@RequiredArgsConstructor
public class ElasticSearchConsumer {
    private final ConfigProperties  configProperties ;
    @Value("${credentials.username}")
    private String username;
    @Value("${credentials.password}")
    private String password;
    @Value("${credentials.hostname}")
    private String hostname;

    @PostConstruct
    public void run() throws IOException {
        consumer();
    }

    public void consumer() throws IOException {
        RestHighLevelClient client = createClient();
        KafkaConsumer<String, String> consumer = createConsumer(configProperties.topic());
        //configProperties.hostname().toString()
        // poll for new data
        while(true) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100)); // new in Kafka 2.0.0+
                Integer recordCount = records.count();
                log.info("Received " + recordCount + " records.");
                BulkRequest bulkRequest = new BulkRequest();

                for (ConsumerRecord<String, String> record : records) {
                    // 2 Strategies for generating a unique id for each record sent to Kafka to make the consumer idempotent
                    // 1. kafka generic id using topic, partition, and offset
                    //String id = record.topic() + "_" + record.partition() + "_" + record.offset();
                    // 2. twitter feed specific id
                    try {
                        String id = extractIdFromTweet(record.value());

                        // insert data into elasticsearch
                        IndexRequest indexRequest = new IndexRequest(
                                "twitter",
                                "tweets",
                                id // used to make our consumer idempotent
                        ).source(record.value(), XContentType.JSON);

                        bulkRequest.add(indexRequest);
                    } catch (NullPointerException e) {
                        log.warn("skipping bad record: " + record.value());
                    }

                }
                if (recordCount > 0) {
                    BulkResponse bulkItemResponses = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                    log.info("Committing offsets...");
                    consumer.commitSync();
                    log.info("Offsets committed.");
                } else {
                    log.info("No records to commit.");
                }
            }catch (Exception e){
                log.error("Error in the loop: "+e.getMessage() +" cause: "+e.getCause(),e);
            }

        }
    }

    private static JsonParser jsonParser = new JsonParser();
    private static String extractIdFromTweet(String tweetJson){
        //return jsonParser.parse(tweetJson).getAsJsonObject().get("id_str").getAsString();
        //It was necessary for twitter messages in JSON
        //return jsonParser.parse(tweetJson).getAsJsonObject().get("data").getAsJsonObject().get("id").getAsString();//adapted for a specific tweet message
        //return tweetJson;
        return jsonParser.parse(tweetJson).getAsJsonObject().get("id").getAsString();
    }

    public  KafkaConsumer<String, String> createConsumer(String topic){
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, configProperties.groupId());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // latest: read since last offset
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // disable auto commit of offsets
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10"); // limit records retrieved
        properties.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000");

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

        // subscribe consumer to our topic(s)
        consumer.subscribe(Collections.singleton(topic));

        return consumer;
    }

    public  RestHighLevelClient createClient() {

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(hostname, 443, "https"))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                        return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });

        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }
}
