package com.dnhsolution.restokabmalang.sistem;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateCetakBillingPojo {

    @SerializedName("success")
    @Expose
    private Integer success;
    @SerializedName("message")
    @Expose
    private String message;

    /**
     *
     * @return
     * The id
     */
    public Integer getSuccess() {
        return success;
    }

    /**
     *
     * @param success
     * The id
     */
    public void setSuccess(Integer success) {
        this.success = success;
    }

    /**
     *
     * @return
     * The username
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     * @param message
     * The Username
     */
    public void setMessage(String message) {
        this.message = message;
    }

}
