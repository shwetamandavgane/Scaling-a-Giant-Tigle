package com.bsds.assignment1.p2.clientp2;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Request {
  private List<Item> items = new ArrayList<>();

  @Override
  public String toString() {
    return "Request{" +
        "items=" + items +
        '}';
  }
}
