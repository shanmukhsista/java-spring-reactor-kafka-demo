

    @PostMapping
    public Mono<String> publishEventAsync(@RequestBody Event event) {
        try {
            byte[] serializedEvent = eventSerializer.writeValueAsBytes(event);
            reactiveKafkaProducerTemplate.send("events.main", serializedEvent).subscribeOn(Schedulers.boundedElastic()).subscribe();
            return Mono.just(UUID.randomUUID().toString());

        } catch (IOException exception) {
            return Mono.error(exception);
        }

    }

    @PostMapping
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