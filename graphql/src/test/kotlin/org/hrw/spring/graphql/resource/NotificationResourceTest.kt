package org.hrw.spring.graphql.resource

import graphql.ExecutionInput
import graphql.execution.instrumentation.SimpleInstrumentation
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters
import org.hrw.spring.graphql.resource.response.Notification
import org.hrw.spring.graphql.resource.response.User
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.GraphQlTester
import reactor.core.publisher.Sinks
import reactor.test.StepVerifier
import java.math.BigDecimal


@GraphQlTest(NotificationResource::class)
@Import(NotificationResourceTest.MyInstrumentation::class, Config::class)
class NotificationResourceTest {

    @Autowired
    private lateinit var graphQlTester: GraphQlTester

    @Autowired
    private lateinit var sinks: Sinks.Many<Notification>


    @Test
    fun `should return notification to user`() {
        // Given
        val document = """
            subscription
            {
                notification {
                    id
                    message
                    user {
                        id
                        name
                    }
                }
            }
            """

        // When
        val response =
            graphQlTester.document(document)
                .executeSubscription()
                .toFlux("notification", Notification::class.java)

        sinks.tryEmitNext(
            Notification(
                id = "some-id",
                message = "some-notification",
                user = User(id = "some-user-id", name = "some-user-name")
            )
        )
        sinks.tryEmitComplete()
        // Then
        StepVerifier.create(response)
            .expectSubscription()
            .expectNext(
                Notification(
                    id = "some-user-id",
                    message = "some-notification",
                    user = User(id = "some-user-id", name = "some-user-name")
                )
            )
            .verifyComplete()

    }

    internal class MyInstrumentation : SimpleInstrumentation() {
        override fun instrumentExecutionInput(
            input: ExecutionInput,
            params: InstrumentationExecutionParameters?
        ): ExecutionInput {
            input.graphQLContext.put("userId", "some-user-id")
            return input
        }
    }
}