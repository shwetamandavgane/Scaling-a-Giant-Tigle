package com.bsds.assignment1.p2.clientp2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Params {

  private Integer maxStores;
  private Integer maxCustomers;
  private Integer maxItems;
  private Integer numPurchases;
  private Integer numItems;
  private Integer date;
  private String ip;
  private Integer itemsPerPurchase;
}
