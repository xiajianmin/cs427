package org.jenkinsci.plugins.tools_autodiscover.modules;


import hudson.model.JDK;
import hudson.tools.ToolInstallation;
import org.jenkinsci.plugins.tools_autodiscover.ToolAutoDiscoveryModule;
import org.jenkinsci.plugins.tools_autodiscover.util.Environment;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JDK discovery module for Mac OS X compatible systems
 */
public class JDKDiscoveryOSX extends ToolAutoDiscoveryModule {
    public JDKDiscoveryOSX() {
        super(new Environment());
    }

    @Override
    public boolean isApplicable(Class<? extends ToolInstallation> toolType) {
        return (toolType == JDK.class) && env.isMacOSX();
    }

    @Override
    public List<ToolInstallation> getToolInstallations(Class<? extends ToolInstallation> toolType) {
        if (!isApplicable(toolType)) {
            return Collections.emptyList();
        }

        List<JVMInfo> jvmList = getLocalJVMs();
        List<ToolInstallation> instances = new ArrayList<ToolInstallation>();
        for (JVMInfo jvm : jvmList) {
            if (jvm.isJDK()) {
                instances.add(new JDK(jvm.getBundleId(), jvm.getHomePath()));
            }
        }
        return Collections.unmodifiableList(instances);
    }

