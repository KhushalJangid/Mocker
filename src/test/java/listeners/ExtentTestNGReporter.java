package listeners;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import utils.EmailClient;
import utils.EmailReportBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.testng.*;

public class ExtentTestNGReporter implements ITestListener {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> test = new ThreadLocal<>();
    private static String reportPath = null;

    @Override
    public void onStart(ITestContext context) {
        try {
            if (reportPath == null) {
                final LocalDateTime now = LocalDateTime.now();

                final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
                reportPath = String.format("reports/ExtentReport-%s.html", dtf.format(now));
                final ExtentSparkReporter reporter = new ExtentSparkReporter(reportPath);
                reporter.loadXMLConfig("src/test/resources/extent-config.xml");

                extent = new ExtentReports();
                extent.attachReporter(reporter);
                extent.setSystemInfo("Tester", System.getProperty("user.name", "Khushal Jangid"));
                extent.setSystemInfo("Operating System", System.getProperty("os.name", "Unknown"));
                extent.setSystemInfo("OS Version", System.getProperty("os.version", "Unknown"));
                extent.setSystemInfo("Architecture", System.getProperty("os.arch", "Unknown"));
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest extentTest = extent.createTest(result.getMethod().getMethodName());
        test.set(extentTest);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.get().pass(String.format("Hash: %s, ClassName: %s, TestName: %s", result.hashCode(), result.getInstanceName(), result.getName()));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        test.get().fail(String.format("Test failed : %s", result.getThrowable()));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().skip("Test skipped");
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
        try {
            String htmlReportBody = EmailReportBuilder.generateHtmlReportBody(context);
            EmailClient.sendEmailWithReport(htmlReportBody, reportPath);
        } catch (FileNotFoundException e) {
            System.out.println("smtp.properties file not found in src/test/resources, skipping email sending");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO Error reading smtp.properties file, skipping email sending");
        }
    }
}