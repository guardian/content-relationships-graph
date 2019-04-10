
import io.circe.generic.semiauto.deriveDecoder
import io.circe.parser
import io.circe.HCursor
import io.circe.Decoder

case class XByLine(name: String, gender: Int)
case class XPerson(description: String, gender: Int)
case class XLocation(description: String)
case class XOrganisation(description: String)
case class XData(
                  path: String,
                  headline: String,
                  bodyText: String,
                  bylines: List[XByLine],
                  people: List[XPerson],
                  locations: List[XLocation],
                  organisations: List[XOrganisation]
                )

object XDataProcessing {

  //implicit val xByLineDecoder = deriveDecoder[XByLine]
  /*
          {
              "Name": "Andrew Pulver",
              "Gender": 0
          }
   */
  implicit val xByLineDecoder: Decoder[XByLine] =
    (hCursor: HCursor) => {
      for {
        name <- hCursor.get[String]("Name")
        gender <- hCursor.downField("Gender").as[Int]
      } yield XByLine(name, gender)
    }

  //implicit val xPersonDecoder = deriveDecoder[XPerson]
  /*
          {
              "BeginOffset": 84,
              "EndOffset": 95,
              "Score": 0.9856677055358887,
              "Text": "Woody Allen",
              "Type": "PERSON",
              "Gender": 0
          }
   */
  implicit val xPersonDecoder: Decoder[XPerson] =
    (hCursor: HCursor) => {
      for {
        description <- hCursor.get[String]("Text")
        gender <- hCursor.downField("Gender").as[Int]
      } yield XPerson(description, gender)
    }


  //implicit val xLocationDecoder = deriveDecoder[XLocation]
  /*
          {
              "BeginOffset": 382,
              "EndOffset": 390,
              "Score": 0.9875016212463379,
              "Text": "New York",
              "Type": "LOCATION"
          }
   */
  implicit val xLocationDecoder: Decoder[XLocation] =
    (hCursor: HCursor) => {
      for {
        description <- hCursor.get[String]("Text")
      } yield XLocation(description)
    }

  //implicit val xOrganisationDecoder = deriveDecoder[XOrganisation]
  /*
          {
              "BeginOffset": 0,
              "EndOffset": 6,
              "Score": 0.9991258978843689,
              "Text": "Amazon",
              "Type": "ORGANIZATION"
          }
   */
  implicit val xOrganisationDecoder: Decoder[XOrganisation] =
  (hCursor: HCursor) => {
    for {
      description <- hCursor.get[String]("Text")
    } yield XOrganisation(description)
  }

  implicit val xDataDecoder = deriveDecoder[XData]

  def readSampleFile(): Option[XData] = {
    val stream = getClass.getResourceAsStream("/jonathan-sample.json")
    val input = scala.io.Source.fromInputStream( stream ).getLines.mkString
    val result = parser.decode[XData](input)
    result match {
      case Right(data) => Some(data)
      case Left(_) => None
    }
  }

}