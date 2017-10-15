package models

/**
  * Created by ERAN on 10/14/2017.
  */
case class URLAnalysis(
 isValid: Boolean,
 status: Int,
 data: Seq[AnalysisElement])

case class AnalysisElement(key: String, value: String)
