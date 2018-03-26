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

import bv.offa.netbeans.cnd.unittest.api.CndTestSuite;
import bv.offa.netbeans.cnd.unittest.api.ManagerAdapter;
import bv.offa.netbeans.cnd.unittest.api.TestFramework;
import bv.offa.netbeans.cnd.unittest.testhelper.Helper;
import static bv.offa.netbeans.cnd.unittest.testhelper.Helper.checkedMatch;
import static bv.offa.netbeans.cnd.unittest.testhelper.TestMatcher.frameworkIs;
import static bv.offa.netbeans.cnd.unittest.testhelper.TestMatcher.hasError;
import static bv.offa.netbeans.cnd.unittest.testhelper.TestMatcher.hasNoError;
import static bv.offa.netbeans.cnd.unittest.testhelper.TestMatcher.matchesTestCase;
import static bv.offa.netbeans.cnd.unittest.testhelper.TestMatcher.matchesTestSuite;
import static bv.offa.netbeans.cnd.unittest.testhelper.TestMatcher.sessionIs;
import static bv.offa.netbeans.cnd.unittest.testhelper.TestMatcher.suiteFrameworkIs;
import static bv.offa.netbeans.cnd.unittest.testhelper.TestMatcher.timeIs;
import java.util.regex.Matcher;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InOrder;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.Report;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

@Tag("Test-Framework")
@Tag("LibCheck")
public class LibCheckTestFinishedHandlerTest
{
    private static final TestFramework FRAMEWORK = TestFramework.LibCheck;
    private static Project project;
    private static Report report;
    private LibCheckTestFinishedHandler handler;
    private TestSession session;
    private ManagerAdapter manager;

    @BeforeAll
    public static void setUpClass()
    {
        project = mock(Project.class);
        when(project.getProjectDirectory())
                .thenReturn(FileUtil.createMemoryFileSystem().getRoot());
        when(project.getLookup()).thenReturn(Lookup.EMPTY);
        report = new Report("suite", project);
    }

    @BeforeEach
    public void setUp()
    {
        handler = new LibCheckTestFinishedHandler();
        session = mock(TestSession.class);
        manager = mock(ManagerAdapter.class);
    }

    @Test
    public void matchesSuccessfulTest()
    {
        assertTrue(handler.matches("ok 1 - Test.c:TestSuite:testA: Passed"));
        assertTrue(handler.matches("ok 5 - Test.c:TestSuite:tesB: Passed"));
        assertTrue(handler.matches("ok 1234 - Test.c:TestSuite:testC: Passed"));
    }

    @Test
    public void parseDataSuccessfulTest()
    {
        Matcher m = checkedMatch(handler, "ok 1 - Test.c:TestSuite:testCase: Passed");
        assertEquals("ok", m.group(1));
        assertEquals("Test.c", m.group(2));
        assertEquals("TestSuite", m.group(3));
        assertEquals("testCase", m.group(4));
    }

    @Test
    public void matchesFailedTest()
    {
        assertTrue(handler.matches("not ok 2 - Test.c:TestSuite:testCase: "
                                    + "Assertion 'x == 1' failed: r == 3, 1 == 1"));
        assertTrue(handler.matches("not ok 470 - Test.c:TestSuite:testCase: "
                                    + "Assertion '\"abc\" == \"!abc\"' failed: "
                                    + "\"abc\" == \"abc\", \"!abc\" == \"!abc\""));
    }

    @Test
    public void parseDataFailedTest()
    {
        Matcher m = checkedMatch(handler, "not ok 2 - Test.c:TestSuite:testCase: "
                                        + "Assertion 'r == 1' failed: r == 3, 1 == 1");
        assertEquals("not ok", m.group(1));
        assertEquals("Test.c", m.group(2));
        assertEquals("TestSuite", m.group(3));
        assertEquals("testCase", m.group(4));
    }

