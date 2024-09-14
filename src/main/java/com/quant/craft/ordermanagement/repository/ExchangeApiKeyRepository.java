package com.quant.craft.ordermanagement.repository;

import com.quant.craft.ordermanagement.domain.exchange.ExchangeApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeApiKeyRepository extends JpaRepository<ExchangeApiKey, Long> {

}
