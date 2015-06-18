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

package bv.offa.netbeans.cnd.unittest.googletest;

import bv.offa.netbeans.cnd.unittest.api.CndTestSuite;
import bv.offa.netbeans.cnd.unittest.api.TestFramework;
import org.netbeans.modules.cnd.testrunner.spi.TestRecognizerHandler;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;

    
/**
 * The class {@code GoogleTestSuiteStartedHandler} handles the start of a
 * test suite.
 * 
 * @author offa
 */
class GoogleTestSuiteStartedHandler extends TestRecognizerHandler
{
    private static final TestFramework testFramework = TestFramework.GOOGLETEST;
    
    public GoogleTestSuiteStartedHandler()
    {
        super("^.*?\\[[-]{10}\\].*? [0-9]+? tests?? from ([^ ]+?)$", true);
    }



    /**
     * Updates the ui and test states.
     * 
     * @param mngr  Manager
     * @param ts    Test session
     */
    @Override
    public void updateUI(Manager mngr, TestSession ts)
    {
        final String suiteName = matcher.group(1);
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
            mngr.displayReport(ts, ts.getReport(0));

            TestSuite suite = new CndTestSuite(suiteName, testFramework);
            ts.addSuite(suite);
            mngr.displaySuiteRunning(ts, suite);
        }
        else
        {
            /* Empty */
        }
    }
}
    
    
