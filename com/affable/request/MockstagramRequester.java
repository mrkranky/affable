package com.affable.request;

import io.parallec.core.*;
import java.util.Map;

public class MockstagramRequester {

    // to be appended userID to this URL
    public static final String url = "http://localhost:3000/api/v1/influencers/1000000";

    /*public void requester() {

        try {
            AsyncHttpClient client = Dsl.asyncHttpClient();

            StringBuilder builder = new StringBuilder(url).append("1000000");

            BoundRequestBuilder getRequest = client.prepareGet(builder.toString());

            Future<Response> responseFuture = getRequest.execute();
            String k = responseFuture.get().getResponseBody();

            System.out.println(k);
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
        }
    }*/

    public void req() {

        ParallelClient pc = new ParallelClient();


        pc.prepareHttpPost("").setTargetHostsFromString(url)
                .execute(new ParallecResponseHandler() {
                    public void onCompleted(ResponseOnSingleTask res,
                                            Map<String, Object> responseContext) {
                        System.out.println( res.toString() );  }
                });

        /*int i=1000000;

        while (true) {
            ParallelClient pc = new ParallelClient();
            pc.prepareHttpGet(url + i)
                    .execute(new ParallecResponseHandler() {
                        public void onCompleted(ResponseOnSingleTask res,
                                                Map<String, Object> responseContext) {
                            System.out.println( res.toString() );  }
                    });
            i++;

            if (i > 1000010) {
                i=1000000;
            }
        }*/


    }

    public static void main(String args[]) {
        MockstagramRequester caller = new MockstagramRequester();

        caller.req();
    }
}
