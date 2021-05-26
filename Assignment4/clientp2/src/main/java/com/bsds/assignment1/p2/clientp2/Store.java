package com.bsds.assignment1.p2.clientp2;

import java.util.Collections;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Store {

  static Params param;

  /**
   * Method to accept inputs from user
   */
  public void acceptInputs() {
    System.out.println("****************GiantTigle Client********");
    System.out.println(
        "Please enter the required inputs when prompted. If you wish to use default values, type 'esc' and press enter key");
    Scanner scanner = new Scanner(System.in);

    //prompt for maxStores
    System.out.print("Enter maximum number of stores to simulate (maxStores): ");
    int maxStores = scanner.nextInt();

    //prompt for maxCustomers
    System.out.print("Enter customers/store: ");
    int maxCustomers = 1000;
    try {
      maxCustomers = scanner.nextInt();
    } catch (InputMismatchException e) {
      maxCustomers = 1000;
    }

    //prompt for max ItemsIDs
    System.out.print("Enter maximum itemIDs: ");
    int maxItems = 100000;
    try {
      maxItems = scanner.nextInt();
    } catch (InputMismatchException e) {
      maxItems = 100000;
    }

    //prompt for purchases/hour
    System.out.print("Enter purchases/hour: ");
    int numPurchases = 60;
    try {
      numPurchases = scanner.nextInt();
    } catch (InputMismatchException e) {
      numPurchases = 60;
    }

    //prompt for items/purchases
    System.out.print("Enter items/purchases: ");
    int itemsperPurchase = 5;
    try {
      itemsperPurchase = scanner.nextInt();
    } catch (InputMismatchException e) {
      itemsperPurchase = 5;
    }

    //prompt for date
    System.out.print("Enter date: ");
    Integer date;
    try {
      date = scanner.nextInt();
      if(date.toString().length()!=8){
        System.out.println("Default date set due to wrong input : 20210101");
        date = 20210101;
      }
    } catch (InputMismatchException e) {
      date = 20210101;
    }

    //prompt for server ip
    System.out.print("Enter server IP: ");
    String ip = "192.10.134.12";
    try {
      ip = scanner.next();
    } catch (InputMismatchException e) {
      ip = "192.10.134.12";
    }


    param = new Params(maxStores, maxCustomers, maxItems,
        numPurchases, itemsperPurchase, date, ip,
        itemsperPurchase);
  }

  public static void main(String[] args) throws InterruptedException {
    Store store = new Store();
    store.acceptInputs();

    /*Thread division per phase  -> Phase 1,2 divide by 4, Phase 3 -> remaining*/
    final Integer maxThreads = param.getMaxStores();
    int centralPhaseThreads = maxThreads / Constants.CENTRAL_PHASE_THREAD_COUNT_DIV.getHeader();
    int westPhaseThreads = maxThreads / Constants.WEST_PHASE_THREAD_COUNT_DIV.getHeader();

    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);

    int phaseEastPhaseDownEnd = param.getNumPurchases() * Constants.CENTRAL_PHASE_PURCHASES_COUNT.getHeader();
    int phaseCentralCountDownEnd = ((int) Math
        .ceil(param.getNumPurchases() * Constants.WEST_PHASE_PURCHASES_COUNT.getHeader()));

    CountDownLatch totalThreads = new CountDownLatch(param.getMaxStores());
    CountDownLatch phaseOneLatch = new CountDownLatch(1);
    CountDownLatch phaseTwoLatch = new CountDownLatch(1);
    CountDownLatch phaseThreeLatch = new CountDownLatch(1);

    Statistics requestStatistics =
        new Statistics("outputData" + param.getMaxStores() + "Threads.csv");
    Thread statsWriteThread = requestStatistics.startWritingToCsv();

    long programStartTime = System.currentTimeMillis();

    System.out.println("East Phase Beginning");
    openStores(param, phaseOneLatch, totalThreads, Constants.EAST_PHASE_NUMBER.getHeader(), phaseEastPhaseDownEnd,
        centralPhaseThreads, successCount, failureCount, requestStatistics);
    phaseOneLatch.await(); //wait for phase 1 purchases to exceed i.e numpurchases * 3

    System.out.println("Central Phase Beginning");
    openStores(param, phaseTwoLatch, totalThreads, Constants.CENTRAL_PHASE_NUMBER.getHeader(), phaseCentralCountDownEnd,
        centralPhaseThreads, successCount, failureCount, requestStatistics);
    phaseTwoLatch.await(); //wait for phase 2 purchases to exceed i.e numpurchases * 5

    System.out.println("West Phase Beginning");
    openStores(param, phaseThreeLatch, totalThreads, Constants.WEST_PHASE_NUMBER.getHeader(), 0, westPhaseThreads,
        successCount, failureCount, requestStatistics);

    //Wait for all threads to complete
    totalThreads.await();
    long endTime = System.currentTimeMillis();
    System.out.println("All requests have been processed at this time");
    System.out.println("Total time : " + ((endTime - programStartTime) / 1000));
    /*  logger.log(Level.INFO, "Client Part Two Starting..........." + store.param.getMaxStores());*/

    requestStatistics.addStatsToQueue(Collections.emptyList());
    statsWriteThread.join();

    requestStatistics.startCalculation();
    requestStatistics.setVals();

    printResults(requestStatistics, store, programStartTime, endTime, successCount, failureCount);
    System.out.println("Client shutting down..........");
  }

  /**
   * @param param
   * @param phaseLatch
   * @param totalThreads
   * @param phaseNumber
   * @param numPurchases
   * @param numThreads
   * @param successCount
   * @param failureCount
   */
  private static void openStores(
      Params param,
      CountDownLatch phaseLatch,
      CountDownLatch totalThreads,
      int phaseNumber,
      int numPurchases,
      int numThreads,
      AtomicInteger successCount,
      AtomicInteger failureCount,
      Statistics requestStatistics) {

    IntStream.range(1, numThreads + 1)
        .forEach(i -> {
          int storeID = phaseNumber * i;
          StoreThread storeThread = new StoreThread(storeID, param, phaseLatch, totalThreads,
              numPurchases, param.getItemsPerPurchase(),successCount, failureCount, requestStatistics);
          (new Thread(storeThread)).start();
        });
  }

  private static void printResults(
      Statistics requestStatistics,
      Store store,
      long startTime,
      long endTime,
      AtomicInteger successCount,
      AtomicInteger failureCount) {

    double wallTime = (endTime - startTime) / 1000;
    double throughput = (successCount.get() + failureCount.get()) / wallTime;

    System.out.println("Max Stores: " + store.param.getMaxStores());
    System.out.println("Number of Successful Requests Sent: " + successCount);
    System.out.println("Number of Unsuccessful Requests: " + failureCount);
    System.out.println("Total Wall Time(s): " + wallTime);
    System.out.println("Throughput (req/s): " + throughput);
    System.out.println("Mean POST response time(ms): " + requestStatistics.getMeanPostLatency());
    System.out
        .println("Median POST response time(ms): " + requestStatistics.getMedianPostLatency());
    System.out.println("Max POST response time(ms): " + requestStatistics.getMaxPostResponseTime());
    System.out.println(
        "99th Percentile POST response time(ms): " + requestStatistics.getP99PostResponseTime());
  }
}
