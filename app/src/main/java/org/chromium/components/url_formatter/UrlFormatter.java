package org.chromium.components.url_formatter;

import android.text.TextUtils;
import java.net.URI;
import org.chromium.base.annotations.JNINamespace;

@JNINamespace("url_formatter::android")
public final class UrlFormatter {
    public static String fixupUrl(String uri) {
        String v0 = TextUtils.isEmpty(uri) ? null : UrlFormatter.nativeFixupUrl(uri);
        return v0;
    }

    public static String formatUrlForDisplay(String uri) {
        return UrlFormatter.nativeFormatUrlForDisplay(uri);
    }

    public static String formatUrlForDisplay(URI uri) {
        return UrlFormatter.formatUrlForDisplay(uri.toString());
    }

    public static String formatUrlForSecurityDisplay(String uri, boolean showScheme) {
        if(showScheme) {
            return UrlFormatter.nativeFormatUrlForSecurityDisplay(uri);
        }

        return UrlFormatter.nativeFormatUrlForSecurityDisplayOmitScheme(uri);
    }

    public static String formatUrlForSecurityDisplay(URI uri, boolean showScheme) {
        return UrlFormatter.formatUrlForSecurityDisplay(uri.toString(), showScheme);
    }

    private static native String nativeFixupUrl(String arg0);
    //{    }

    private static native String nativeFormatUrlForDisplay(String arg0);
    //{    }

    private static native String nativeFormatUrlForSecurityDisplay(String arg0);
    //{    }

    private static native String nativeFormatUrlForSecurityDisplayOmitScheme(String arg0);
    //{    }
}