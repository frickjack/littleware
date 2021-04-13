package littleware.audit

import java.util.logging.{Logger, Level}

object Cli {
    val log = Logger.getLogger(Cli.getClass().getName())

    def main(args:Array[String]):Unit = {
        log.log(Level.INFO, s"""{ "context": "startup", "message": "hello" }""")
    }
}