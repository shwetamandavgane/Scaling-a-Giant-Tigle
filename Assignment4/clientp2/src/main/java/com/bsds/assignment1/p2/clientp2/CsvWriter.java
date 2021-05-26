package com.bsds.assignment1.p2.clientp2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CsvWriter {

  private final String CSV_HEADERS =
      "RequestType,ResponseCode,StartTimeStamp,EndTimeStamp,Latency";

  private String fileName;
  private PrintWriter printWriter;
  private BlockingQueue<Collection<Statistics.IndividualRequestStatistic>> statsQueue;

  CsvWriter(String fileName,
      BlockingQueue<Collection<Statistics.IndividualRequestStatistic>> statsQueue) {
    this.fileName = fileName;
    this.statsQueue = statsQueue;
  }

  public Thread startWriter() {
    createFile();
    return createWriteThread();
  }

  public void createFile() {
    File outputFile = new File(fileName);
    try {
      printWriter = new PrintWriter(outputFile);
      printWriter.println(CSV_HEADERS);
    } catch (FileNotFoundException e) {
      System.err.println(e.getMessage());
    }
  }

  private String generateCsvLine(Statistics.IndividualRequestStatistic stat) {
    return stat.getRequestType() + "," + stat.getResponseCode() + "," + stat.getRequestStartTime()
        + "," + stat.getRequestEndTime() + "," + stat.getLatency();
  }

  private void writeThreadData() {
    try {
      Collection<Statistics.IndividualRequestStatistic> reqStats = statsQueue.take();
      while (reqStats.size() > 0) {
        reqStats.forEach(stats -> {
          String csvLine = generateCsvLine(stats);
          printWriter.println(csvLine);
        });
        reqStats = statsQueue.take();
      }
      printWriter.close();
    } catch (InterruptedException e) {
      System.err.println(e.getMessage());
    }
  }

  /**
   * Create a thread for writing to csv
   *
   * @return
   */
  public Thread createWriteThread() {
    Runnable thread = this::writeThreadData;
    Thread writeThread = new Thread(thread);
    writeThread.start();
    return writeThread;
  }
}