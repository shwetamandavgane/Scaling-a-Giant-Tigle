package com.northeastern.bsds.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Store {
      private Integer storeId;
      private String storeName;
      private String storeLocation;
}
