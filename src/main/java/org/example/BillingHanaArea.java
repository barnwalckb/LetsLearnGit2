package org.example;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.List;

import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.*;
import app.krista.ksdk.entities.EntityReference;
import app.krista.model.base.*;
import org.json.JSONException;

import javax.inject.Inject;
import javax.inject.Named;

@Domain(id = "catEntryDomain_9e555941-54a9-426f-8b25-6e795b4b92c9",
        name = "Chandan",
        ecosystemId = "catEntryEcosystem_a88d397e-d230-4ac6-9299-c961555bde7d",
        ecosystemName = "MetagraphTraining",
        ecosystemVersion = "1b6a24e2-a67b-4459-abeb-aeb551085995")
public class BillingHanaArea {

    private final Invoker invoker;

    @Inject
    public BillingHanaArea(@Named("self") Invoker invoker) {
        this.invoker = invoker;
    }

    public String getAuthCred() {
        String userName = invoker.getAttributes().get("UserName").toString() + ":" + invoker.getAttributes().get("Password").toString();
        String authCred = Base64.getEncoder().encodeToString(userName.getBytes());
        return authCred;
    }

    @CatalogRequest(
            id = "localDomainRequest_bf37628e-d4de-4f9a-a667-67257cdb49dc",
            name = "Get Bill Information S4 Hana",
            description = "Accepts Delivery numbers and returns list of Bill Info.",
            area = "Billing Hana",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Bill Info List", type = "[ Entity ]",required = false)
    public List<BillingS4Hana> getBillInformationS4Hana(
            @Field.Text(name = "Delivery Number", required = true, attributes = {}, options = {}) String deliveryNumber) throws JSONException, IOException, InterruptedException {
            return new BillingS4Hana().getBillingInfo(deliveryNumber,getAuthCred());
    }

    @CatalogRequest(
            id = "localDomainRequest_2b99f2ff-d5c4-4a93-a7ef-88cd38db3c4a",
            name = "Save or Post Bill",
            description = "Save or Posts bill after checking open quantity for a given delivery number.\nReturns Save Bill text response",
            area = "Billing Hana",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Text(name = "Save Bill Response", required = false, attributes = {}, options = {})
    public String saveOrPostBill(
            @Field.Text(name = "Delivery Number", required = true, attributes = {}, options = {}) String deliveryNumber) throws JSONException, IOException, InterruptedException {
            return new BillingS4Hana().postBillingInformation(deliveryNumber,getAuthCred());
    }

    @CatalogRequest(
            id = "localDomainRequest_e4f2a787-7090-4d9e-9242-16cc4163f1ad",
            name = "Check Quantity",
            description = "Takes list of bill Info items and if Open Qty >0 returns True else False",
            area = "Billing Hana",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Boolean(name = "Check Quantity Response", required = false, attributes = {}, options = {})
    public Boolean checkQuantity(
            @Field.Desc(name = "Bill Info List", type = "[ Entity ]",required = true) List<BillingS4Hana> billInfoList) {
            if(!billInfoList.isEmpty()){
                for(BillingS4Hana billingS4Hana : billInfoList){

                    if(billingS4Hana.openQuantity > 0){
                        return true;
                    }
                }

            }return false;
    }

}