import akka.util.ByteString
import com.typesafe.config.ConfigFactory

object ProductRequirements {
  private val loadConfig = (s: String) => ConfigFactory.load().getString(s)
  private val COLUMN1: String = loadConfig("requirements.field1")
  private val COLUMN2: String = loadConfig("requirements.field2")
  private val VALUE1: String = loadConfig("requirements.value1")
  private val VALUE2: String = loadConfig("requirements.value2")
  private val TARGET: String = loadConfig("requirements.target")

  def fitWithProductRequirements(m: Map[String, String]): Boolean = {
    m(COLUMN1).contains(VALUE1) && m(COLUMN2).contains(VALUE2)
  }
  val getTargetedValue = (m: Map[String, String]) => ByteString(m(TARGET))
}
