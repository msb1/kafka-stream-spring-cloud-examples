package com.barnwaldo.anomalydetection;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

/**
 *
 * @author barnwaldo
 * @version 1.0
 * @since Jan 11, 2019
 */
@Component
public class DataStreamListener {

    @EnableBinding(KafkaStreamsProcessor.class)
    public class DataProcessorApplication {

        /**
         * DevNotes: compilation fails unless method returns a KStream
         *
         * @param views
         * @return 
         */
        @StreamListener("input")
        @SendTo("output")
        public KStream<?, ?> process(KStream<String, String> views) {
            
//            final Serde<String> stringSerde = Serdes.String();
//            final Serde<Long> longSerde = Serdes.Long();
 
            // Read the source stream.  In this example, we ignore whatever is stored in the record key and
            // assume the record value contains the username (and each record would represent a single
            // click by the corresponding user).

            KTable<Windowed<String>, Long> anomalousUsers = views
                    // map the user name as key, because the subsequent counting is performed based on the key
                    .map((ignoredKey, username) -> new KeyValue<>(username, username))
                    // count users, using one-minute tumbling windows;
                    // no need to specify explicit serdes because the resulting key and value types match our default serde settings
                    .groupByKey()
                    .windowedBy(TimeWindows.of(TimeUnit.MINUTES.toMillis(1)))
                    .count()
                    // get users whose one-minute count is >= 3
                    .filter((windowedUserId, count) -> count >= 3);

            // Note: The following operations would NOT be needed for the actual anomaly detection,
            // which would normally stop at the filter() above.  We use the operations below only to
            // "massage" the output data so it is easier to inspect on the console via
            // kafka-console-consumer.

            return anomalousUsers
                    // get rid of windows (and the underlying KTable) by transforming the KTable to a KStream
                    .toStream()
                    // sanitize the output by removing null record values (again, we do this only so that the
                    // output is easier to read via kafka-console-consumer combined with LongDeserializer
                    // because LongDeserializer fails on null values, and even though we could configure
                    // kafka-console-consumer to skip messages on error the output still wouldn't look pretty)
                    .filter((windowedUserId, count) -> count != null)
                    .map((windowedUserId, count) -> new KeyValue<>(windowedUserId.toString(), "User: "
                            + windowedUserId.toString().substring(0, windowedUserId.toString().indexOf('@')).replaceAll("[^a-zA-Z0-9]","")
                            + ", count=" + count
                            + ", dateTime=" + LocalDateTime.now().toString()));

        }
    }

}
