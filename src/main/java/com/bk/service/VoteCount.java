package com.bk.service;

import com.bk.model.ProductName;

public record VoteCount(ProductName product, String displayName, long votes) {
}
