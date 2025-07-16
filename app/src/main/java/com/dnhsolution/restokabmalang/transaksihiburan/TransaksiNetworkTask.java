package com.dnhsolution.restokabmalang.transaksihiburan;

import android.content.Context;

import com.dnhsolution.restokabmalang.utilities.BaseTask;
import com.dnhsolution.restokabmalang.utilities.Connecting;
import com.dnhsolution.restokabmalang.utilities.OnDataFetched;

import java.util.HashMap;

public class TransaksiNetworkTask extends BaseTask {

    private final OnDataFetched listener;//listener in fragment that shows and hides ProgressBar
    private final Context context;
    private final String url;
    private final HashMap<String, String> params;

    public TransaksiNetworkTask(OnDataFetched onDataFetchedListener, Context context, String url, HashMap<String, String> params) {
        this.listener = onDataFetchedListener;
        this.context = context;
        this.url = url;
        this.params = params;
    }

    @Override
    public Object call() throws Exception {

        Object result = null;
        result = new Connecting().postConnection(url,params);//some network request for example
        return result;
    }

    @Override
    public void setUiForLoading() {
        listener.showProgressBar();
    }

    @Override
    public void setDataAfterLoading(Object result) {
        listener.setDataInPageWithResult(result);
        listener.hideProgressBar();
    }
}
