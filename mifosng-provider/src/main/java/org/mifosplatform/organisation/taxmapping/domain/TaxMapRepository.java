package org.mifosplatform.organisation.taxmapping.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaxMapRepository extends JpaRepository<TaxMap, Long>,JpaSpecificationExecutor<TaxMap> {

}
