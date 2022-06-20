package org.hrw.spring.graphql.resource

import org.hrw.spring.graphql.resource.response.Notification
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.data.method.annotation.ContextValue
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

@RestController
class NotificationResource(private val sinks: Sinks.Many<Notification>) {
    @SubscriptionMapping(value = "notification")
    fun notification(@ContextValue(required = true) userId: String): Flux<Notification> {
        sinks.tryEmitNext(Notification(userId, "some-notification"))
        return sinks.asFlux()
    }
}

@Configuration
class Config {
    @Bean
    fun sinks() = Sinks.many().multicast().onBackpressureBuffer<Notification>()
}