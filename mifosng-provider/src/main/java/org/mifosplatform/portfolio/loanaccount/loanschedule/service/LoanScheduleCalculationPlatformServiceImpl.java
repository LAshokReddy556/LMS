/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;
import org.mifosplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.mifosplatform.organisation.taxmapping.domain.LoanTaxMap;
import org.mifosplatform.organisation.taxmapping.domain.LoanTaxMapRepository;
import org.mifosplatform.organisation.taxmapping.domain.TaxMap;
import org.mifosplatform.organisation.taxmapping.domain.TaxMapRepositoryWrapper;
import org.mifosplatform.organisation.taxmapping.exception.TaxMapTypeNotExist;
import org.mifosplatform.organisation.taxmapping.exception.TaxTypeNotExist;
import org.mifosplatform.portfolio.accountdetails.domain.AccountType;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.mifosplatform.portfolio.loanaccount.serialization.CalculateLoanScheduleQueryFromApiJsonHelper;
import org.mifosplatform.portfolio.loanaccount.service.LoanReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProduct;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRepository;
import org.mifosplatform.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.mifosplatform.portfolio.loanproduct.serialization.LoanProductDataValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Service
public class LoanScheduleCalculationPlatformServiceImpl implements LoanScheduleCalculationPlatformService {

	private static final String TAXCODE = "Tax Type";
	private static final String PERCENTAGE = "Percentage";
	private static final String FLAT = "Flat";
	
    private final CalculateLoanScheduleQueryFromApiJsonHelper fromApiJsonDeserializer;
    private final LoanScheduleAssembler loanScheduleAssembler;
    private final FromJsonHelper fromJsonHelper;
    private final LoanProductRepository loanProductRepository;
    private final LoanProductDataValidator loanProductCommandFromApiJsonDeserializer;
    private final LoanReadPlatformService loanReadPlatformService;
    private final MCodeReadPlatformService mCodeReadPlatformService;
    private final TaxMapRepositoryWrapper taxMapRepository;
    private final LoanTaxMapRepository loanTaxMapRepository;
    
    

    @Autowired
    public LoanScheduleCalculationPlatformServiceImpl(final CalculateLoanScheduleQueryFromApiJsonHelper fromApiJsonDeserializer,
            final LoanScheduleAssembler loanScheduleAssembler, final FromJsonHelper fromJsonHelper, 
            final LoanProductRepository loanProductRepository, final LoanProductDataValidator loanProductCommandFromApiJsonDeserializer,
            final LoanReadPlatformService loanReadPlatformService, final MCodeReadPlatformService mCodeReadPlatformService,
            final TaxMapRepositoryWrapper taxMapRepository, final LoanTaxMapRepository loanTaxMapRepository) {
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.loanScheduleAssembler = loanScheduleAssembler;
        this.fromJsonHelper = fromJsonHelper;
        this.loanProductRepository = loanProductRepository;
        this.loanProductCommandFromApiJsonDeserializer = loanProductCommandFromApiJsonDeserializer;
        this.loanReadPlatformService = loanReadPlatformService;
        this.mCodeReadPlatformService = mCodeReadPlatformService;
        this.taxMapRepository = taxMapRepository;
        this.loanTaxMapRepository = loanTaxMapRepository;
    }

    @Override
    public LoanScheduleModel calculateLoanSchedule(final JsonQuery query) {

        this.fromApiJsonDeserializer.validate(query.json());
        
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

        final Long productId = this.fromJsonHelper.extractLongNamed("productId", query.parsedJson());
        final LoanProduct loanProduct = this.loanProductRepository.findOne(productId);
        if (loanProduct == null) { throw new LoanProductNotFoundException(productId); }

        if (loanProduct.useBorrowerCycle()) {
            final Long clientId = this.fromJsonHelper.extractLongNamed("clientId", query.parsedJson());
            final Long groupId = this.fromJsonHelper.extractLongNamed("groupId", query.parsedJson());
            Integer cycleNumber = 0;
            if (clientId != null) {
                cycleNumber = this.loanReadPlatformService.retriveLoanCounter(clientId);
            } else if (groupId != null) {
                cycleNumber = this.loanReadPlatformService.retriveLoanCounter(groupId, AccountType.GROUP.getValue());
            }
            this.loanProductCommandFromApiJsonDeserializer.validateMinMaxConstraints(query.parsedJson(), baseDataValidator,
                    loanProduct, cycleNumber);
        } else {
            this.loanProductCommandFromApiJsonDeserializer.validateMinMaxConstraints(query.parsedJson(), baseDataValidator,
                    loanProduct);
        }
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
        
        return this.loanScheduleAssembler.assembleLoanScheduleFrom(query.parsedJson());
    }

