package com.github.radium226.common;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.io.InputStream;

public class Ok {

    public static InputStream download(OkHttpClient httpClient, String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
            .build();
        Response response = httpClient.newCall(request).execute();
        InputStream bodyInputStream = response.body().byteStream();
        return bodyInputStream;
    }

}
