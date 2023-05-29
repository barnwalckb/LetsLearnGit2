package org.example;

import app.krista.extension.impl.anno.Extension;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.InvokerRequest;
import app.krista.extension.impl.anno.StaticResource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

@Extension(name="Billing",version="1.0")
@StaticResource(path = "docs", file = "docs")
@Field.Text(value ="UserName")
@Field.Text(value = "Password", isSecured = true)
@Field.Text(value = "Url")
public class BillingExtension {

    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateAttributes(@NotNull Map<String, Object> attributes) throws IOException, InterruptedException {

        String userName=attributes.get("UserName").toString()+":"+attributes.get("Password").toString();
        String authCred= Base64.getEncoder().encodeToString(userName.getBytes());
        String url=attributes.get("Url").toString();


        if (!Authenticate(url,authCred)) throw new IllegalArgumentException("Authentication failed !!!");
    }

    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection(){
    }

    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)public Map<String, String> customTabs() {
        return Map.of("Documentation", "static/docs");
    }

    public boolean Authenticate(String url, String authCred) throws IOException, InterruptedException {



        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .headers("accept", "application/json", "Authorization", "Basic " + authCred)
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status code from Authenticate method "+ response.statusCode());
        System.out.println(response.headers()+" <----Respon Headers");
        System.out.println("auth cred  :"+authCred);

        return response.statusCode() == 200;
    }


}
