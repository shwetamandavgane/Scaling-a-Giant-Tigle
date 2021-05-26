package com.bsds.assignment1.p2.clientp2;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Calculator {

  private final String SEPARATOR = ",";
  private final int METHOD_TYPE_COL_INDEX = 0;
  private final int RESPONSE_CODE_COL_INDEX = 1;
  private final int START_TIME_COL_INDEX = 2;
  private final int END_TIME_COL_INDEX = 3;
  private final int LATENCY_COL_INDEX = 4;

  private String outputCsvFilePathString;
  private Path outputCsvFilePath;
  Double avgLatency = (double) 0;
  Integer maxLatency = 0;
  Integer totalRequests = 0;
  List<Integer> countingArray = new ArrayList<>();
  Integer medianLatency = 0;
  Integer p99Latency = 0;

  public Calculator(String outputCsvFilePathString) {
    this.outputCsvFilePathString = outputCsvFilePathString;
    this.outputCsvFilePath = Paths.get(outputCsvFilePathString);
  }

  public void calculateGraph() {
    generateMeanAndMaxResponseTime();
    generateMedianAndP99();
  }

  public void generateMeanAndMaxResponseTime() {
    Integer totalLatency = 0;

    try {
      BufferedReader bufferedReader = Files.newBufferedReader(outputCsvFilePath);
      bufferedReader.readLine();
      String currentLine = bufferedReader.readLine();

      while (currentLine != null) {
        String[] csvLine = currentLine.split(SEPARATOR);
        String methodName = csvLine[METHOD_TYPE_COL_INDEX];
        Integer latency = Integer.parseInt(csvLine[LATENCY_COL_INDEX]);

        totalLatency += latency;
        totalRequests++;
        countingArray.add(latency);


        if (maxLatency < latency) {
          maxLatency = latency;
        }
        currentLine = bufferedReader.readLine();
      }
      avgLatency = (double) (totalLatency / totalRequests);
      Collections.sort(countingArray);
      this.medianLatency = countingArray.get(countingArray.size() / 2);

    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  private void createCountingArraysForKthValue() {
    generateMeanAndMaxResponseTime();

    try {
      BufferedReader bufferedReader = Files.newBufferedReader(outputCsvFilePath);
      bufferedReader.readLine();
      String csvLine = bufferedReader.readLine();

      while (csvLine != null) {
        String[] data = csvLine.split(SEPARATOR);
        int latency = Integer.parseInt(data[LATENCY_COL_INDEX]);
        Integer[] methodCountingArray = new Integer[latency];
        if (methodCountingArray[latency] == null) {
          methodCountingArray[latency] = 0;
        }
        methodCountingArray[latency]++;

        csvLine = bufferedReader.readLine();
      }
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  private void generateMedianAndP99() {
    if (countingArray.isEmpty()) {
      createCountingArraysForKthValue();
    }

    medianLatency =
        calculatePercentileValue(countingArray, totalRequests, 0.5);
    p99Latency =
        calculatePercentileValue(countingArray, totalRequests, 0.99);

  }

  private Integer calculatePercentileValue(
      List<Integer> countingArray, int totalReqCount, double percentile) {
    Collections.sort(countingArray);
    int index = (int) Math.ceil(percentile  * countingArray.size());
    return countingArray.get(index-1);
  }
}