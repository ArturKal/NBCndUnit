/*
 * NBCndUnit - C/C++ unit tests for NetBeans.
 * Copyright (C) 2015  offa
 * 
 * This file is part of NBCndUnit.
 *
 * NBCndUnit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NBCndUnit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NBCndUnit.  If not, see <http://www.gnu.org/licenses/>.
 */

package bv.offa.netbeans.cnd.unittest.libunittestcpp;

import bv.offa.netbeans.cnd.unittest.TestSupportUtils;
import bv.offa.netbeans.cnd.unittest.api.CndTestCase;
import bv.offa.netbeans.cnd.unittest.api.CndTestSuite;
import bv.offa.netbeans.cnd.unittest.api.TestFramework;
import org.netbeans.modules.cnd.testrunner.spi.TestRecognizerHandler;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;

/**
 * The class {@code LibunittestCppTestFinishedHandler} handles the test
 * output.
 * 
 * @author offa
 */
class LibunittestCppTestFinishedHandler extends TestRecognizerHandler
{
    private static final TestFramework testFramework = TestFramework.LIBUNITTESTCPP;
    private static final String MSG_OK = "ok";
    private static final String MSG_FAILED = "FAIL";
    private static final String MSG_SKIP = "SKIP";


    public LibunittestCppTestFinishedHandler()
    {
        super("^(.+?)::(.+?) \\.{3} \\[([0-9].*?)s\\] (ok|FAIL|SKIP).*?$", true);
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
            currentSuite = new CndTestSuite(suiteName, testFramework);
            ts.addSuite(currentSuite);
            mngr.displaySuiteRunning(ts, currentSuite);
        }
        else if( currentSuite.getName().equals(suiteName) == false )
        {
            mngr.displayReport(ts, ts.getReport(0L));

            TestSuite suite = new CndTestSuite(suiteName, testFramework);
            ts.addSuite(suite);
            mngr.displaySuiteRunning(ts, suite);
        }
        else
        {
            /* Empty */
        }

        final String testName = normalise(matcher.group(2));
        Testcase testCase = new CndTestCase(testName, testFramework, ts);
        testCase.setClassName(suiteName);
        testCase.setTimeMillis(TestSupportUtils.parseTimeSecToMillis(matcher.group(3)));

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
            throw new IllegalStateException("Unknown result: <" + result + ">");
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
    
