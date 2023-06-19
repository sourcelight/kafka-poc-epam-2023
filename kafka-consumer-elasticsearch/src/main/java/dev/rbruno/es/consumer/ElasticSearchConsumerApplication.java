package dev.rbruno.es.consumer;

import dev.rbruno.es.consumer.config.YamlPropertySourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Slf4j
@SpringBootApplication
@PropertySources({
        @PropertySource(value= "file:${external_path}", factory = YamlPropertySourceFactory.class)
})
public class ElasticSearchConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElasticSearchConsumerApplication.class, args);

    }


}
