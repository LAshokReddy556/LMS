package org.mifosplatform.organisation.taxmapping.domain;

import org.mifosplatform.organisation.taxmapping.exception.TaxMapInstanceNotFoundException;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstance;
import org.mifosplatform.portfolio.calendar.domain.CalendarInstanceRepository;
import org.mifosplatform.portfolio.calendar.exception.CalendarInstanceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link TaxMapRepository} that is responsible for
 * checking if {@link TaxMap} is returned when using
 * <code>findOne</code> repository method and throwing an appropriate not found
 * exception.
 * </p>
 * 
 * <p>
 * This is to avoid need for checking and throwing in multiple areas of code
 * base where {@link TaxMapRepository} is required.
 * </p>
 */
@Service
public class TaxMapRepositoryWrapper {

	private final TaxMapRepository repository;

    @Autowired
    public TaxMapRepositoryWrapper(final TaxMapRepository repository) {
        this.repository = repository;
    }

    public TaxMap findOneWithNotFoundDetection(final Long taxId) {
        final TaxMap taxMap = this.repository.findOne(taxId);
        if (taxMap == null) { throw new TaxMapInstanceNotFoundException(taxId); }
        return taxMap;
    }

    public void save(final TaxMap taxMap) {
        this.repository.save(taxMap);
    }

    public void delete(final TaxMap taxMap) {
        this.repository.delete(taxMap);
    }

    public void saveAndFlush(final TaxMap taxMap) {
        this.repository.saveAndFlush(taxMap);
    }
}
