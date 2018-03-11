package com.kstreee.ci.analysis

final case class AnalysisReportItem(path: String,
                                    filename: String,
                                    line: Int,
                                    column: Int,
                                    message: String,
                                    author: Option[String] = None) {
  override def toString: String = {
    s"""
       |path : $path
       |filename : $filename
       |line : $line
       |column : $column
       |message : $message
     """.stripMargin
  }
}