	@Override
	public String calculateTaxLoanSchedule(JsonQuery query, boolean flag) {
		// TODO Auto-generated method stub
		JsonObject taxJson;
		JsonArray taxArray = new JsonArray();
		
		this.fromApiJsonDeserializer.validateForTaxes(query.json());
		
		final BigDecimal principal = this.fromJsonHelper.extractBigDecimalWithLocaleNamed("principal", query.parsedJson());
		
		BigDecimal amount = principal;
		
		if (query.parsedJson().isJsonObject()) {
			
            final JsonObject topLevelJsonElement = query.parsedJson().getAsJsonObject();
            
            final Locale locale = this.fromJsonHelper.extractLocaleParameter(topLevelJsonElement);
            
            if (topLevelJsonElement.has("taxes") && topLevelJsonElement.get("taxes").isJsonArray()) {
            	
            	final JsonArray array = topLevelJsonElement.get("taxes").getAsJsonArray();
                                         
				for (int i = 0; i < array.size(); i++) {
					
					taxJson = new JsonObject();
					
                    final JsonObject loanChargeElement = array.get(i).getAsJsonObject();                
                    final Long taxId = this.fromJsonHelper.extractLongNamed("id", loanChargeElement); 
                    
                    amount = calculateTax(loanChargeElement, locale, amount); 
                    final BigDecimal taxAmount = this.fromJsonHelper.extractBigDecimalNamed("taxAmount", loanChargeElement, locale);
                    
                    if(flag) {            	           
                    	TaxMap taxMap = this.taxMapRepository.findOneWithNotFoundDetection(taxId);
                    	
                    	LoanTaxMap loanTaxMap = new LoanTaxMap(0L, taxId, taxAmount, new Date(), taxMap.getTaxInclusive());
                    	this.loanTaxMapRepository.save(loanTaxMap);
                    	taxJson.addProperty("taxMapId", loanTaxMap.getId());
                    }
                    taxJson.addProperty("taxAmount", taxAmount);
                    taxJson.addProperty("taxId", taxId);
                    taxArray.add(taxJson);
                }	
            }
        }
		 
		taxJson = new JsonObject();
		taxJson.addProperty("finalAmount", amount);
		taxJson.add("taxArray", taxArray);
		
		return taxJson.toString();
	}
	
	private BigDecimal calculateTax(JsonObject loanChargeElement, 
			Locale locale, BigDecimal principal) {

		BigDecimal taxAmount;
		final Long taxId = this.fromJsonHelper.extractLongNamed("id", loanChargeElement);
		final String type = this.fromJsonHelper.extractStringNamed("type", loanChargeElement);
		final BigDecimal amount = this.fromJsonHelper.extractBigDecimalNamed("taxValue", loanChargeElement, locale);

		TaxMap taxMap = this.taxMapRepository.findOneWithNotFoundDetection(taxId);
		
		if (isMcodeValue(type)) {
			// String chargeType = taxMap.getChargeType();
			String taxType = taxMap.getTaxType();
			int taxInclusive = taxMap.getTaxInclusive();

			if (PERCENTAGE.equalsIgnoreCase(taxType))
				taxAmount = percentageOf(principal, amount);
			else if (FLAT.equalsIgnoreCase(taxType))
				taxAmount = amount;
			else
				throw new TaxMapTypeNotExist(taxType);
			
			loanChargeElement.addProperty("taxAmount", taxAmount);
			if (taxInclusive == 1) {
				return principal.subtract(taxAmount);
			} else {
				return principal.add(taxAmount);
			}

		} else {
			throw new TaxTypeNotExist(type);
		}
	}

	private boolean isMcodeValue(String mcodeType) {
		
		Collection<MCodeData> mcodeDatas = this.mCodeReadPlatformService.getCodeValue(TAXCODE);
		
        for (MCodeData mCodeData : mcodeDatas) {
			if(mcodeType.equalsIgnoreCase(mCodeData.getmCodeValue())) {
				return true;
			}
		}
		return false;
	}

	private static BigDecimal percentageOf(final BigDecimal value,final BigDecimal percentage) {

        BigDecimal percentageOf = BigDecimal.ZERO;

        if (isGreaterThanZero(value)) {
            final MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
            final BigDecimal multiplicand = percentage.divide(BigDecimal.valueOf(100l), mc);
            percentageOf = value.multiply(multiplicand, mc);
        }
        return percentageOf;
    }
	
	private static boolean isGreaterThanZero(final BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) == 1;
    }
}