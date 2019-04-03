import com.typesafe.config.ConfigFactory

case class Capi(key: String)
case class Db(uri: String, username: String, password: String)
object Config {

  private val c = ConfigFactory.load
  val capi = Capi(c.getString("capi.key"))
  val database = Db(c.getString("database.uri"),
                    c.getString("database.user"),
                    c.getString("database.password"))
}
