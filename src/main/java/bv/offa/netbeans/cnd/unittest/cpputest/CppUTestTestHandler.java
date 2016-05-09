/*
 * NBCndUnit - C/C++ unit tests for NetBeans.
 * Copyright (C) 2015-2016  offa
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

package bv.offa.netbeans.cnd.unittest.cpputest;

import bv.offa.netbeans.cnd.unittest.api.CndTestCase;
import bv.offa.netbeans.cnd.unittest.api.CndTestHandler;
import bv.offa.netbeans.cnd.unittest.api.CndTestSuite;
import bv.offa.netbeans.cnd.unittest.api.ManagerAdapter;
import bv.offa.netbeans.cnd.unittest.api.TestFramework;
import org.netbeans.modules.gsf.testrunner.api.TestSession;

/**
 * The class {@code CppUTestHandler} handles the test output.
 *
 * @author offa
 */
class CppUTestTestHandler extends CndTestHandler
{
    private static final int GROUP_IGNORED = 1;
    private static final int GROUP_SUITE = 2;
    private static final int GROUP_CASE = 3;
    private static final int GROUP_TIME = 4;
    private static final int GROUP_TIME_VALUE = 5;
    private static final TestFramework TESTFRAMEWORK = TestFramework.CPPUTEST;
    private static boolean firstSuite;
    private final TestSessionInformation info;

    public CppUTestTestHandler(TestSessionInformation info)
    {
        super("^(IGNORE_)??TEST\\(([^, ]+?), ([^, ]+?)\\)"
                + "( \\- ([0-9]+?) ms)?$", true, true);
        this.info = info;
        suiteFinished();
    }

    
    /**
     * Updates the UI.
     * 
     * @param manager       Manager Adapter
     * @param session       Test session
     */
    @Override
    public void updateUI(ManagerAdapter manager, TestSession session)
    {
        final String suiteName = getMatchGroup(GROUP_SUITE);
        CndTestSuite currentSuite = (CndTestSuite) session.getCurrentSuite();

        if( isSameTestSuite(currentSuite, suiteName) == false )
        {
            if( firstSuite == true )
            {
                manager.testStarted(session);
                firstSuite = false;
            }
            else
            {
                manager.displayReport(session, session.getReport(0));
            }

            currentSuite = new CndTestSuite(suiteName, TESTFRAMEWORK);
            session.addSuite(currentSuite);
            manager.displaySuiteRunning(session, currentSuite);
        }
        
        final String caseName = getMatchGroup(GROUP_CASE);
        CndTestCase testcase = new CndTestCase(caseName, TESTFRAMEWORK, session);
        testcase.setClassName(suiteName);
        
        final String ignored = getMatchGroup(GROUP_IGNORED);
        final String time = getMatchGroup(GROUP_TIME);
        
        if( ignored != null )
        {
            testcase.setSkipped();
        }
        else if( time != null )
        {
            final String timeValue = getMatchGroup(GROUP_TIME_VALUE);
            long testTime = Long.valueOf(timeValue);
            testcase.setTimeMillis(testTime);
            info.addTime(testTime);
        }
        else
        {
            // Test time is separated, eg. failed or test with additional output
        }
        
        session.addTestCase(testcase);
    }
    
    
    /**
     * Indicates the current suite has finished.
     */
    static void suiteFinished()
    {
        CppUTestTestHandler.firstSuite = true;
    }
}
