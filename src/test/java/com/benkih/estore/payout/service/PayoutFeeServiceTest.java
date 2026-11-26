package com.benkih.estore.payout.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.benkih.estore.common.enums.CurrencyCode;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PayoutFeeServiceTest {

    private final PayoutFeeService service = new PayoutFeeService();

    @Test
    void transferFeeUsesThePaystackNgnBands() {
        assertEquals(bd(10), service.calculateTransferFee(bd(5_000), CurrencyCode.NGN));
        assertEquals(bd(25), service.calculateTransferFee(bd(20_000), CurrencyCode.NGN));
        assertEquals(bd(25), service.calculateTransferFee(bd(50_000), CurrencyCode.NGN));
        assertEquals(bd(50), service.calculateTransferFee(bd(50_001), CurrencyCode.NGN));
    }

    @Test
    void stampDutyAppliesFromTenThousandNaira() {
        assertEquals(BigDecimal.ZERO, service.calculateStampDuty(bd(9_999), CurrencyCode.NGN));
        assertEquals(bd(50), service.calculateStampDuty(bd(10_000), CurrencyCode.NGN));
    }

    @Test
    void rejectsNonPositiveAmountsAndUnsupportedCurrencies() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculateTransferFee(BigDecimal.ZERO, CurrencyCode.NGN));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculateTransferFee(bd(1_000), CurrencyCode.USD));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculateStampDuty(null, CurrencyCode.NGN));
    }

    private BigDecimal bd(long value) {
        return BigDecimal.valueOf(value);
    }
}
