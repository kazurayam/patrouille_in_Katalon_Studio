import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.testng.keyword.TestNGBuiltinKeywords as TestNGKW
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.Keys as Keys
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.materialstore.MaterialstoreFacade
import com.kazurayam.materialstore.resolvent.Artifact
import com.kazurayam.materialstore.resolvent.ArtifactGroup
import com.kazurayam.materialstore.filesystem.FileType
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import com.kazurayam.materialstore.metadata.Metadata
import com.kazurayam.materialstore.metadata.MetadataPattern
import com.kazurayam.patrouille.Utils
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.util.KeywordUtil

/**
 * main/resolvent
 * 
 * 1. requires a parameter named "jobDescription" of Tuple which contains
 *    - [0] - a JobName which the "download" script created
 *    - [1] - a JobTimestamp which the "download" script created last
 * 
 * 2. perform "Chronological comparison" of the latest job and the 2nd latest job.
 * 
 * 3. create a new JobTimestamp directory where the comparison result is stored.
 */

// instantiate the store object
Path projectDir = Paths.get(RunConfiguration.getProjectDir())
Store store = Stores.newInstance(projectDir.resolve("store"))

// make sure the parameter is provided
assert jobDescription != null
JobName jobName = (JobName)jobDescription[0]
JobTimestamp latestTimestamp = (JobTimestamp)jobDescription[1]

WebUI.comment("jobName=${jobName.toString()}")
WebUI.comment("latestTimestamp=${latestTimestamp.toString()}")


// identify the 2nd latest jobTimestamp
JobTimestamp previousTimestamp = store.findJobTimestampPriorTo(jobName, latestTimestamp)
if (previousTimestamp == JobTimestamp.NULL_OBJECT) {
	KeywordUtil.markFailedAndStop("previous JobTimestamp prior to ${latestTimestamp} is not found")
}

// Look up the materials stored in the previous time of run
MaterialList left = store.select(jobName, previousTimestamp, MetadataPattern.ANY)
assert left.size() > 0

// Look up the materials stored in the latest time of run
MaterialList right = store.select(jobName, latestTimestamp, MetadataPattern.ANY)
assert right.size() > 0

// the facade clall that work for you
MaterialstoreFacade facade = MaterialstoreFacade.newInstance(store)

// do comparing while create diff.
// The result will be carried in the instances of Artifact class
ArtifactGroup prepared = 
    ArtifactGroup.builder(left, right)
	    .ignoreKeys("URL.protocol", "URL.host", "URL.port")
		.build()

// now make the diff of the left and the right
ArtifactGroup workedOut = facade.workOn(prepared)

// if the difference is greater than this criteria value (unit %),
// the difference should be marked
double criteria = 0.1d
