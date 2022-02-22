import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import com.kazurayam.materialstore.metadata.Metadata
import com.kazurayam.patrouille.Utils
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

/**
 * main/download
 * 
 * 1. get access to a web page of the Financial Services Agency, Japan 金融庁
 * 2. look up URLs of Excel files published in the page
 * 3. download the .xlsx files
 * 4. store the materials into the materialstore with 
 *    JobName = the title of the web page
 *    JobTimestamp = the timestamp when this script ran 
 */
Path projectDir = Paths.get(RunConfiguration.getProjectDir())

Store store = Stores.newInstance(projectDir.resolve("store"))
JobName jobName = new JobName("つみたてNISAの対象商品")
JobTimestamp jobTimestamp = JobTimestamp.now()

URL base = new URL("https://www.fsa.go.jp/policy/nisa2/about/tsumitate/target/index.html")
WebUI.openBrowser('')
WebUI.navigateToUrl(base.toString())

List<TestObject> targets = Arrays.asList(
		Utils.byXPath("(//a[text()='EXCEL'])[1]"),
		Utils.byXPath("(//a[text()='EXCEL'])[2]"),
		Utils.byXPath("(//a[text()='EXCEL'])[3]"),
		Utils.byXPath("(//a[text()='EXCEL'])[4]"))

targets.eachWithIndex { tObj, index ->
	if (WebUI.verifyElementPresent(tObj, 5, FailureHandling.CONTINUE_ON_FAILURE)) {
		String relative = WebUI.getAttribute(tObj, "href")
		
		println "relative:${relative}"
		
		WebUI.comment("href____=${relative}")
		URL resolved = Utils.resolveURL(base, relative)
		WebUI.comment("resolved=${relative}")
		Path tempFile = Files.createTempFile(null, null)
		assert tempFile != null
		Utils.download(resolved, tempFile)
		assert Files.exists(tempFile)
		assert tempFile.size() > 0
		
		Metadata metadata = 
			new Metadata.Builder(resolved)
						.put("seq", index + "").build()
		store.write(jobName, jobTimestamp, FileType.XLSX, metadata, tempFile)
		
	}
}

WebUI.delay(3)
WebUI.closeBrowser()

return new Tuple(jobName, jobTimestamp)
