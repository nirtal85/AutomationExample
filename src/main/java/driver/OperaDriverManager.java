package driver;

import java.net.URI;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;

import lombok.SneakyThrows;

public class OperaDriverManager extends DriverManager {

	@Override
	@SneakyThrows
	protected void createDriver(ITestContext context, ITestResult result) {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setBrowserName("opera");
		capabilities.setVersion("55.0");
		capabilities.setCapability("screenResolution", "1280x1024x24");
		capabilities.setCapability("name", result.getMethod().getMethodName());
		capabilities.setCapability("enableVNC", true);
		if ("true".equals(System.getProperty("enableVideo"))) {
			capabilities.setCapability("enableVideo", true);
			capabilities.setCapability("videoFrameRate", 24);
		}
		driver.set(new RemoteWebDriver(URI.create(getGridURL(context) + "/wd/hub").toURL(), capabilities));
	}
}
