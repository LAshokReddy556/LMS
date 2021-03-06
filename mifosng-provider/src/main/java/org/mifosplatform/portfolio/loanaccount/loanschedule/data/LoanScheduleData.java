/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosplatform.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object to represent aspects of a loan schedule such as:
 * 
 * <ul>
 * <li>Totals information - the totals for each part of repayment schedule
 * monitored.</li>
 * <li>Repayment schedule - the principal due, outstanding balance and cost of
 * loan items such as interest and charges (both fees and penalties)</li>
 * </ul>
 */
//@SuppressWarnings("unused")
public class LoanScheduleData {

    /**
     * The currency associated with all monetary values in loan schedule.
     */
    private final CurrencyData currency;
    private final Integer loanTermInDays;
    private final BigDecimal totalPrincipalDisbursed;
    private final BigDecimal totalPrincipalExpected;
    private final BigDecimal totalPrincipalPaid;
    private final BigDecimal totalInterestCharged;
    private final BigDecimal totalFeeChargesCharged;
    private final BigDecimal totalPenaltyChargesCharged;
    private final BigDecimal totalWaived;
    private final BigDecimal totalWrittenOff;
    private final BigDecimal totalRepaymentExpected;
    private final BigDecimal totalRepayment;
    private final BigDecimal totalPaidInAdvance;
    private final BigDecimal totalPaidLate;
    private final BigDecimal totalOutstanding;
    private  BigDecimal rsidualAmount;
    /**
     * <code>periods</code> is collection of data objects containing specific
     * information to each period of the loan schedule including disbursement
     * and repayment information.
     */
    private final Collection<LoanSchedulePeriodData> periods;

    public LoanScheduleData(final CurrencyData currency, final Collection<LoanSchedulePeriodData> periods, final Integer loanTermInDays,
            final BigDecimal totalPrincipalDisbursed, final BigDecimal totalPrincipalExpected, final BigDecimal totalPrincipalPaid,
            final BigDecimal totalInterestCharged, final BigDecimal totalFeeChargesCharged, final BigDecimal totalPenaltyChargesCharged,
            final BigDecimal totalWaived, final BigDecimal totalWrittenOff, final BigDecimal totalRepaymentExpected,
            final BigDecimal totalRepayment, final BigDecimal totalPaidInAdvance, final BigDecimal totalPaidLate,
            final BigDecimal totalOutstanding) {
        this.currency = currency;
        this.periods = periods;
        this.loanTermInDays = loanTermInDays;
        this.totalPrincipalDisbursed = totalPrincipalDisbursed;
        this.totalPrincipalExpected = totalPrincipalExpected;
        this.totalPrincipalPaid = totalPrincipalPaid;
        this.totalInterestCharged = totalInterestCharged;
        this.totalFeeChargesCharged = totalFeeChargesCharged;
        this.totalPenaltyChargesCharged = totalPenaltyChargesCharged;
        this.totalWaived = totalWaived;
        this.totalWrittenOff = totalWrittenOff;
        this.totalRepaymentExpected = totalRepaymentExpected;
        this.totalRepayment = totalRepayment;
        this.totalPaidInAdvance = totalPaidInAdvance;
        this.totalPaidLate = totalPaidLate;
        this.totalOutstanding = totalOutstanding;
    }

    public Collection<LoanSchedulePeriodData> getPeriods() {
        return this.periods;
    }

	public BigDecimal getRsidualAmount() {
		return rsidualAmount;
	}

	public void setRsidualAmount(BigDecimal rsidualAmount) {
		this.rsidualAmount = rsidualAmount;
	}

	private BigDecimal defaultToZeroIfNull(final BigDecimal possibleNullValue) {
        BigDecimal value = BigDecimal.ZERO;
        if (possibleNullValue != null) {
            value = possibleNullValue;
        }
        return value;
    }
	
	public BigDecimal totalPrincipalExpected() {
		return defaultToZeroIfNull(this.totalPrincipalExpected);
	}
	
	public BigDecimal totalInterestCharged() {
		return defaultToZeroIfNull(this.totalInterestCharged);
	}
	
	public BigDecimal totalFeeChargesCharged() {
		return defaultToZeroIfNull(this.totalFeeChargesCharged);
	}
	
	public BigDecimal totalPenaltyChargesCharged() {
		return defaultToZeroIfNull(this.totalPenaltyChargesCharged);
	}
	
	public BigDecimal totalRepaymentExpected() {
		return defaultToZeroIfNull(this.totalRepaymentExpected);
	}
	
	public BigDecimal totalRepayment() {
		return defaultToZeroIfNull(this.totalRepayment);
	}
	
	public BigDecimal totalOutstanding() {
		return defaultToZeroIfNull(this.totalOutstanding);
	}	
    
}