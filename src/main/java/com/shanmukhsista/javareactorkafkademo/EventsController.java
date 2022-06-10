package com.shanmukhsista.javareactorkafkademo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class EventsController {

    ReactiveKafkaProducerTemplate<String, byte[]> reactiveKafkaProducerTemplate;

    private static final ObjectMapper eventSerializer = new ObjectMapper();

    public EventsController(ReactiveKafkaProducerTemplate<String, byte[]> reactiveKafkaProducerTemplate) {
        this.reactiveKafkaProducerTemplate = reactiveKafkaProducerTemplate;
    }

    @PostMapping(value = "/blocking")
    public Mono<String> publishEventWaitingForKafkaResponse(@RequestBody Event event) {
        try {
            byte[] serializedEvent = eventSerializer.writeValueAsBytes(event);
            return reactiveKafkaProducerTemplate.send("events.main", serializedEvent).subscribeOn(Schedulers.boundedElastic()).map(result -> {
                // handle something from producer response.
                return UUID.randomUUID().toString();
            });
        } catch (IOException exception) {
            return Mono.error(exception);
        }
    }

    @PostMapping
    public Mono<String> publishEventAsync(@RequestBody Event event) {
        try {
            byte[] serializedEvent = eventSerializer.writeValueAsBytes(event);
            // Fire a kafka call but don't wait for the response. Return 200 from our Service.
            reactiveKafkaProducerTemplate.send("events.main", serializedEvent).subscribeOn(Schedulers.boundedElastic()).subscribe();
            return Mono.just(UUID.randomUUID().toString());
        } catch (IOException exception) {
            return Mono.error(exception);
        }
    }
}
