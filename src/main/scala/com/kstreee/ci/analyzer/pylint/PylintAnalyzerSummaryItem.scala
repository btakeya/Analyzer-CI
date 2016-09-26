package com.kstreee.ci.analyzer.pylint

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class PylintAnalyzerSummaryItem(message: String,
                                     obj: String,
                                     line: Int,
                                     column: Int,
                                     path: String,
                                     messageId: String,
                                     errorType: String,
                                     symbol: String,
                                     module: String)

object PylintAnalyzerSummaryItem {
  implicit val pylintReportReads: Reads[PylintAnalyzerSummaryItem] = (
    (JsPath \ "message").read[String] and
      (JsPath \ "obj").read[String] and
      (JsPath \ "line").read[Int] and
      (JsPath \ "column").read[Int] and
      (JsPath \ "path").read[String] and
      (JsPath \ "message-id").read[String] and
      (JsPath \ "type").read[String] and
      (JsPath \ "symbol").read[String] and
      (JsPath \ "module").read[String]
    ) (PylintAnalyzerSummaryItem.apply _)
}