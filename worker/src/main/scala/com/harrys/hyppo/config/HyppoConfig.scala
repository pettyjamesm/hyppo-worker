package com.harrys.hyppo.config

import com.amazonaws.auth.{AWSCredentialsProvider, BasicAWSCredentials, DefaultAWSCredentialsProviderChain}
import com.amazonaws.internal.StaticCredentialsProvider
import com.harrys.hyppo.worker.actor.amqp.RabbitHttpClient
import com.rabbitmq.client.ConnectionFactory
import com.typesafe.config.Config

import scala.concurrent.duration._

/**
 * Created by jpetty on 8/27/15.
 */
abstract class HyppoConfig(config: Config) extends Serializable {

  //  Setup AWS Credentials either through explicit values or defaults from the environment / system properties / instance profile
  final def awsCredentialsProvider: AWSCredentialsProvider = {
    if (config.hasPath("hyppo.aws.access-key-id") && config.hasPath("hyppo.aws.secret-key")){
      new StaticCredentialsProvider(new BasicAWSCredentials(config.getString("hyppo.aws.access-key-id"), config.getString("hyppo.aws.secret-key")))
    } else {
      new DefaultAWSCredentialsProviderChain()
    }
  }

  //  Maximum wait time before work is given up on
  final val workTimeout: FiniteDuration = Duration(config.getDuration("hyppo.work-timeout").toMillis, MILLISECONDS)

  //  Location where date storage occurs
  final val dataBucketName: String = config.getString("hyppo.data-bucket-name")

  //  Timeout duration on operations involving rabbitmq
  final val rabbitMQTimeout: FiniteDuration = Duration(config.getDuration("hyppo.rabbitmq.timeout").toMillis, MILLISECONDS)

  //  Creates a rabbitMQ connection factory
  final def rabbitMQConnectionFactory: ConnectionFactory = {
    val factory = new ConnectionFactory()
    factory.setUri(config.getString("hyppo.rabbitmq.uri"))
    factory.setConnectionTimeout(rabbitMQTimeout.toMillis.toInt)
    factory
  }

  final val rabbitMQApiPort = config.getInt("hyppo.rabbitmq.rest-api-port")

  final val rabbitMQApiSSL  = config.getBoolean("hyppo.rabbitmq.rest-api-ssl")

  final def newRabbitMQApiClient(): RabbitHttpClient = {
    new RabbitHttpClient(rabbitMQConnectionFactory, rabbitMQApiPort, useSSL = rabbitMQApiSSL)
  }

  final def underlying: Config = config
}