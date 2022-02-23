import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.materialstore.MaterialstoreFacade
import com.kazurayam.materialstore.MaterialstoreFacade.Result
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import com.kazurayam.materialstore.resolvent.ArtifactGroup
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

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
MaterialList left = store.select(jobName, previousTimestamp, QueryOnMetadata.ANY)
assert left.size() > 0

// Look up the materials stored in the latest time of run
MaterialList right = store.select(jobName, latestTimestamp, QueryOnMetadata.ANY)
assert right.size() > 0

// the facade clall that work for you
MaterialstoreFacade facade = MaterialstoreFacade.newInstance(store)

// do comparing while create diff.
// The result will be carried in the instances of Artifact class
ArtifactGroup prepared = 
    ArtifactGroup.builder(left, right)
	    .ignoreKeys("URL.protocol", "URL.host", "URL.port")
		.build()

// if the difference is greater than this criteria value (unit %),
// the difference should be marked
double criteria = 0.1d

// file name of HTML report
String fileName = jobName.toString() + "-index.html"

// make diff and compile report
Result result = facade.makeDiffAndReport(jobName, prepared, criteria, fileName)

assert Files.exists(result.report())
WebUI.comment("The report can be found at ${result.report()}")

// if any significant difference found, this Test Case should FAIL
int warnings = result.warnings()
if (warnings > 0) {
	KeywordUtil.makrWarning("found ${warnings} differences")
}