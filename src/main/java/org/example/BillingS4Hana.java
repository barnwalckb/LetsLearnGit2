package org.example;


import app.krista.extension.impl.anno.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Domain(id = "catEntryDomain_9e555941-54a9-426f-8b25-6e795b4b92c9",
        name = "Chandan",
        ecosystemId = "catEntryEcosystem_a88d397e-d230-4ac6-9299-c961555bde7d",
        ecosystemName = "MetagraphTraining",
        ecosystemVersion ="1b6a24e2-a67b-4459-abeb-aeb551085995")
@Entity(name = "Billing S4 Hana", id = "localDomainEntity_9c309317-c741-48ae-aa1b-b40a4fa467f4", primaryKey = "Delivery Number", options = {})
public class BillingS4Hana {

    @Field.Text(name = "Delivery Number", required = true, attributes = {}, options = {})
    public String deliveryNumber;

    @Field.Text(name = "Item Number", required = false, attributes = {}, options = {})
    public String itemNumber;

    @Field.Text(name = "Material Number", required = false, attributes = {}, options = {})
    public String materialNumber;

    @Field.Text(name = "Customer Name", required = false, attributes = {}, options = {})
    public String customerName;

    @Field(name = "Open Quantity", type = "Number",required = false, attributes = {}, options = {})
    public Double openQuantity;

    public List<BillingS4Hana> getBillingInfo(String deliveryNumber, String authCred) throws IOException, InterruptedException, JSONException
    {
        String GET_API_URL = "https://linux-aohx.neovatic.com:8101/sap/opu/odata/sap/ZBILLING_QTYCHK_SRV/HEADER_INSet?$format=json&$filter=DELIVERY%20eq%20'{{Delivery Number}}'";

        GET_API_URL = GET_API_URL.replace("{{Delivery Number}}",deliveryNumber);

        System.out.println(GET_API_URL);

        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .headers("accept","application/json","Authorization","Basic " + authCred)
                .uri(URI.create(GET_API_URL))
                .build();
        java.net.http.HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());

        List<BillingS4Hana> billingS4HanaList = new ArrayList<>();
        if(response.statusCode() != 200){
            System.out.println("*** Get Request Failed Something wrong! ***");
            return billingS4HanaList;
        }

        String body = response.body().toString();

        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonObject = objectMapper.readTree(body);

            for(JsonNode jsonNode : jsonObject.get("d").get("results")){

                BillingS4Hana billingS4Hana = new BillingS4Hana();
                billingS4Hana.setDeliveryNumber(jsonNode.get("DELIVERY").asText());
                billingS4Hana.setItemNumber(jsonNode.get("ITEM").asText());
                billingS4Hana.setMaterialNumber(jsonNode.get("MATNR").asText());
                billingS4Hana.setCustomerName(jsonNode.get("NAME1").asText());
                billingS4Hana.setOpenQuantity(Double.valueOf(jsonNode.get("OPEN_QTY").asText()));

                billingS4HanaList.add(billingS4Hana);
            }
        }catch(JsonProcessingException e){
            throw new RuntimeException(e);
    }
        return billingS4HanaList;
    }

    public String postBillingInformation(String deliveryNumber,String authCreds) throws IOException, InterruptedException, JSONException{

        String POST_API_URL = "https://linux-aohx.neovatic.com:8101/sap/opu/odata/sap/ZBILLING_CREATE_SRV/HEADER_INSet";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getCsrf = HttpRequest.newBuilder().GET()
                .header("x-csrf-token","fetch")
                .header("content-type","application/json")
                .header("Accept","application/json")
                .header("Authorization","Basic "+ authCreds)
                .uri(URI.create(POST_API_URL)).build();

        HttpResponse<String> csrfResponse = client.send(getCsrf,HttpResponse.BodyHandlers.ofString());
        System.out.println(csrfResponse);

        String token;
        token = csrfResponse.headers().map().get("x-csrf-token").toString();
        System.out.println(token);
        token = token.substring(1, token.length() - 1);
        String Cookie = csrfResponse.headers().map().get("set-cookie").toString();
        System.out.println(token);
        Cookie = Cookie.substring(Cookie.indexOf("SAP_SESSIONID_TST_110"),
                Cookie.lastIndexOf(";") + 1);
        System.out.println(Cookie);

        JSONObject jsonObject = new JSONObject();

        try{
            jsonObject.put("DELIVERY",deliveryNumber);

            JSONArray jsonArray = new JSONArray();
            jsonObject.put("HeadToMSG",jsonArray);
        }catch(JSONException e){
            throw new RuntimeException(e);
        }
        HttpRequest postBody = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                .header("x-csrf-token", token)
                .header("Authorization", "Basic " +  authCreds) //"TlRQTERFTU86V2VsY29tZUAxMjM=")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Cookie", Cookie)
                .uri(URI.create(POST_API_URL)).build();

        HttpResponse<String> postResponse = client.send(postBody, HttpResponse.BodyHandlers.ofString());

        System.out.println("body=====>>>" + postResponse.body());

        String responsePost = parseJSON_BillingHana(postResponse.body());
        System.out.println(responsePost);

        return responsePost;
    }

    public String parseJSON_BillingHana(String jsonString){

        ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder stringBuilder = new StringBuilder();

        try{
            JsonNode rootNode =  objectMapper.readTree(jsonString);
            JsonNode resultsNode = rootNode.path("d").path("HeadToMSG").path("results");

            int i = 0;
            for(JsonNode jsonNode :resultsNode){

                String delivery = jsonNode.get("DELIVERY").asText();

                String message = jsonNode.get("Message").asText();

                if(i == 0){
                    if(!delivery.equals(""))
                        stringBuilder.append("Bill Number : ").append(delivery).append(System.lineSeparator());
                    else
                        stringBuilder.append(System.lineSeparator()).append("Error Message").append(System.lineSeparator());
                }
                stringBuilder.append(System.lineSeparator()).append("Message").append(++i).append(": ").append(message).append(System.lineSeparator());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return String.valueOf(stringBuilder);

        }
    public BillingS4Hana() {
    }

    public BillingS4Hana(String deliveryNumber, String itemNumber, String materialNumber, String customerName, Double openQuantity) {
        this.deliveryNumber = deliveryNumber;
        this.itemNumber = itemNumber;
        this.materialNumber = materialNumber;
        this.customerName = customerName;
        this.openQuantity = openQuantity;
    }

    public String getDeliveryNumber() {
        return deliveryNumber;
    }

    public void setDeliveryNumber(String deliveryNumber) {
        this.deliveryNumber = deliveryNumber;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getMaterialNumber() {
        return materialNumber;
    }

    public void setMaterialNumber(String materialNumber) {
        this.materialNumber = materialNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Double getOpenQuantity() {
        return openQuantity;
    }

    public void setOpenQuantity(Double openQuantity) {
        this.openQuantity = openQuantity;
    }

    @Override
    public String toString() {
        return "BillingS4Hana{" +
                "deliveryNumber='" + deliveryNumber + '\'' +
                ", itemNumber='" + itemNumber + '\'' +
                ", materialNumber='" + materialNumber + '\'' +
                ", customerName='" + customerName + '\'' +
                ", openQuantity=" + openQuantity +
                '}';
    }
}