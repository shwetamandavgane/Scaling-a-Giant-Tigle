package com.bsds.assignment1.p2.clientp2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {

 // private Integer storeID;
  private Integer itemID;
  private Integer numberOfItems;

  @Override
  public String toString() {
    return "Item{" +
        "itemID='" + itemID + '\'' +
        ", numberOfItems=" + numberOfItems +
        '}';
  }
}
