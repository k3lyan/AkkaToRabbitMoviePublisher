import akka.stream.alpakka.amqp.AmqpConnectionFactoryConnectionProvider
import com.rabbitmq.client.ConnectionFactory
import com.typesafe.config.ConfigFactory

object RabbitMQConnect {
  private val loadConfig = (s: String) => ConfigFactory.load().getString(s)
  private val RABBITMQ_HOST = loadConfig("rabbitmq.host")
  private val RABBITMQ_PORT = loadConfig("rabbitmq.port")
  private val RABBITMQ_PWD = loadConfig("rabbitmq.password")
  private val RABBITMQ_USER = loadConfig("rabbitmq.username")
  val RABBITMQ_QUEUE = loadConfig("rabbitmq.queue")
  val RABBITMQ_EXCHANGEE = loadConfig("rabbitmq.exchange")

  def getConnection: AmqpConnectionFactoryConnectionProvider = {
    val factory = new ConnectionFactory()
    factory.setHost(RABBITMQ_HOST)
    factory.setPort(RABBITMQ_PORT.toInt)
    factory.setUsername(RABBITMQ_USER)
    factory.setPassword(RABBITMQ_PWD)
    AmqpConnectionFactoryConnectionProvider(factory)
  }
}
