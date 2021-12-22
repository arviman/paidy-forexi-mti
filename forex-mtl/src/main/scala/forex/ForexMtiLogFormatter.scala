package forex

import wvlet.log.LogFormatter.{appendStackTrace, highlightLog}
import wvlet.log.{LogFormatter, LogRecord}

object ForexMtiLogFormatter extends LogFormatter {
  override def formatLog(r: LogRecord): String = {
    val log = s"[${highlightLog(r.level, r.leafLoggerName)}] ${highlightLog(r.level, r.getMessage)}"
    appendStackTrace(log, r)
  }
}