    /**
     * Returns the list of locally available JVMs. Must be running under Mac OS X.
     *
     * @return List of JVMInfo objects containing information of each JVM.
     */
    protected List<JVMInfo> getLocalJVMs() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("/usr/libexec/java_home -X");
        } catch (IOException ignored) {
        } catch (SecurityException ignored) {
        }

        if (process == null) {
            return Collections.emptyList();
        }

        // Process is running. Parse the output and return the list.
        try {
            JavaHomeParser parser = new JavaHomeParser(process.getInputStream());
            return parser.getJVMInfoList();
        } catch (XMLStreamException ignored) {
            return Collections.emptyList();
        }
    }

    /**
     * Data class representing JVM instances. Uses Builder Pattern.
     */
    public static class JVMInfo {
        public static final String JVM_ENABLED = "JVMEnabled";
        public static final String JVM_BLACKLISTED = "JVMBlacklisted";
        public static final String JVM_IS_BUILTIN = "JVMIsBuiltIn";
        public static final String JVM_VERSION = "JVMVersion";
        public static final String JVM_VENDOR = "JVMVendor";
        public static final String JVM_PLATFORM_VERSION = "JVMPlatformVersion";
        public static final String JVM_NAME = "JVMName";
        public static final String JVM_HOME_PATH = "JVMHomePath";
        public static final String JVM_BUNDLE_ID = "JVMBundleID";
        public static final String JVM_ARCH = "JVMArch";

        private final String name;
        private final String bundleId;
        private final String homePath;
        private final String arch;
        private final String version;
        private final String vendor;
        private final String platformVersion;
        private final boolean blacklisted;
        private final boolean enabled;
        private final boolean builtIn;

        private JVMInfo(Builder builder) {
            this.name = builder.name;
            this.bundleId = builder.bundleId;
            this.homePath = builder.homePath;
            this.arch = builder.arch;
            this.version = builder.version;
            this.vendor = builder.vendor;
            this.platformVersion = builder.platformVersion;
            this.blacklisted = builder.blacklisted;
            this.enabled = builder.enabled;
            this.builtIn = builder.builtIn;
        }

        /**
         * Get human-readable name of the JVM instance.
         *
         * @return String for human-readable name of JVM
         */
        public String getName() {
            return name;
        }

        /**
         * Get OS X bundle identifier for the JVM instance.
         *
         * @return String for bundle identifier
         */
        public String getBundleId() {
            return bundleId;
        }

        /**
         * Get JAVA_HOME path for the JVM instance.
         *
         * @return String for JAVA_HOME path
         */
        public String getHomePath() {
            return homePath;
        }

        /**
         * Get the name of architecture supported by JVM instance.
         *
         * @return String for architecture
         */
        public String getArchitecture() {
            return arch;
        }

        /**
         * Get the version of JVM instance.
         *
         * @return String for version
         */
        public String getVersion() {
            return version;
        }

        /**
         * Get the vendor of JVM instance. This is usually 'Apple Inc.' for Apple-provided
         * JVMs or 'Oracle Corporation' for Oracle-provided ones.
         *
         * @return String for JVM vendor
         */
        public String getVendor() {
            return vendor;
        }

        /**
         * Get the platform version of JVM instance.
         * <p/>
         * Platform version is tied to updates
         *
         * @return String for version
         */
        public String getPlatformVersion() {
            return platformVersion;
        }

        /**
         * Returns whether the JVM is blacklisted in the system.
         *
         * @return true if blacklisted; false otherwise
         */
        public boolean isBlacklisted() {
            return blacklisted;
        }

        /**
         * Returns whether the JVM is enabled in the system.
         *
         * @return true if enabled; false otherwise
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Returns whether the JVM is shipped with the operating system.
         *
         * @return true if shipped with OS; false otherwise
         */
        public boolean isBuiltIn() {
            return builtIn;
        }

        /**
         * Tests whether the described JVM is JDK.
         *
         * @return true if JVMInfo describes a JDK; false otherwise
         */
        public boolean isJDK() {
            return bundleId.toLowerCase().contains("jdk");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            JVMInfo jvmInfo = (JVMInfo) o;

            if (blacklisted != jvmInfo.blacklisted) return false;
            if (builtIn != jvmInfo.builtIn) return false;
            if (enabled != jvmInfo.enabled) return false;
            if (arch != null ? !arch.equals(jvmInfo.arch) : jvmInfo.arch != null) return false;
            if (bundleId != null ? !bundleId.equals(jvmInfo.bundleId) : jvmInfo.bundleId != null) return false;
            if (!homePath.equals(jvmInfo.homePath)) return false;
            if (name != null ? !name.equals(jvmInfo.name) : jvmInfo.name != null) return false;
            if (platformVersion != null ? !platformVersion.equals(jvmInfo.platformVersion) : jvmInfo.platformVersion != null)
                return false;
            if (vendor != null ? !vendor.equals(jvmInfo.vendor) : jvmInfo.vendor != null) return false;
            if (version != null ? !version.equals(jvmInfo.version) : jvmInfo.version != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (bundleId != null ? bundleId.hashCode() : 0);
            result = 31 * result + homePath.hashCode();
            result = 31 * result + (arch != null ? arch.hashCode() : 0);
            result = 31 * result + (version != null ? version.hashCode() : 0);
            result = 31 * result + (vendor != null ? vendor.hashCode() : 0);
            result = 31 * result + (platformVersion != null ? platformVersion.hashCode() : 0);
            result = 31 * result + (blacklisted ? 1 : 0);
            result = 31 * result + (enabled ? 1 : 0);
            result = 31 * result + (builtIn ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("JVMInfo{");
            sb.append(JVM_NAME).append(":'").append(name).append("',");
            sb.append(JVM_BUNDLE_ID).append(":'").append(bundleId).append("',");
            sb.append(JVM_HOME_PATH).append(":'").append(homePath).append("',");
            sb.append(JVM_ARCH).append(":'").append(arch).append("',");
            sb.append(JVM_VERSION).append(":'").append(version).append("',");
            sb.append(JVM_VENDOR).append(":'").append(vendor).append("',");
            sb.append(JVM_PLATFORM_VERSION).append(":'").append(platformVersion).append("',");
            sb.append(JVM_BLACKLISTED).append(":").append(blacklisted).append(',');
            sb.append(JVM_ENABLED).append(":").append(enabled).append(',');
            sb.append(JVM_IS_BUILTIN).append(":").append(builtIn);
            sb.append('}');
            return sb.toString();
        }

        /**
         * Builder class for JVMInfo.
         */
        public static class Builder {
            private String name;
            private String bundleId;
            private String homePath;
            private String arch;
            private String version;
            private String vendor;
            private String platformVersion;
            private boolean blacklisted;
            private boolean enabled;
            private boolean builtIn;

            /**
             * Construct an empty-initialized builder object.
             */
            public Builder() {
                clear();
            }

            /**
             * Update JVMInfo builder field with String value.
             *
             * @param key   Key in the property list referring to the field
             * @param value String value to be updated
             * @throws IllegalArgumentException the key is unknown, or the type of associated value is incompatible with
             *                                  String.
             */
            public void update(String key, String value) {
                if (JVM_ARCH.equals(key)) {
                    setArchitecture(value);
                } else if (JVM_BUNDLE_ID.equals(key)) {
                    setBundleId(value);
                } else if (JVM_HOME_PATH.equals(key)) {
                    setHomePath(value);
                } else if (JVM_NAME.equals(key)) {
                    setName(value);
                } else if (JVM_PLATFORM_VERSION.equals(key)) {
                    setPlatformVersion(value);
                } else if (JVM_VENDOR.equals(key)) {
                    setVendor(value);
                } else if (JVM_VERSION.equals(key)) {
                    setVersion(value);
                } else {
                    throw new IllegalArgumentException("Unknown key, or key not applicable to String value");
                }
            }

            /**
             * Update JVMInfo builder field with boolean value.
             *
             * @param key   Key in the property list referring to the field
             * @param value boolean value to be updated
             * @throws IllegalArgumentException the key is unknown, or the type of associated value is incompatible with
             *                                  boolean.
             */
            public void update(String key, boolean value) {
                if (JVM_ENABLED.equals(key)) {
                    setEnabled(value);
                } else if (JVM_BLACKLISTED.equals(key)) {
                    setBlacklisted(value);
                } else if (JVM_IS_BUILTIN.equals(key)) {
                    setBuiltIn(value);
                } else {
                    throw new IllegalArgumentException("Unknown key, or key not applicable to boolean value");
                }
            }

            /**
             * Clears the builder to the initial state.
             */
            public void clear() {
                name = null;
                bundleId = null;
                homePath = null;
                arch = null;
                version = null;
                vendor = null;
                platformVersion = null;
                blacklisted = false;
                enabled = false;
                builtIn = false;
            }

            /**
             * Set name of JVM to be built.
             *
             * @param name name of JVM
             */
            public void setName(String name) {
                this.name = name;
            }

            /**
             * Set bundle identifier of JVM to be built.
             *
             * @param bundleId bundle identifier of JVM
             */
            public void setBundleId(String bundleId) {
                this.bundleId = bundleId;
            }

            /**
             * Set path of JVM to be built.
             *
             * @param homePath JAVA_HOME path of JVM
             */
            public void setHomePath(String homePath) {
                this.homePath = homePath;
            }

            /**
             * Set architecture of JVM to be built.
             *
             * @param arch architecture of JVM
             */
            public void setArchitecture(String arch) {
                this.arch = arch;
            }

            /**
             * Set version of JVM to be built.
             *
             * @param version version of JVM
             */
            public void setVersion(String version) {
                this.version = version;
            }

            /**
             * Set vendor of JVM to be built.
             *
             * @param vendor vendor of JVM
             */
            public void setVendor(String vendor) {
                this.vendor = vendor;
            }

            /**
             * Set platform version of JVM to be built.
             *
             * @param platformVersion platform version of JVM
             */
            public void setPlatformVersion(String platformVersion) {
                this.platformVersion = platformVersion;
            }

            /**
             * Set the flag whether the JVM is blacklisted.
             *
             * @param blacklisted true to flag JVM as blacklisted, false otherwise
             */
            public void setBlacklisted(boolean blacklisted) {
                this.blacklisted = blacklisted;
            }

            /**
             * Set the flag whether the JVM is enabled.
             *
             * @param enabled true to flag JVM as enabled, false otherwise
             */
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            /**
             * Set the flag whether the JVM is built-in.
             *
             * @param builtIn true to flag JVM as built-in, false otherwise
             */
            public void setBuiltIn(boolean builtIn) {
                this.builtIn = builtIn;
            }

            /**
             * Build the JVMInfo object using the internal state.
             *
             * @return JVMInfo instance built from the internal state
             */
            public JVMInfo build() {
                return new JVMInfo(this);
            }
        }
    }

    /**
     * Parser for Mac OS X /usr/libexec/java_home utility.
     */
    public static class JavaHomeParser {
        public static final String PLIST_FALSE = "false";
        public static final String PLIST_TRUE = "true";
        public static final String PLIST_STRING = "string";
        public static final String PLIST_KEY = "key";
        public static final String PLIST_DICT = "dict";

        private List<JVMInfo> jvmList = new ArrayList<JVMInfo>();

        /**
         * Build the instance by parsing an java.io.InputStream.
         * <p/>
         * InputStream must be formatted according to proper XML PLIST format used by /usr/libexec/java_home.
         *
         * @param inputStream java.io.InputStream to be parsed
         * @throws XMLStreamException inputStream is not in valid XML PLIST format, or parser has failed for other
         *                            reasons.
         */
        public JavaHomeParser(InputStream inputStream) throws XMLStreamException {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader reader = factory.createXMLEventReader(inputStream);

            JVMInfo.Builder jiBuilder = new JVMInfo.Builder();
            String key = null;

            // Apple plist xml format has following structure:
            // <?xml ...>
            // <!DOCTYPE ...>
            // <plist ...>
            //   <array>
            //     <dict>
            //       <key>
            //       VALUE (<string>, <true/>, <false/>, ...)
            //       ...
            //       <key>
            //       VALUE
            //     </dict>
            //     <dict>
            //       ...
            //     </dict>
            //   </array>
            // </plist>
            // Each <dict> element represents a JVM entry.
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {
                    StartElement element = event.asStartElement();
                    String elementName = element.getName().getLocalPart();
                    if (PLIST_DICT.equals(elementName)) {
                        // Start of a single JVM entry
                        jiBuilder.clear();
                    } else if (PLIST_KEY.equals(elementName)) {
                        // Remember the key, so it can be used when value is read
                        event = reader.nextEvent();
                        key = event.asCharacters().getData();
                    } else if (PLIST_STRING.equals(elementName)) {
                        event = reader.nextEvent();
                        String value = event.asCharacters().getData();
                        jiBuilder.update(key, value);
                    } else if (PLIST_TRUE.equals(elementName)) {
                        jiBuilder.update(key, true);
                    } else if (PLIST_FALSE.equals(elementName)) {
                        jiBuilder.update(key, false);
                    }
                } else if (event.isEndElement()) {
                    EndElement element = event.asEndElement();
                    String elementName = element.getName().getLocalPart();
                    if (PLIST_DICT.equals(elementName)) {
                        // End of single entry.
                        jvmList.add(jiBuilder.build());
                    }
                }
            }
        }

        /**
         * Get the list of JVM instances parsed from the input stream.
         *
         * @return List containing metadata for all found JVM instances
         */
        public List<JVMInfo> getJVMInfoList() {
            return Collections.unmodifiableList(jvmList);
        }
    }
}
