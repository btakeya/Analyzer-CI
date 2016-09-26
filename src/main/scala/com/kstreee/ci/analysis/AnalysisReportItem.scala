package com.kstreee.ci.analysis

final case class AnalysisReportItem(path: String,
                                    filename: String,
                                    author: String,
                                    line: Int,
                                    column: Int,
                                    message: String) {
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