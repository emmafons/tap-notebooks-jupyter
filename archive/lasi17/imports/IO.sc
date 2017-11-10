object Local {
  import java.io.File
  import scala.io.Source

  val IN_DIR_NAME = "/input_files"
  val OUT_DIR_NAME = "/output_files"

  val thisDir = new File(".").getCanonicalPath
  val inputFileDir = thisDir+IN_DIR_NAME
  val outputFileDir = thisDir+OUT_DIR_NAME

  val visibleFile = (file:File) => !file.isHidden
  val textFile = (file:File) => file.getName.split('.').last.contains("txt")

  def directoryFromString(directory:String):Option[File] = {
    val thisDir = new File(directory)
    if (thisDir.exists && thisDir.isDirectory) Some(thisDir)
    else None
  }

  def listFiles(directory:String):List[File] = {
    directoryFromString(directory) match {
      case Some(dir) => dir.listFiles.toList
      case None => List[File]()
    }
  }

  def listThisDir = listFiles(thisDir)

  def listThisDirVisible = listThisDir.filter(visibleFile)

  def listThisDirText = listThisDirVisible.filter(textFile)

  def readFile(file:File) = {
    val source = Source.fromFile(file.getCanonicalPath)
    try {
      source.getLines.mkString("\n\n")
    } finally {
      source.close
    }
  }
}



object Tap {

  import scalaj.http._

  //val API_URL = "https://b9yiddda69.execute-api.ap-southeast-2.amazonaws.com/initialtest/v1"
  val API_URL = "http://localhost:8080/v1"
  val HEALTH_URL = API_URL+"/health"
  val CLEAN_URL = API_URL+"/analyse/text/clean"

  case class Message(message:String)
  case class Results(message:String,results:List[String])

  def serverDetails = Http(API_URL).asString

  def getHealthMessage = {
    println(s"Connecting to $HEALTH_URL")
    val response = Http(HEALTH_URL).asString
    //println(response)
    upickle.default.read[Message](response.body)
  }

  def serverIsHealthy = {
    try { getHealthMessage.message=="ok" }
    catch { case e:Exception => {
      println(s"There was a problem with the server: $e")
      false }
    }
  }

  def cleanText(text:String) = {
    //println(s"Cleaning text: $text")
    val response = Http(CLEAN_URL).postData(text).header("content-type", "application/json").asString
    //println(response)
    upickle.default.read[Results](response.body)
  }
}