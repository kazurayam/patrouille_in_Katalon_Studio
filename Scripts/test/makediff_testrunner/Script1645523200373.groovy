import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase

import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

JobName jobName = new JobName("つみたてNISAの対象商品")
JobTimestamp jobTimestamp = new JobTimestamp("20220222_172254")
Tuple jobName_jobTimestamp = new Tuple(jobName, jobTimestamp)

WebUI.callTestCase(
	findTestCase("main/makeDiff"),
	["jobDescription": jobName_jobTimestamp]
)
