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

package bv.offa.netbeans.cnd.unittest;

import bv.offa.netbeans.cnd.unittest.api.CndTestCase;
import bv.offa.netbeans.cnd.unittest.api.CndTestSuite;
import bv.offa.netbeans.cnd.unittest.api.TestFramework;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

public class TestSupportUtilsTest
{
    private static final String testCaseName = "testCase";
    private static final String testSuiteName = "TestSuite";
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private static TestSession testSessionMock;
    private static Project projectMock;
    
    @BeforeClass
    public static void setUpClass()
    {
        testSessionMock = mock(TestSession.class);
        projectMock = mock(Project.class);
        when(projectMock.getProjectDirectory()).thenReturn(FileUtil.createMemoryFileSystem().getRoot());
        when(projectMock.getLookup()).thenReturn(Lookup.EMPTY);
    }
    
    
    
    @Test
    public void testParseTimeToMillis()
    {
        assertEquals(12345000L, parseMs("12345.0"));
        assertEquals(67890000L, parseMs("67890"));
        assertEquals(3000L, parseMs("3"));
        assertEquals(100L, parseMs("0.100471"));
        assertEquals(477100L, parseMs("477.100486"));
        assertEquals(101L, parseMs("0.1005"));
        assertEquals(12000L, parseMs("12.00"));
        assertEquals(10L, parseMs("0.01"));
        assertEquals(0L, parseMs("0.0"));
        assertEquals(0L, parseMs("2.646e-05"));
        assertEquals(4L, parseMs("427.4272e-05"));
        assertEquals(74L, parseMs("7.4272e-02"));
        assertEquals(320000L, parseMs("3.2e02"));
        assertEquals(0L, parseMs("0.000144571"));
        assertEquals(1L, parseMs("0.00144571"));
        assertEquals(234L, parseMs("0.234108"));
    }
    
    @Test
    public void testParseTimeToMillisWithInvalidInput()
    {
        assertEquals(0L, parseMs("-7.4272e-02"));
        assertEquals(0L, parseMs("-0.100471"));
        assertEquals(0L, parseMs("-477.100486"));
        assertEquals(0L, parseMs("7.4272b-02"));
        assertEquals(0L, parseMs("7.4272e"));
        assertEquals(0L, parseMs(""));
    }
    
    private long parseMs(String timeSec)
    {
        return TestSupportUtils.parseTimeSecToMillis(timeSec);
    }
    
    @Test
    public void testGetUniqueDeclaratonNameTestCaseCppUTest()
    {
        CndTestCase testCase = new CndTestCase(testCaseName, TestFramework.CPPUTEST, testSessionMock);
        testCase.setClassName(testSuiteName);
        
        final String expected = "C:TEST_" + testSuiteName + "_" + testCaseName + "_Test";
        assertEquals(expected, TestSupportUtils.getUniqueDeclaratonName(testCase));
    }
    
    @Test
    public void testGetUniqueDeclaratonNameTestCaseCppUTestIgnored()
    {
        CndTestCase testCase = new CndTestCase(testCaseName, TestFramework.CPPUTEST, testSessionMock);
        testCase.setClassName(testSuiteName);
        testCase.setStatus(Status.SKIPPED);
        
        final String expected = "C:IGNORE" + testSuiteName + "_" + testCaseName + "_Test";
        assertEquals(expected, TestSupportUtils.getUniqueDeclaratonName(testCase));
    }
    
    @Test
    public void testGetUniqueDeclaratonNameTestSuiteCppUTest()
    {
        CndTestSuite testSuite = new CndTestSuite(testSuiteName, TestFramework.CPPUTEST);
        
        final String expected = "S:TEST_GROUP_CppUTestGroup" + testSuiteName;
        assertEquals(expected, TestSupportUtils.getUniqueDeclaratonName(testSuite));
    }
    
    @Test
    public void testGetUniqueDeclaratonNameTestCaseGoogleTest()
    {
        CndTestCase testCase = new CndTestCase(testCaseName, TestFramework.GOOGLETEST, testSessionMock);
        testCase.setClassName(testSuiteName);
        
        final String expected = "C:" + testSuiteName + "_" + testCaseName + "_Test";
        assertEquals(expected, TestSupportUtils.getUniqueDeclaratonName(testCase));
    }
    
    @Test
    public void testGetUniqueDeclaratonNameTestSuiteGoogleTest()
    {
        CndTestSuite testSuite = new CndTestSuite(testSuiteName, TestFramework.GOOGLETEST);
        
        final String expected = "C:" + testSuiteName;
        assertEquals(expected, TestSupportUtils.getUniqueDeclaratonName(testSuite));
    }
    
    @Test
    public void testGetUniqueDeclaratonNameTestCaseLibunittestCpp()
    {
        CndTestCase testCase = new CndTestCase(testCaseName, TestFramework.LIBUNITTESTCPP, testSessionMock);
        testCase.setClassName(testSuiteName);
        
        final String expected = "S:" + testSuiteName + "::" + testCaseName;
        assertEquals(expected, TestSupportUtils.getUniqueDeclaratonName(testCase));
    }
    
    @Test
    public void testGetUniqueDeclaratonNameTestCaseLibunittestCppWithToken()
    {
        CndTestCase testCase = new CndTestCase(testCaseName + "::test", TestFramework.LIBUNITTESTCPP, testSessionMock);
        testCase.setClassName(testSuiteName);
        
        final String expected = "S:" + testSuiteName + "::" + testCaseName;
        assertEquals(expected, TestSupportUtils.getUniqueDeclaratonName(testCase));
    }
    
    @Test
    public void testGetUniqueDeclaratonNameTestSuiteLibunittestCpp()
    {
        CndTestSuite testSuite = new CndTestSuite(testSuiteName, TestFramework.LIBUNITTESTCPP);
        
        final String expected = "S:" + testSuiteName + "::__testcollection_child__";
        assertEquals(expected, TestSupportUtils.getUniqueDeclaratonName(testSuite));
    }
}
