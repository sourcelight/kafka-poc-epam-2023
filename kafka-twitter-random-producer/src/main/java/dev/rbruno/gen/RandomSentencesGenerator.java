package dev.rbruno.gen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class RandomSentencesGenerator implements Runnable{
    private final AtomicBoolean ab = new AtomicBoolean(false);
    private final BlockingQueue<String> blockingQueue;

    private final long ms;

    @Override
    public void run() {
        ab.set(true);
        try {
            generateRandomString(ab, blockingQueue, ms);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void generateRandomString(AtomicBoolean ab,BlockingQueue<String> blockingQueue, long ms) throws InterruptedException {
        //System.out.println("Hello, this is your first generated random sentence: ");

        String[] names = { "Peter", "Michell", "Jane", "Steve","Matthew" };
        String[] places = { "Sofia", "Plovdiv", "Varna", "Burgas","Richard" };
        String[] verbs = { "eats", "holds", "sees", "plays with", "brings","drives" };
        String[] nouns = { "stones", "cake", "apple", "laptop", "bikes","cars" };
        String[] adverbs = { "slowly", "diligently", "warmly", "sadly", "rapidly","very quick" };
        String[] details = { "near the river", "at home", "in the park","on the mountain" };

        String composition = "%s from %s %s %s %s %s%n";

        ab.set(true);
        while(ab.get()){
            String randomName = getRandomWord(names);
            String randomPlace = getRandomWord(places);
            String randomVerb = getRandomWord(verbs);
            String randomNoun = getRandomWord(nouns);
            String randomAdverb = getRandomWord(adverbs);
            String randomDetail = getRandomWord(details);

            String sentence = String.format(composition, randomName, randomPlace, randomAdverb, randomVerb, randomNoun, randomDetail);

            JSONObject json = new JSONObject();
            json.put("sentence", sentence);
            json.put("id", randomUUID());
            String result = json.toString();
            System.out.println(result);
            blockingQueue.put(result);
            Thread.sleep(ms);
        }
    }
    public static String getRandomWord(String[] words){
        Random random = new Random();
        int randomIndx = random.nextInt(words.length);
        String word = words[randomIndx];
        return word;
    }


    /**
     * Utility method used for the shutdown hook to stop the thread
     */
    public void stop() {
        ab.set(false);
        log.info("putting to false the atomic variable");
    }


    static String randomUUID() {

        UUID randomUUID = UUID.randomUUID();

        return randomUUID.toString().replaceAll("_", "");

    }

}
