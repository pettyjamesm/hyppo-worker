package com.harrys.hyppo.worker.actor.amqp

import akka.testkit.TestActorRef
import com.harrys.hyppo.config.CoordinatorConfig
import com.harrys.hyppo.worker.{TestObjects, TestConfig}
import com.harrys.hyppo.worker.api.proto.CreateIngestionTasksRequest

import scala.util.Try

/**
 * Created by jpetty on 9/17/15.
 */
class QueueProxyActorTests extends RabbitMQTests  {

  val config = new CoordinatorConfig(TestConfig.basicTestConfig)


  "The Queue Proxy" must {
    val proxy = TestActorRef(new RabbitWorkQueueProxy(config))

    "successfully enqueue messages" in {
      val source      =  TestObjects.testIngestionSource(name = "queue proxy")
      val integration = TestObjects.testProcessedDataIntegration(source)
      val work        = CreateIngestionTasksRequest(integration, TestObjects.testIngestionJob(source))
      val connection  = config.rabbitMQConnectionFactory.newConnection()
      val channel     = connection.createChannel()
      val queueName = HyppoQueue.integrationQueueName(integration)

      try {
        channel.queueDelete(queueName)
        proxy ! work
        within(config.rabbitMQTimeout){
          awaitAssert(channel.queueDeclarePassive(queueName).getMessageCount shouldBe 1)
        }
      } finally {
        Try(channel.queueDelete(queueName))
        Try(channel.close())
        Try(connection.close())
      }
    }
  }
}