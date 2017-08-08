package com.kerk12.pilock;

/**
 * Checks the version number returned by the server and determines if the latest app version is being used.
 */

public class VersionChecker {
    private static String LatestServerVersion = "0.2.0";

    /**
     * Checks whether the app being run is the latest version for the server currently being used.
     * @param serverVer The server's version.
     * @return True if the latest app for this server software version is being used, false if the server or the app is outdated.
     */
    public static boolean CheckVersion(String serverVer){
        return LatestServerVersion.equals(serverVer);
    }
}
