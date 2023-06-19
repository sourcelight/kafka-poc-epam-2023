package dev.rbruno.kafka.tp;

import dev.rbruno.gen.RandomSentencesGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONException;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TwitterProducer {

    private String bearerToken = "YOUR_BEARER_TOKEN";
    //private String topic = "twitter_tweets";
    private String topic = "random_sentences1";

    /**
     * This class is the orchestrator: it calls the TwitterProducerThread class as thread and starts it.
     * The TwitterProducerThread takes as arguments the bearer token(necessary for accessing the twitter API), the rules to filter twitters and the queue
     */
    public TwitterProducer(){}

    public static void main(String[] args) {
        new TwitterProducer().run();
    }

    public void run(){
        /** Set up your blocking queues: Be sure to size these properties based on expected TPS of your stream */
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(10);//Producer and consumer are synchronized with put and take so it's not necessary a big capacity

        //previously used for the twitter API
        /*Map<String, String> rules = new HashMap<>();
        rules.put("cats has:images", "cat images");
        rules.put("dogs has:images", "dog images");
        rules.put("love", "");
        //I start to extract the tweets from Twitter according specific rules and insert in the blocking queue
        //TwitterProducerThread tp = new TwitterProducerThread(msgQueue,bearerToken,rules,500);*/

        RandomSentencesGenerator rsg = new RandomSentencesGenerator(msgQueue,500);
        Thread threadTwitter = new Thread(rsg);
        threadTwitter.start();

        // create kafka producer
        KafkaProducer<String, String> producer = createKafkaProducer();

        // create a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("stopping application...");
            rsg.stop();//I stop the thread putting the atomic variable
            //client.stop(); // disconnect from twitter
            producer.close(); // close producerG
        }));

        // send tweets to kafka
        String msgTxt = null;
        while (threadTwitter.isAlive()) {
            String msg = null;
            try {
                msg = msgQueue.poll(2, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
                //client.stop();
            }

            if (msg != null) {
                try{
                    //msgTxt = new JSONObject(msg).getJSONObject("data").get("text").toString(); that was for messages coming from Twitter
                    //msgTxt = new JSONObject(msg).toString();
                    msgTxt=msg;
                    log.info("Message: " + msgTxt);
                }catch (JSONException e){
                    log.error("error: "+e.getMessage() + "msgError:"+ msg);
                }
                producer.send(new ProducerRecord<>(topic, null, msgTxt), new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if (e == null) {
                            log.info("record inserted at: partition: {}, offset: {}",recordMetadata.partition(),recordMetadata.offset());
                        }
                        if (e != null) {
                            log.error("Error: ", e);
                        }
                    }
                });
            }
        }
        log.info("end of application");
    }


    public KafkaProducer<String, String> createKafkaProducer() {

        // create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // create safe Producer
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true" );
        //The properties below are set by default if I set as above an idempotent producer

        //properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        //properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        //properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

        // create high throughput Producer (at the expense of a bit of latency and CPU usage)
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024)); // 32KB batch size

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
        return producer;
    }
}
