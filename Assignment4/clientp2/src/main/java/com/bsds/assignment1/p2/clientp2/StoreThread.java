package com.bsds.assignment1.p2.clientp2;

import com.bsds.assignment1.p2.clientp2.Statistics.IndividualRequestStatistic;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.PurchaseApi;
import io.swagger.client.model.Purchase;
import io.swagger.client.model.PurchaseItems;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class StoreThread implements Runnable {

  private static final int POST_SUCCESS_CODE = 201;

  private int storeID;
  Params parameter;
  Integer numPurchases;
  Integer itemsPerPurchase;

  private CountDownLatch phaseLatch;
  private CountDownLatch totalPhases;
  final Integer DEFAULT_ITEMS_COUNT = 2;

  private AtomicInteger successCount;
  private AtomicInteger failureCount;
  private Statistics requestStatistics;

  public StoreThread(int storeID, Params parameter, CountDownLatch phaseLatch,
      CountDownLatch totalPhases, Integer numPurchases, Integer itemsPerPurchase, AtomicInteger successCount,
      AtomicInteger failureCount, Statistics requestStatistics) {
    this.storeID = storeID;
    this.parameter = parameter;
    this.phaseLatch = phaseLatch;
    this.totalPhases = totalPhases;
    this.numPurchases = numPurchases;
    this.itemsPerPurchase = itemsPerPurchase;
    this.successCount = successCount;
    this.failureCount = failureCount;
    this.requestStatistics = requestStatistics;
  }

  @Override
  public void run() {
    int numPurchases = 0;
    LinkedList<Statistics.IndividualRequestStatistic> threadStats = new LinkedList<>();

    for (int hour = 1; hour <= 9; hour++) {
      for (int requests = 0; requests < this.parameter.getNumPurchases(); requests++) {
        PurchaseApi purchaseApi = new PurchaseApi();
        ApiClient apiClient = purchaseApi.getApiClient();
        apiClient.setReadTimeout(300000);
        apiClient.setWriteTimeout(300000);
        apiClient.setConnectTimeout(300000);

        /**Generate custIDs between storeID*1000 and storeID*1000+customers/store**/
        Integer custID = ThreadLocalRandom.current().nextInt(storeID*1000, this.storeID * 1000 + parameter.getMaxCustomers());
        //set base URL
       // apiClient.setBasePath(
         //   "http://localhost/purchase/911/customer/411/date/11111118");
         apiClient.setBasePath(
          "http://bsds-612468346.us-east-1.elb.amazonaws.com/Server_6650_war/tigle/purchase/"
             + this.storeID + "/customer/" + custID + "/date/12121212");
        //set request body
        Purchase purchase = new Purchase();
        List<PurchaseItems> items = new ArrayList<>();
        for (int i = 0; i < parameter.getItemsPerPurchase(); i++) {
          PurchaseItems item = new PurchaseItems();
          item.setItemID(String.valueOf(ThreadLocalRandom.current().nextInt(0, 100)));
          item.setNumberOfItems(ThreadLocalRandom.current().nextInt(0, itemsPerPurchase*500));
          items.add(item);
        }
        purchase.setItems(items);
        long startTime = 0,endTime = 0;
        ApiResponse<Void> response = null;
        try {
          int flag = 0;
          startTime = System.currentTimeMillis();
          while(flag < 3) {
            response = purchaseApi
                .newPurchaseWithHttpInfo(purchase, storeID, custID, parameter.getDate().toString());

            if(response.getStatusCode()==201){
              endTime = System.currentTimeMillis();
              break;
            }
            flag++;
          }
          if(flag ==3)
            endTime = System.currentTimeMillis();

          incrementCounts(response.getStatusCode() == POST_SUCCESS_CODE);

          Statistics.IndividualRequestStatistic stats =
              new IndividualRequestStatistic(startTime, endTime, response.getStatusCode()
                    , Statistics.POST_METHOD_NAME_ONE);
          threadStats.add(stats);

        } catch (ApiException e) {
          failureCount.incrementAndGet();
          endTime = System.currentTimeMillis();
          Statistics.IndividualRequestStatistic stats=
                new IndividualRequestStatistic(startTime, endTime, e.getCode()
                    , Statistics.POST_METHOD_NAME_ONE);
          threadStats.add(stats);
        }

        numPurchases++;

        //when the threshold is reached decrement lat32ch -> numpurchases*3/5
        if (numPurchases == this.numPurchases) {
          phaseLatch.countDown();
        }
      }
    }
    //decrement latch for threads
    totalPhases.countDown();
    requestStatistics.addStatsToQueue(threadStats);
  }

  private void incrementCounts(boolean isCorrectResponse) {
    if (isCorrectResponse) {
      successCount.incrementAndGet();
    } else {
      failureCount.incrementAndGet();
    }
  }
}