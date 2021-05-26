package com.bsds.assignment1.p2.clientp2;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@NoArgsConstructor
public class Statistics {

  public static final String POST_METHOD_NAME_ONE = "POST";

  private static final Logger logger = LogManager.getLogger(Statistics.class);

  private String fileName;
  private BlockingQueue<Collection<IndividualRequestStatistic>> requestWriteQueue = new LinkedBlockingQueue<>();
  private CsvWriter csvWriter;
  private Calculator statisticsCalculator;

  private Double meanPostLatency;
  private long medianPostLatency;
  private long p99PostResponseTime;
  private long maxPostResponseTime;

  public Statistics(String outputFileName) {
    this.fileName = outputFileName;
    this.csvWriter = new CsvWriter(fileName, requestWriteQueue);
  }

  public Thread startWritingToCsv() {
    return csvWriter.startWriter();
  }

  public void startCalculation() {
    statisticsCalculator = new Calculator(fileName);
    statisticsCalculator.calculateGraph();
  }

  public void setVals() {
    this.meanPostLatency = statisticsCalculator.getAvgLatency();
    this.maxPostResponseTime = statisticsCalculator.getMaxLatency();
    this.medianPostLatency = statisticsCalculator.getMedianLatency();
    this.p99PostResponseTime = statisticsCalculator.getP99Latency();
  }

  public void addStatsToQueue(Collection<IndividualRequestStatistic> statsToAdd) {
    try {
      requestWriteQueue.put(statsToAdd);
    } catch (InterruptedException e) {
      logger.log(Level.FATAL, e.getMessage());
    }
  }

  @Getter
  @Builder
  @AllArgsConstructor
  public static class IndividualRequestStatistic implements Comparable<IndividualRequestStatistic> {

    private long requestStartTime;
    private long requestEndTime;
    private int responseCode;
    private String requestType;

    public long getLatency() {
      return requestEndTime - requestStartTime;
    }

    @Override
    public int compareTo(IndividualRequestStatistic otherStat) {
      return (int) Math.ceil(this.getLatency() - otherStat.getLatency());
    }
  }
}