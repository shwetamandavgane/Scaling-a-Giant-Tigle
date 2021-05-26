package com.northeastern.bsds.store.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Items {
  private Integer storeid;
  private Integer itemId;
  private Integer numberOfItems;

}
