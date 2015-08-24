package com.ft.universalpublishing.documentstore.transform;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(monochrome = true, tags = { "@BodyTransformation", "~@Ignore" }, format = { "pretty" })
public class RunBodyTransformationTest {
}
