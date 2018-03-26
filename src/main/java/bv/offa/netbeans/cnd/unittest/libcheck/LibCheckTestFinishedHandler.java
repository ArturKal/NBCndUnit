/*
 * NBCndUnit - C/C++ unit tests for NetBeans.
 * Copyright (C) 2015-2018  offa
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

package bv.offa.netbeans.cnd.unittest.libcheck;

import bv.offa.netbeans.cnd.unittest.api.CndTestCase;
import bv.offa.netbeans.cnd.unittest.api.CndTestHandler;
import bv.offa.netbeans.cnd.unittest.api.ManagerAdapter;
import bv.offa.netbeans.cnd.unittest.api.TestFramework;
import org.netbeans.modules.gsf.testrunner.api.TestSession;

/**
 * The class {@code LibCheckTestFinishedHandler} handles the test
 * output.
 *
 * @author offa
 */
public class LibCheckTestFinishedHandler extends CndTestHandler
{
    private static final int GROUP_RESULT = 1;
    private static final int GROUP_SUITE = 3;
    private static final int GROUP_CASE = 4;
    private static boolean firstSuite;

    public LibCheckTestFinishedHandler()
    {
        super(TestFramework.LibCheck, "^((?:not )??ok) [0-9]+? - (.+?):(.+?):(.+?): .*?$");
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

        if( isSameTestSuite(currentSuite(session), suiteName) == false )
        {
            updateSessionState(manager, session);
            startNewTestSuite(suiteName, session, manager);
        }

        final String testName = getMatchGroup(GROUP_CASE);
        CndTestCase testCase = startNewTestCase(testName, suiteName, session);
        updateResult(testCase);
    }


    /**
     * Updates the session state.
     *
     * @param manager   Manager
     * @param session   Session
     */
    private void updateSessionState(ManagerAdapter manager, TestSession session)
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
    }

    
    /**
     * Indicates the current suite has finished.
     */
    static void suiteFinished()
    {
        LibCheckTestFinishedHandler.firstSuite = true;
    }


    /**
     * Updates the test result.
     *
     * @param testCase      Test Case
     * @param location      Test location
     */
    private void updateResult(CndTestCase testCase)
    {
        final String result = getMatchGroup(GROUP_RESULT);

        if( result.equals("not ok") == true )
        {
            testCase.setError();
        }
    }

}
