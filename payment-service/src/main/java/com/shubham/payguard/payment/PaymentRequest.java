package com.shubham.payguard.payment;

import com.shubham.payguard.common.Channel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PaymentRequest(
        @NotBlank @Size(max = 35) String endToEndId,
        @NotBlank @Size(max = 34) String debtorAccount,
        @NotBlank @Size(max = 34) String creditorAccount,
        @NotNull @DecimalMin("0.01") @Digits(integer = 15, fraction = 2) BigDecimal amount,
        @NotBlank @Pattern(regexp = "[A-Z]{3}", message = "must be a 3-letter ISO 4217 code") String currency,
        @NotNull Channel channel,
        @NotBlank @Pattern(regexp = "[A-Z]{2}", message = "must be a 2-letter ISO 3166 code") String creditorCountry) {
}
