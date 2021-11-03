package com.dnhsolution.restokabmalang.sistem.produk;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeletePojo {

    @SerializedName("success")
    @Expose
    private Integer id;
    @SerializedName("message")
    @Expose
    private String username;

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The username
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @param username
     * The Username
     */
    public void setUsername(String username) {
        this.username = username;
    }

}
