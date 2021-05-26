package com.bsds.assignment1.p2.clientp2;

public enum Constants {

   CENTRAL_PHASE_THREAD_COUNT_DIV(4),
   WEST_PHASE_THREAD_COUNT_DIV(2),

   CENTRAL_PHASE_PURCHASES_COUNT( 3 ),
   WEST_PHASE_PURCHASES_COUNT(5),

   EAST_PHASE_NUMBER (4),
   CENTRAL_PHASE_NUMBER (5),
   WEST_PHASE_NUMBER (6);
  private Integer header;

  //Constructor for initialization
  Constants(final Integer header) {
    this.header = header;
  }

  public Integer getHeader() {
    return header;
  }
}

