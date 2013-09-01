package s3downloader

import org.specs2.mutable.Specification

/**
 * User: mauricio
 * Date: 9/1/13
 * Time: 1:40 PM
 */
class MainSpec extends Specification {

  "main" should {
    "generate the sizes correctly" in {
      Main.toPieces( 33, 4 ) === List((0,8), (9,16), (17,24), (25,32), (33,33))
    }
  }

}
