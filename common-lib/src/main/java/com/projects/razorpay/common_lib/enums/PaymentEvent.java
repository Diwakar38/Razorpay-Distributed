package com.projects.razorpay.common_lib.enums;

public enum PaymentEvent {
    AUTHORIZE_ATTEMPT,
    AUTHORIZE_SUCCESS,
    CAPTURE_SUCCESS,
    CAPTURE_FAILURE,
    CAPTURE_TIMEOUT,
    AUTHORIZE_FAIL,
    CAPTURE_REQUEST,
    CAPTURE_FAIL,
    REFUND_INIT,
    REFUND_COMPLETE,
    CANCEL,
    SETTLE
}
