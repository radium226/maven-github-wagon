/**
 *    Copyright 2015 Radium226
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.github.radium226.common;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.io.InputStream;

public final class Ok {

    public static InputStream download(OkHttpClient httpClient, String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
            .build();
        Response response = httpClient.newCall(request).execute();
        InputStream bodyInputStream = response.body().byteStream();
        return bodyInputStream;
    }

}
