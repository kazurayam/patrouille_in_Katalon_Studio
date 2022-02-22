package com.kazurayam.patrouille

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

public class Utils {

	static TestObject byXPath(String xpath) {
		TestObject tObj = new TestObject(xpath)
		tObj.addProperty("xpath", ConditionType.EQUALS, xpath)
		return tObj
	}


	static URL resolveURL(URL base, String relative) {
		if (relative.startsWith("http")) {
			return new URL(relative)
		} else {
			String protocol = base.getProtocol()
			String host = base.getHost()
			Path bp = Paths.get(base.getPath())
			Path resolved = bp.getParent().resolve(relative).normalize()
			return new URL(protocol, host, resolved.toString())
		}
	}

	static int download(URL url, Path path) {
		InputStream inputStream = url.openStream()
		Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
	}
}
