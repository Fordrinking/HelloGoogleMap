package com.hill1942.hellogooglemap.SENetwork;

/**
 * Created by ykdac on 3/23/2017.
 */
 public class Result {
    public String mResultValue;
    public Exception mException;
    public Result(String resultValue) {
        mResultValue = resultValue;
    }
    public Result(Exception exception) {
        mException = exception;
    }
}
