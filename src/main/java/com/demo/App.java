package com.demo;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Hello world!
 *
 */
@Slf4j
public class App 
{
    public static void main( String[] args )
    {
        log.info("starting kafka client.....");
        log.info("getting env info:");
        
        String topic = System.getenv("TOPIC_NAME");
        //localhost:9092
        String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
        String mode = System.getenv("MODE");
        log.info("{} {} {}", topic, bootstrapServers, mode);

        if (mode.equals("PRODUCE")) {
            int numMessages = Integer.parseInt(System.getenv("PRODUCER_NUM_MSG"));
            produce(topic, bootstrapServers, numMessages);
        } else if (mode.equals("CONSUME")) {
                String consumerGroupId = System.getenv("CONSUMER_GROUP_ID");
                consume(topic, bootstrapServers,consumerGroupId);
        }

    }

    private static void consume(String topic, String bootstrapServers, String consumerGroupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,          
           "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, 
           "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList(topic));
        //int total_count = 0;

        try {
            while (true) {
              ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
              for (ConsumerRecord<String, String> record : records) {
                String key = record.key();
                String value = record.value();
                //total_count += value.getCount();
                log.info("Consumed record with key {} and value {}", key, value);
              }
            }
          } finally {
            consumer.close();
          }


    }
    /*
     * producer method that sends x number of sample messages
     */
    private static void produce(String topic, String bootstrapServers, int numMessages) {
        log.info("Calling producer....");
        //Producer config
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG,"all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put( ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
        "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<String, String>(props);
        for (int i =0; i < numMessages; i++) {
            String cnt = String.valueOf(i);
            producer.send(new ProducerRecord<String,String>(topic, cnt, "Message "+cnt), new Callback(){
                @Override
                public void onCompletion(RecordMetadata metadata, Exception e) {
                    if (e != null) {
                        e.printStackTrace();
                      } else {
                        log.info("Produced record to topic {} partition {} @offset {}",metadata.topic(),metadata.partition(),metadata.offset());
                      }
                }
            });
        }
        producer.flush();
        log.info("{} messages were produced to topic {}", numMessages,topic);
        producer.close();        
    }
}
