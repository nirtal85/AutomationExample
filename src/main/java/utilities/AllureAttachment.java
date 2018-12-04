package utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.testng.ITestContext;
import org.testng.ITestResult;

import driver.DriverManager;
import driver.DriverManagerFactory;
import driver.DriverType;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AllureAttachment {
	static String videoURL;
	DriverManager driverManager;

	public AllureAttachment(DriverType driverType) {
		driverManager = DriverManagerFactory.getManager(driverType);
	}

	@Attachment(value = "{0}", type = "text/plain")
	public static String addTextAttachment(String message) {
		return message;
	}

	@Attachment(value = "Page Screenshot", type = "image/png")
	@SneakyThrows
	public byte[] addScreenshotAttachment(ITestContext context, ITestResult result) {
		return driverManager.captureScreenshotAsBytes(context, result);
	}

	public static void attachVideo(String sessionId, ITestContext context) {
		try {
			URL videoUrl = new URL(DriverManager.getGridURL(context) + "/video/" + sessionId + ".mp4");
			InputStream is = getVideo(videoUrl);
			Allure.addAttachment("Video", "video/mp4", is, "mp4");
		} catch (Exception e) {
			log.error("attachAllureVideo" + e.getMessage());
		}
	}

	public static InputStream getVideo(URL url) {
		int lastSize = 0;
		int exit = 1;
		for (int i = 0; i < 20; i++) {
			try {
				int size = Integer.parseInt(url.openConnection().getHeaderField("Content-Length"));
				log.info(i + ") Content-Length: " + size);
				if (size > lastSize) {
					lastSize = size;
					Thread.sleep(2000);
				} else if (size == lastSize) {
					exit--;
					Thread.sleep(2000);
				}
				if (exit < 0) {
					return url.openStream();
				}
			} catch (Exception e) {
				log.error("getSelenoidVideo" + e.getMessage());
			}
		}
		return null;
	}

	@SneakyThrows
	public static void deleteVideos(ITestContext context) {
		HttpClient httpClient = HttpClientBuilder.create().build();
		String videoPath = DriverManager.getGridURL(context) + "/video/";
		List<String> videos = Arrays.asList(Jsoup.connect(videoPath).get().body().text().split("\\r?\\n"));
		videos.forEach(video -> {
			try {
				videoURL = videoPath + video;
				HttpDelete httpDelete = new HttpDelete(videoURL);
				httpDelete.setHeader("Content-Type", "application/x-www-form-urlencoded");
				log.info("Video URL: " + video + " Delete request status is: "
						+ Integer.toString(httpClient.execute(httpDelete).getStatusLine().getStatusCode()));
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		});
	}
}