    @Test
    public void updateUIStartsTestIfFirstTest()
    {
        checkedMatch(handler, "ok 1 - Test.c:TestSuite:testCase: Passed");
        handler.updateUI(manager, session);
        verify(manager).testStarted(session);
    }

    @Test
    public void updateUIStartsTestBeforeSuite()
    {
        checkedMatch(handler, "ok 1 - Test.c:TestSuite:testCase: Passed");
        handler.updateUI(manager, session);
        InOrder inOrder = inOrder(manager);
        inOrder.verify(manager).testStarted(any(TestSession.class));
        inOrder.verify(manager).displaySuiteRunning(any(TestSession.class), any(CndTestSuite.class));
    }

    @Test
    public void updateUIDisplaysReportIfNotFirstTest()
    {
        checkedMatch(handler, "ok 1 - Test.c:TestSuite:testCase: Passed");
        when(session.getReport(anyLong())).thenReturn(report);
        handler.updateUI(manager, session);
        handler.updateUI(manager, session);
        verify(manager).displayReport(session, report);
    }

    @Test
    public void updateUIStartsNewSuiteIfFirstSuite()
    {
        checkedMatch(handler, "ok 1 - Test.c:TestSuite:testCase: Passed");
        handler.updateUI(manager, session);
        verify(session).addSuite(argThat(allOf(matchesTestSuite("TestSuite"),
                                                suiteFrameworkIs(FRAMEWORK))));
        verify(manager).displaySuiteRunning(eq(session), argThat(allOf(matchesTestSuite("TestSuite"),
                                                                        suiteFrameworkIs(FRAMEWORK))));
    }

    @Test
    public void updateUIStartsNewSuiteIfNewSuiteStarted()
    {
        checkedMatch(handler, "ok 1 - Test.c:TestSuite:testCase: Passed");
        Helper.createCurrentTestSuite("TestSuit", FRAMEWORK, session);
        handler.updateUI(manager, session);
        verify(session).addSuite(argThat(allOf(matchesTestSuite("TestSuite"),
                                                suiteFrameworkIs(FRAMEWORK))));
        verify(manager).displaySuiteRunning(eq(session), argThat(allOf(matchesTestSuite("TestSuite"),
                                                                        suiteFrameworkIs(FRAMEWORK))));
    }

    @Test
    public void updateUIDoesNothingIfSameSuite()
    {
        checkedMatch(handler, "ok 1 - Test.c:TestSuite:testCase: Passed");
        CndTestSuite suite = new CndTestSuite("TestSuite", FRAMEWORK);
        when(session.getCurrentSuite()).thenReturn(suite);
        handler.updateUI(manager, session);

        verify(session, never()).addSuite(any(CndTestSuite.class));
        verify(manager, never()).displaySuiteRunning(any(TestSession.class), any(TestSuite.class));
    }

    @Test
    public void updateUIAddsTestCase()
    {
        checkedMatch(handler, "ok 1 - Test.c:TestSuite:testCase: Passed");
        handler.updateUI(manager, session);
        verify(session).addTestCase(argThat(matchesTestCase("testCase", "TestSuite")));
    }

    @Test
    public void updateUISetsTestCaseInformation()
    {
        checkedMatch(handler, "ok 1 - Test.c:TestSuite:testCase: Passed");
        handler.updateUI(manager, session);
        verify(session).addTestCase(argThat(allOf(matchesTestCase("testCase", "TestSuite"),
                                                    frameworkIs(FRAMEWORK),
                                                    sessionIs(session),
                                                    timeIs(0),
                                                    hasNoError())));
    }

    @Test
    public void updateUISetsErrorOnFailure()
    {
        checkedMatch(handler, "not ok 2 - Test.c:TestSuite:testCase: "
                            + "Assertion 'r == 1' failed: r == 3, 1 == 1");
        handler.updateUI(manager, session);
        verify(session).addTestCase(argThat(hasError()));
    }

}