package org.chromium.base.library_loader;
import org.chromium.base.annotations.SuppressFBWarnings;
@SuppressFBWarnings
public class NativeLibraries {
    public static final int CPU_FAMILY_UNKNOWN = 0;
    public static final int CPU_FAMILY_ARM = 1;
    public static final int CPU_FAMILY_MIPS = 2;
    public static final int CPU_FAMILY_X86 = 3;
    public static boolean sUseLinker = true;
    public static boolean sUseLibraryInZipFile = false;
    public static boolean sEnableLinkerTests = false;
    public static final String[] LIBRARIES =
      {"chrome"};
    static String sVersionNumber =
      "96.0.2924.87";
    public static int sCpuFamily =
            CPU_FAMILY_UNKNOWN;
}
