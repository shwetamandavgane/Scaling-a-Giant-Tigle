package com.northeastern.bsds.store.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class StoreResponse {
  List<Stores> stores = new ArrayList<>();
}
