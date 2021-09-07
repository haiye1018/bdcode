import java.io.{File, FileInputStream}
import java.util.Date
import java.util.zip.GZIPInputStream

import scala.io.Source
// scalac -d class/ mr.scala
// Windows C:\\"Program Files"\\Java\\jdk1.8.0_73\\bin\\jar.exe -cvf mr.jar -C class/ .
// Linux jar -cvf mr.jar -C class/ .
// Windows scala -cp .\mr.jar mr "D:\\DownLoad\\climateExample"
// Linux scala -cp ./mr.jar mr /opt/data
object mr {
  def getMaxAndMinTemperatureFromOneFile( filename:String ) : (Int,Int) = {
    var file:scala.io.BufferedSource = null
    if(filename.endsWith("gz")) {
      val fis = new FileInputStream(filename)
      file = Source.fromInputStream(new GZIPInputStream(fis))
    }else{
      file = Source.fromFile(filename)
    }
    var max = 0
    var min = 0
    file.getLines().map(
      line =>
        (line.split(",")(2), line.split(",")(3).toInt)
    ).foreach {
      x =>
        if (x._1 == "TMAX" && x._2 > max) {
          max = x._2
        }
        if (x._1 == "TMIN" && x._2 < min) {
          min = x._2
        }
    }
    file.close()
    return (max,min)
  }

  def iteratorDirectory(basePath: String): Array[File] =
  {
    val dir = new File(basePath)
    return dir.listFiles().filter(_.isFile)
  }

  def getMaxAndMinTemperature(basePath: String): (Int,Int) ={
    val files = iteratorDirectory(basePath).filter(x=>x.getName.endsWith(".csv.gz"))
    var globalMax = 0
    var globalMin = 0
    files.foreach{
      x =>
        print(x.getAbsolutePath + "\n")
        val maxMin = getMaxAndMinTemperatureFromOneFile(x.getAbsolutePath)
        if (maxMin._1 > globalMax) {
          globalMax = maxMin._1
        }
        if (maxMin._2 < globalMin) {
          globalMin = maxMin._2
        }
    }
    return (globalMax,globalMin)
  }
  def main(args: Array[String]) {
    var basePath: String = null
    if (args.length > 0){
      basePath = args(0)
    }else {
      basePath = "D:\\DownLoad\\climateExample"
    }
    var startTime = new Date().getTime

    val maxMin = getMaxAndMinTemperature(basePath)
    printf("max=%d, min=%d\n", maxMin._1,maxMin._2)

    val endTime = new Date().getTime
    printf("Eclipsed Time： %d s\n", (endTime - startTime)/1000)
  }
}
