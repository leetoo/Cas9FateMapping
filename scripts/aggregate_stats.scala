import scala.io._
import java.io._
import scala.collection.mutable._
import scala.util.matching.Regex

val statsFiles = args(0).split(",")
val output = new PrintWriter(args(1))
val output2 = new PrintWriter(args(2))

output2.write("sample\tusedUMI\ttotalUMI\n")

val minBasesMatchingPostAlignment = 0.90
val passString = "PASS"
var outputHeader = false

val keep_column_start = 0
val keep_column_end_plus_one = 23

statsFiles.foreach{statsFileLine => {
  val statsFile = new File(statsFileLine)

  if (statsFile.exists) {
    val sample = statsFile.getName.split("\\.")(0)
    val openStatsFile = Source.fromFile(statsFile).getLines()
    val header = openStatsFile.next()
    val headerTokens = new HashMap[String,Int]()
    header.split("\t").zipWithIndex.map{case(tk,ind) => headerTokens(tk) = ind}

    if (!outputHeader) {
      output.write("sample\t" + header.split("\t").slice(keep_column_start,keep_column_end_plus_one).mkString("\t") + "\n")
      outputHeader = true
    }

    var usedLines = 0
    var totalLines = 0
    openStatsFile.foreach{line => {
      val sp = line.split("\t")
      val fwdMatch = sp(headerTokens("fwdBasesMatching")).toDouble
      val revMatch = sp(headerTokens("revBasesMatching")).toDouble
      val passFail = sp(headerTokens("fail.reason"))

      if (fwdMatch > minBasesMatchingPostAlignment /*&& revMatch > minBasesMatchingPostAlignment*/ && passFail == passString && !(line contains "WT_") && !(line contains "UNKNOWN")) {
        output.write(sample + "\t" + sp.slice(keep_column_start,keep_column_end_plus_one).mkString("\t") + "\n")
        usedLines += 1
      }
      totalLines += 1
    }}
    println("processed " + statsFile + " with " + usedLines + " UMIs used of a total " + totalLines + " UMIs")
    output2.write(sample + "\t" + usedLines + "\t" + totalLines + "\n")

  } else {
    throw new IllegalStateException("Unable to find file " + statsFileLine)
  }
}}
output2.close()
output.close()
