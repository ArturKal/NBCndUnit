package bv.offa.netbeans.cnd.unittest.libunittestcpp;

import static bv.offa.netbeans.cnd.unittest.libunittestcpp.LibunittestCppTestHandlerFactory.parseSecTimeToMillis;
import org.netbeans.modules.cnd.testrunner.spi.TestRecognizerHandler;
import org.netbeans.modules.gsf.testrunner.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;

/**
 * The class {@code LibunittestCppTestFinishedHandler} handles the test
 * output.
 */
class LibunittestCppTestFinishedHandler extends TestRecognizerHandler
{
    private static final String LIBUNITTESTCPP = "LibunittestC++"; //NOI18N
    private static final String MSG_OK = "ok"; //NOI18N
    private static final String MSG_FAILED = "FAIL"; //NOI18N
    private static final String MSG_SKIP = "SKIP"; //NOI18N


    public LibunittestCppTestFinishedHandler()
    {
        super("^(.+?)::(.+?) \\.{3} \\[([0-9].*?)s\\] (ok|FAIL|SKIP).*?$", true); //NOI18N
    }



    /**
     * Updates the ui and test states.
     * 
     * @param mngr  Manager
     * @param ts    Test session
     * @exception IllegalStateException If the handler gets into an
     *                                  illegal state or parses unknown
     *                                  output values
     */
    @Override
    public void updateUI(Manager mngr, TestSession ts)
    {
        final String suiteName = normalise(matcher.group(1));
        TestSuite currentSuite = ts.getCurrentSuite();

        if( currentSuite == null )
        {
            mngr.testStarted(ts);
            currentSuite = new TestSuite(suiteName);
            ts.addSuite(currentSuite);
            mngr.displaySuiteRunning(ts, currentSuite);
        }
        else if( currentSuite.getName().equals(suiteName) == false )
        {
            mngr.displayReport(ts, ts.getReport(0L));

            TestSuite suite = new TestSuite(suiteName);
            ts.addSuite(suite);
            mngr.displaySuiteRunning(ts, suite);
        }
        else
        {
            /* Empty */
        }

        final String testName = normalise(matcher.group(2));
        Testcase testCase = new Testcase(testName, LIBUNITTESTCPP, ts);
        testCase.setClassName(suiteName);
        testCase.setTimeMillis(parseSecTimeToMillis(matcher.group(3)));

        final String result = matcher.group(4);

        if( result.equals(MSG_OK) == true )
        {
            // Testcase ok
        }
        else if( result.equals(MSG_FAILED) == true )
        {
            Trouble trouble = new Trouble(true);
            testCase.setTrouble(trouble);
        }
        else if( result.equals(MSG_SKIP) == true )
        {
            testCase.setStatus(Status.SKIPPED);
        }
        else
        {
            throw new IllegalStateException("Unknown result: <" + result + ">"); //NOI18N
        }

        ts.addTestCase(testCase);
    }


    /**
     * Normalises the input. This will replace all prohibited characters.
     * 
     * @param input     Input string
     * @return          Normalised output
     */
    private String normalise(String input)
    {
        return input.replace('<', '(').replace('>', ')');
    }
}
    
