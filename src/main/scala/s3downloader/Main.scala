package s3downloader

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.model.GetObjectRequest
import java.io.File
import java.util.concurrent.{Executors, ConcurrentHashMap}

/**
 * User: mauricio
 * Date: 9/1/13
 * Time: 1:23 PM
 */

case class Status(piece: Int, finished: Boolean)

object Main {

  def main(args: Array[String]) {

    val key = args(0)
    val secret = args(1)
    val bucket = args(2)
    val credentials = new BasicAWSCredentials(key, secret)
    val path = args(3)
    val pieces = 80
    val map = new ConcurrentHashMap[Int, Boolean]()
    val pool = Executors.newFixedThreadPool(20)

    val client = new AmazonS3Client(credentials)
    val metadata = client.getObjectMetadata(bucket, path)
    val size = metadata.getContentLength
    val filenameFormat = "filename-%03d.part"

    val ranges = toPieces(size, pieces)

    println(s"Fill document size is ${size}")
    println(s"Downloading pieces ${ranges}")

    ranges.zipWithIndex.foreach {
      case (pair, index) =>
        val start = pair._1
        val end = pair._2
        val size = pair._2 - pair._1

        val request = new GetObjectRequest(bucket, path)
          .withRange(start, end)
        val file = new File(filenameFormat.format(index))

        if (file != size) {
          val runnable = new Runnable {
            def run() {
              try {
                println(s"starting for index ${index} to file ${file.getAbsolutePath}")
                client.getObject(request, file)
                map.put(index, true)
              } catch {
                case e: Exception => {
                  map.put(index, false)
                  println(s"Failed to execute part ${index}")
                  e.printStackTrace()
                }
              }
            }
          }

          pool.submit(runnable)
        } else {
          map.put(index, true)
        }
    }

    while (map.size() != ranges.size) {
      println("Waiting for work to finish")
      Thread.sleep(5000)
    }

    pool.shutdown()

  }

  def toPieces(size: Long, parts: Long): Seq[(Long, Long)] = {

    val partSize = size / parts
    val range = if ((size % parts) == 0) {
      0L.until(parts)
    } else {
      0L.until(parts).inclusive
    }

    range.foldRight(List[(Long, Long)]()) {
      (value, accumulated) =>
        val result = if (accumulated.isEmpty) {
          (0L -> partSize)
        } else {
          val last = accumulated.head
          val start = last._2 + 1
          val end = last._2 + partSize
          if (end > size) {
            (start -> size)
          } else {
            (start -> end)
          }
        }

        result :: accumulated
    }.reverse
  }

}
