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

package bv.offa.netbeans.cnd.unittest.cpputest;

import bv.offa.netbeans.cnd.unittest.api.CndTestCase;
import bv.offa.netbeans.cnd.unittest.api.TestFramework;
import bv.offa.netbeans.cnd.unittest.ui.TestRunnerUINodeFactory;
import java.util.regex.Matcher;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import static org.mockito.Mockito.*;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;

public class CppUTestErrorHandlerTest
{
    private static final TestSessionInformation DONT_CARE_INFO = new TestSessionInformation();
    private CppUTestErrorHandler handler;
    
    
    @Before
    public void setUp()
    {
        handler = new CppUTestErrorHandler(DONT_CARE_INFO);
    }
    
    @Test
    public void testMatchesErrorLocationLine()
    {
        final String line = "test/SuiteName.cpp:37: error: Failure in TEST(SuiteName, testName)";
        assertTrue(handler.matches(line));
    }
    
    @Test
    public void testParsesDataErrorLocationLine()
    {
        final String line = "test/SuiteName.cpp:37: error: Failure in TEST(SuiteName, testName)";
        Matcher m = handler.match(line);
        
        assertTrue(m.find());
        assertEquals(4, m.groupCount());
        assertEquals("test/SuiteName.cpp", m.group(1));
        assertEquals("37", m.group(2));
        assertEquals("SuiteName", m.group(3));
        assertEquals("testName", m.group(4));
    }
    
    @Test
    public void testUpdateUIDoesNothingIfNoTestCase()
    {
        final String input = "test/SuiteName.cpp:37: error: Failure in TEST(SuiteName, testName)";
        Matcher m = handler.match(input);
        assertTrue(m.find());
        
        TestRunnerUINodeFactory factory = new TestRunnerUINodeFactory();
        TestSession session = mock(TestSession.class);
        Testcase testCase = null;
        when(session.getCurrentTestCase()).thenReturn(testCase);
        when(session.getNodeFactory()).thenReturn(factory);
        
        handler.updateUI(null, session);
    }
    
    @Test
    public void testUpdateUIIgnoresNotMatchingTestCase()
    {
        final String input = "test/SuiteName.cpp:37: error: Failure in TEST(SuiteName, testName)";
        Matcher m = handler.match(input);
        assertTrue(m.find());

        TestRunnerUINodeFactory factory = new TestRunnerUINodeFactory();
        TestSession session = mock(TestSession.class);
        Testcase testCase = new CndTestCase("testCase", TestFramework.CPPUTEST, session);
        testCase.setClassName("SuiteName");
        when(session.getCurrentTestCase()).thenReturn(testCase);
        when(session.getNodeFactory()).thenReturn(factory);

        handler.updateUI(null, session);

        assertNull(testCase.getTrouble());
        assertNull(testCase.getLocation());
    }
    
    @Test
    public void testUpdateUIIgnoresNotMatchingTestSuite()
    {
        final String input = "test/SuiteName.cpp:37: error: Failure in TEST(SuiteName, testName)";
        Matcher m = handler.match(input);
        assertTrue(m.find());

        TestRunnerUINodeFactory factory = new TestRunnerUINodeFactory();
        TestSession session = mock(TestSession.class);
        Testcase testCase = new CndTestCase("testName", TestFramework.CPPUTEST, session);
        testCase.setClassName("WrongSuiteName");
        when(session.getCurrentTestCase()).thenReturn(testCase);
        when(session.getNodeFactory()).thenReturn(factory);

        handler.updateUI(null, session);

        assertNull(testCase.getTrouble());
        assertNull(testCase.getLocation());
    }
    
    @Test
    public void testUpdateUISetsTrouble()
    {
        final String input = "test/SuiteName.cpp:37: error: Failure in TEST(SuiteName, testName)";
        Matcher m = handler.match(input);
        assertTrue(m.find());

        TestRunnerUINodeFactory factory = new TestRunnerUINodeFactory();
        TestSession session = mock(TestSession.class);
        Testcase testCase = new CndTestCase("testName", TestFramework.CPPUTEST, session);
        testCase.setClassName("SuiteName");
        when(session.getCurrentTestCase()).thenReturn(testCase);
        when(session.getNodeFactory()).thenReturn(factory);

        handler.updateUI(null, session);
        Trouble t = testCase.getTrouble();
        
        assertNotNull(t);
        assertTrue(t.isError());
        assertEquals("test/SuiteName.cpp:37", t.getStackTrace()[0]);
    }
    
    @Test
    public void testUpdateUIUpdatesTrouble()
    {
        final String input = "test/SuiteName.cpp:37: error: Failure in TEST(SuiteName, testName)";
        Matcher m = handler.match(input);
        assertTrue(m.find());

        TestRunnerUINodeFactory factory = new TestRunnerUINodeFactory();
        TestSession session = mock(TestSession.class);
        Testcase testCase = new CndTestCase("testName", TestFramework.CPPUTEST, session);
        testCase.setClassName("SuiteName");
        testCase.setTrouble(new Trouble(false));
        when(session.getCurrentTestCase()).thenReturn(testCase);
        when(session.getNodeFactory()).thenReturn(factory);

        handler.updateUI(null, session);
        Trouble t = testCase.getTrouble();
        
        assertNotNull(t);
        assertTrue(t.isError());
        assertEquals("test/SuiteName.cpp:37", t.getStackTrace()[0]);
    }
    
}
