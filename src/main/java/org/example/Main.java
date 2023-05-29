package org.example;

import org.json.JSONException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws JSONException, IOException, InterruptedException {
        System.out.println("Hello world!");
        BillingS4Hana billingS4Hana = new BillingS4Hana();
        //System.out.println(billingS4Hana.getBillingInfo("0080000686","TlRQTERFTU86TmVvQDEyMw=="));
       System.out.println(billingS4Hana.postBillingInformation("0080000686","TlRQTERFTU86TmVvQDEyMw=="));
        System.out.println("j");

    }
}