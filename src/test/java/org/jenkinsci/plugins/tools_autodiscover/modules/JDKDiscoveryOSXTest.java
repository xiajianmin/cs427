package org.jenkinsci.plugins.tools_autodiscover.modules;

import org.jenkinsci.plugins.tools_autodiscover.util.Environment;
import org.junit.Assume;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.jenkinsci.plugins.tools_autodiscover.modules.JDKDiscoveryOSX.JavaHomeParser;
import static org.junit.Assert.*;

public class JDKDiscoveryOSXTest {

    public static final String TEST_OUTPUT = "/org/jenkinsci/plugins/tools_autodiscover/modules/JDKDiscoveryOSXTest/java_home.out";

    private List<JDKDiscoveryOSX.JVMInfo> getExpectedJVMList() {
        List<JDKDiscoveryOSX.JVMInfo> expectedList = new ArrayList<JDKDiscoveryOSX.JVMInfo>();

        JDKDiscoveryOSX.JVMInfo.Builder builder = new JDKDiscoveryOSX.JVMInfo.Builder();

        builder.setEnabled(true);
        builder.setBlacklisted(false);
        builder.setBuiltIn(false);
        builder.setName("Java SE 7");
        builder.setVendor("Oracle Corporation");
        builder.setVersion("1.7.0_45");
        builder.setPlatformVersion("1.7");
        builder.setHomePath("/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home");
        builder.setArchitecture("x86_64");
        builder.setBundleId("com.oracle.java.7u45.jdk");

        expectedList.add(builder.build());

        builder.setVersion("1.7.0_11");
        builder.setHomePath("/Library/Java/JavaVirtualMachines/jdk1.7.0_11.jdk/Contents/Home");
        builder.setBundleId("com.oracle.java.7u11.jdk");

        expectedList.add(builder.build());

        builder.setBuiltIn(true);
        builder.setName("Java SE 6");
        builder.setVendor("Apple Inc.");
        builder.setVersion("1.6.0_65-b14-462");
        builder.setPlatformVersion("1.6");
        builder.setHomePath("/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home");
        builder.setBundleId("com.apple.javajdk16");

        expectedList.add(builder.build());

        builder.setArchitecture("i386");
        expectedList.add(builder.build());

        return expectedList;
    }

    @Test
    public void testJavaHomeParser() throws Exception {
        JavaHomeParser parser = new JavaHomeParser(getClass()
                .getResourceAsStream(TEST_OUTPUT));
        List<JDKDiscoveryOSX.JVMInfo> jvmList = parser.getJVMInfoList();

        assertEquals(4, jvmList.size());
        assertEquals(getExpectedJVMList(), jvmList);
    }

    @Test(expected = XMLStreamException.class)
    public void testJavaHomeParserMalformed() throws Exception {
        JavaHomeParser parser = new JavaHomeParser(new ByteArrayInputStream(
                "fubar".getBytes()));
        List<JDKDiscoveryOSX.JVMInfo> jvmList = parser.getJVMInfoList();
        assertTrue(jvmList.isEmpty());
    }

    @Test
    public void testJVMInfoIsJDK() {
        JDKDiscoveryOSX.JVMInfo.Builder builder = new JDKDiscoveryOSX.JVMInfo.Builder();
        // JDK test is performed by bundle identifiers
        builder.setBundleId("com.oracle.java.7u??");
        assertFalse(builder.build().isJDK());
        builder.setBundleId("com.oracle.java.7u??.jdk");
        assertTrue(builder.build().isJDK());
    }

    @Test
    public void testGetLocalJVMs() {
        // Only enable this test under OS X environment
        Assume.assumeTrue(new Environment().isMacOSX());

        JDKDiscoveryOSX discoveryModule = new JDKDiscoveryOSX();
        List<JDKDiscoveryOSX.JVMInfo> jvmList = discoveryModule.getLocalJVMs();
        // OS X should have JVM available if this test is running (JUnit!)
        assertFalse(jvmList.isEmpty());
    }

    @Test
    public void testHashCode() {
        JDKDiscoveryOSX.JVMInfo.Builder builder = new JDKDiscoveryOSX.JVMInfo.Builder();
        builder.setName(null);
        builder.setBundleId(null);
        builder.setHomePath("");
        builder.setArchitecture(null);
        builder.setVersion(null);
        builder.setVendor(null);
        builder.setPlatformVersion(null);
        builder.setBlacklisted(false);
        builder.setEnabled(false);
        builder.setBuiltIn(false);
        JDKDiscoveryOSX.JVMInfo info = builder.build();
        assertEquals(0, info.hashCode());

        builder.setEnabled(true);
        builder.setBlacklisted(false);
        builder.setBuiltIn(false);
        builder.setName("Java SE 7");
        builder.setVendor("Oracle Corporation");
        builder.setVersion("1.7.0_45");
        builder.setPlatformVersion("1.7");
        builder.setHomePath("/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home");
        builder.setArchitecture("x86_64");
        builder.setBundleId("com.oracle.java.7u45.jdk");
        info = builder.build();
        assertNotEquals(0, info.hashCode());
    }

    @Test
    public void testGetSetFunction() {
        JDKDiscoveryOSX.JVMInfo.Builder builder = new JDKDiscoveryOSX.JVMInfo.Builder();
        builder.setEnabled(true);
        builder.setBlacklisted(false);
        builder.setBuiltIn(false);
        builder.setName("Java SE 7");
        builder.setVendor("Oracle Corporation");
        builder.setVersion("1.7.0_45");
        builder.setPlatformVersion("1.7");
        builder.setHomePath("/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home");
        builder.setArchitecture("x86_64");
        builder.setBundleId("com.oracle.java.7u45.jdk");

        JDKDiscoveryOSX.JVMInfo info = builder.build();
        assertEquals("x86_64", info.getArchitecture());
        assertEquals("Java SE 7", info.getName());
        assertEquals("Oracle Corporation", info.getVendor());
        assertEquals("1.7.0_45", info.getVersion());
        assertEquals("1.7", info.getPlatformVersion());
        assertEquals(
                "/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home",
                info.getHomePath());
        assertEquals("x86_64", info.getArchitecture());
        assertEquals("com.oracle.java.7u45.jdk", info.getBundleId());
    }

    @Test
    public void testToString() {
        JDKDiscoveryOSX.JVMInfo.Builder builder = new JDKDiscoveryOSX.JVMInfo.Builder();
        builder.setName("Hello");
        builder.setBundleId("World");
        builder.setHomePath("Nice");
        builder.setArchitecture("to");
        builder.setVersion("meet");
        builder.setVendor("you");
        builder.setPlatformVersion("bye");
        builder.setBlacklisted(false);
        builder.setEnabled(false);
        builder.setBuiltIn(false);
        JDKDiscoveryOSX.JVMInfo info = builder.build();
        String expected_result = "JVMInfo{JVMName:'Hello',JVMBundleID:'World',JVMHomePath:'Nice',"
                + "JVMArch:'to',JVMVersion:'meet',JVMVendor:'you',JVMPlatformVersion:'bye',"
                + "JVMBlacklisted:false,JVMEnabled:false,JVMIsBuiltIn:false}";
        assertEquals(expected_result, info.toString());

    }
}
