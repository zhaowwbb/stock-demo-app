package com.demo.stocks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class FinnhubQuoteResponse {
    @JsonProperty("o") public BigDecimal open;
    @JsonProperty("h") public BigDecimal high;
    @JsonProperty("l") public BigDecimal low;
    @JsonProperty("c") public BigDecimal close;
    @JsonProperty("v") public Long volume;
    @JsonProperty("t") public Long timestamp; // Unix epoch timestamp seconds
}