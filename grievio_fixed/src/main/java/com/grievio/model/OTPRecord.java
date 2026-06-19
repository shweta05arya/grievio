package com.grievio.model;

public class OTPRecord {
    private String email;
    private String otpCode;
    private String purpose;
    private boolean verified;

    public OTPRecord() {}
    public OTPRecord(String email, String otpCode, String purpose, boolean verified) {
        this.email = email; this.otpCode = otpCode; this.purpose = purpose; this.verified = verified;
    }
    public String getEmail()              { return email; }
    public void   setEmail(String v)      { this.email = v; }
    public String getOtpCode()            { return otpCode; }
    public void   setOtpCode(String v)    { this.otpCode = v; }
    public String getPurpose()            { return purpose; }
    public void   setPurpose(String v)    { this.purpose = v; }
    public boolean isVerified()           { return verified; }
    public void    setVerified(boolean v) { this.verified = v; }
}
