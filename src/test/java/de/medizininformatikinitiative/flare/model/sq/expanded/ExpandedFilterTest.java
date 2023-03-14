package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static de.medizininformatikinitiative.flare.model.sq.Comparator.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ExpandedFilterTest {


    public static final TermCode UNIT = new TermCode("http://unitsofmeasure.org", "g/dL", "g/dL");
    public static final BigDecimal RANGE_LOWER_BOUND = BigDecimal.valueOf(4);
    public static final BigDecimal RANGE_UPPER_BOUND = BigDecimal.valueOf(10);
    public static final BigDecimal COMPARATOR_VALUE = BigDecimal
            .valueOf(10);

    @Test
    void comparatorFilter_WithoutUnit() {
        ExpandedComparatorFilter comparatorFilter = new ExpandedComparatorFilter("value-quantity",
                                                                                 LESS_THAN, COMPARATOR_VALUE, null);

        assertThat(comparatorFilter.toParams()).isEqualTo(QueryParams.of("value-quantity",
                                                                         LESS_THAN.toString() + COMPARATOR_VALUE));
    }

    @Test
    void comparatorFilter_WithUnit() {
        ExpandedComparatorFilter comparatorFilter = new ExpandedComparatorFilter("value-quantity",
                                                                                 LESS_THAN, COMPARATOR_VALUE, UNIT);

        assertThat(comparatorFilter.toParams()).isEqualTo(QueryParams.of("value-quantity",
                                                                         LESS_THAN.toString() + COMPARATOR_VALUE +
                                                                                 "|" + UNIT.system() + "|" + UNIT.code()));
    }


    @Test
    void rangeFilter_WithoutUnit() {
        ExpandedRangeFilter rangeFilter = new ExpandedRangeFilter("value-quantity", RANGE_LOWER_BOUND,
                                                                  RANGE_UPPER_BOUND, null);

        QueryParams expectedQueryParams = QueryParams.of("value-quantity",
                                                         GREATER_EQUAL.toString() + RANGE_LOWER_BOUND)
                .appendParam("value-quantity", LESS_EQUAL.toString() + RANGE_UPPER_BOUND);

        assertThat(rangeFilter.toParams()).isEqualTo(expectedQueryParams);
    }

    @Test
    void rangeFilter_WithUnit() {
        ExpandedRangeFilter rangeFilter = new ExpandedRangeFilter("value-quantity", RANGE_LOWER_BOUND,
                                                                  RANGE_UPPER_BOUND, UNIT);

        QueryParams expectedQueryParams = QueryParams.of("value-quantity", GREATER_EQUAL.toString() +
                        RANGE_LOWER_BOUND + "|" + UNIT.system() + "|" + UNIT.code())
                .appendParam("value-quantity", LESS_EQUAL.toString() + RANGE_UPPER_BOUND + "|" +
                        UNIT.system() + "|" + UNIT.code());

        assertThat(rangeFilter.toParams()).isEqualTo(expectedQueryParams);
    }

    @Test
    void appendParam_WithoutUnit() {
        QueryParams queryParams = QueryParams.EMPTY.appendParam("value-quantity", LESS_EQUAL,
                                                                BigDecimal.valueOf(34), null);

        assertThat(queryParams).isEqualTo(QueryParams.of("value-quantity", LESS_EQUAL.toString() +
                BigDecimal.valueOf(34)));
    }

}
