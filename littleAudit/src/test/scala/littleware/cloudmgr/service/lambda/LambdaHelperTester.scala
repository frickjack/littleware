package littleware.cloudmgr.service.lambda

import com.google.inject

import littleware.cloudmgr.service
import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith

import LambdaHelper.CookieInfo

class LambdaHelperTester extends littleware.scala.test.LittleTest {
    
    @Test
    def testParseCookies() = try {
        val testStr = "abc=ABC;def=DEF;ghi=GHI;"
        val expected = Map("abc" -> "ABC", "def" -> "DEF", "ghi" -> "GHI")
        val result = LambdaHelper.parseCookies(testStr)
        assertEquals("parseCookies gave expected result", expected, result)
    } catch basicHandler

    @Test
    def testCookieToString() = try {
        val tests = Seq(
            CookieInfo("test", "value", None, 0) -> "test=value; Path=/; Secure; HttpOnly; SameSite=Strict",
            CookieInfo("test", "value", None, 900) -> "test=value; Path=/; Secure; HttpOnly; SameSite=Strict; Max-Age=900",
            CookieInfo("test", "value", Some(".frickjack.com"), 900) -> "test=value; Path=/; Secure; HttpOnly; SameSite=Strict; Max-Age=900; Domain=.frickjack.com",
            CookieInfo("test", "value", Some(".frickjack.com"), -1) -> "test=value; Path=/; Secure; HttpOnly; SameSite=Strict; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Domain=.frickjack.com"
        )
        tests.foreach(_ match {
            case (input, expected) => 
                assertEquals("cookie toString gave expected", expected, input.toString())
        })
    } catch basicHandler
}
