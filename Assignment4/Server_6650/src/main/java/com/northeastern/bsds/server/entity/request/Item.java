package com.northeastern.bsds.server.entity.request;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
  @SerializedName("ItemID")
  private String itemID;

  @SerializedName("numberOfItems:")
  private Integer numberOfItems;

  @Override
  public String toString() {
    return "Item{" +
        "itemID='" + itemID + '\'' +
        ", numberOfItems=" + numberOfItems +
        '}';
  }
}
