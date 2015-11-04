package org.mifosplatform.organisation.taxmapping.service;

import java.util.List;

import org.mifosplatform.organisation.taxmapping.data.TaxMapData;

/**
 * @author hugo
 * 
 */
public interface TaxMapReadPlatformService {

	List<TaxMapData> retriveTaxMapData();

	TaxMapData retrievedSingleTaxMapData(Long id);

}
