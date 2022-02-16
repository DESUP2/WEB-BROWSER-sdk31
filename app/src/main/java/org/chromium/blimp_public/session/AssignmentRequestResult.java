// Decompiled by JEB v3.7.0.201909272058

package org.chromium.blimp_public.session;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AssignmentRequestResult {
    @Retention(RetentionPolicy.SOURCE)
    public @interface AssignmentRequestResultEnum {
    }

    public static final int BAD_REQUEST = 2;
    public static final int BAD_RESPONSE = 3;
    public static final int EXPIRED_ACCESS_TOKEN = 5;
    public static final int INVALID_CERT = 11;
    public static final int INVALID_PROTOCOL_VERSION = 4;
    public static final int NETWORK_FAILURE = 10;
    public static final int OK = 1;
    public static final int OUT_OF_VMS = 7;
    public static final int SERVER_ERROR = 8;
    public static final int SERVER_INTERRUPTED = 9;
    public static final int UNKNOWN = 0;
    public static final int USER_INVALID = 6;

}

