package org.openjava.upay;

public enum Version
{
    CURRENT(1, 0, 0),
    
    V1_1_0(1, 1, 0);
    
    private static final String CHAR_DOT = ".";
    
    private int major;
    private int minor;
    private int maintain;
    
    private Version(int major, int minor, int maintain)
    {
        this.major = major;
        this.minor = minor;
        this.maintain = maintain;
    }
    
    public boolean compatibleWith(Version version)
    {
        return compareWith(version) >= 0;
    }
    
    public int compareWith(Version version)
    {
        if (version == null) {
            return 1;
        }
        
        int result = major - version.major;
        if (result == 0) {
            result = minor - version.minor;
            if (result == 0) {
                result = maintain - version.maintain;
            }
        }
        
        return result;
    }
    
    public int getMajor()
    {
        return major;
    }

    public void setMajor(int major)
    {
        this.major = major;
    }

    public int getMinor()
    {
        return minor;
    }

    public void setMinor(int minor)
    {
        this.minor = minor;
    }

    public int getMaintain()
    {
        return maintain;
    }

    public void setMaintain(int maintain)
    {
        this.maintain = maintain;
    }

    public String toVersion()
    {
        return toString();
    }
    
    public String toString()
    {
        return major + CHAR_DOT + minor + CHAR_DOT + maintain;
    }
    
    public static Version fromVersion(String version)
    {
        try {
            String[] versions = version.split("\\" + CHAR_DOT);
            if (versions.length < 3) {
                throw new IllegalArgumentException("Invalid version string format: " + version + ", but 1.0.1 expected");
            }
            Version[] values = values();
            for (Version value : values) {
                if (Integer.parseInt(versions[0]) == value.major && Integer.parseInt(versions[1]) == value.minor
                    &&  Integer.parseInt(versions[2]) == value.maintain) {
                    return value;
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid version string format: " + version + ", but 1.0.1 expected", ex);
        }
        throw new IllegalArgumentException("Version: " + version + " not exists");
    }
    
    public static Version currentVersion()
    {
        return Version.CURRENT;
    }
}
