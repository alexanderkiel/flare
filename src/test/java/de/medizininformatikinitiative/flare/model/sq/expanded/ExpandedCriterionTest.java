package de.medizininformatikinitiative.flare.model.sq.expanded;

import de.medizininformatikinitiative.flare.model.fhir.Query;
import de.medizininformatikinitiative.flare.model.fhir.QueryParams;
import de.medizininformatikinitiative.flare.model.sq.Comparator;
import de.medizininformatikinitiative.flare.model.sq.TermCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static de.medizininformatikinitiative.flare.model.sq.Comparator.GREATER_EQUAL;
import static de.medizininformatikinitiative.flare.model.sq.Comparator.LESS_EQUAL;
import static org.assertj.core.api.Assertions.assertThat;

class ExpandedCriterionTest {

    static final TermCode UNIT = new TermCode("http://unitsofmeasure.org", "ug/dL", "ug/dL");
    static final TermCode C71_1 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "C71.1", "Frontallappen");
    static final TermCode CONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "confirmed", "Confirmed");
    static final TermCode SEVERE = TermCode.of("http://snomed.info/sct", "24484000", "Severe");
    static final TermCode CORTISOL = TermCode.of("http://loinc.org", "2143-6", "Cortisol");
    static final BigDecimal DECIMAL_LB = BigDecimal.valueOf(17.9);
    static final BigDecimal DECIMAL_UB = BigDecimal.valueOf(22);
    static final BigDecimal DECIMAL_LB_1 = BigDecimal.valueOf(23);
    static final BigDecimal DECIMAL_UB_1 = BigDecimal.valueOf(42);
    static final BigDecimal DECIMAL_LB_2 = BigDecimal.valueOf(30);
    static final BigDecimal DECIMAL_UB_2 = BigDecimal.valueOf(43.5);
    static final BigDecimal DECIMAL_1 = BigDecimal.valueOf(7.3);
    static final BigDecimal DECIMAL_2 = BigDecimal.valueOf(10);

    @Test
    void toQuery() {
        var criterion = ExpandedCriterion.of("Condition", "code", C71_1);

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.of("code", C71_1)));
    }

    @Test
    void toQuery_withOneConceptFilter() {
        var criterion = ExpandedCriterion.of("Condition", "code", C71_1)
                .appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.EMPTY
                .appendParam("code", C71_1)
                .appendParam("verification-status", CONFIRMED)));
    }

    @Test
    void toQuery_withTwoConceptFilters() {
        var criterion = ExpandedCriterion.of("Condition", "code", C71_1)
                .appendFilter(new ExpandedConceptFilter("verification-status", CONFIRMED))
                .appendFilter(new ExpandedConceptFilter("severity", SEVERE));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Condition", QueryParams.EMPTY
                .appendParam("code", C71_1)
                .appendParam("verification-status", CONFIRMED)
                .appendParam("severity", SEVERE)));
    }

    @ParameterizedTest
    @EnumSource
    void toQuery_withOneComparatorFilter(Comparator comparator) {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedComparatorFilter(
                        "value-quantity", comparator, DECIMAL_1,
                        UNIT));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam("value-quantity", comparator,
                        DECIMAL_1, UNIT)));
    }

    @ParameterizedTest
    @MethodSource("arityTwoComparatorArgumentProvider")
    void toQuery_withTwoComparatorFilters(Comparator comparator1, Comparator comparator2) {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedComparatorFilter("value-quantity", comparator1, DECIMAL_1, UNIT))
                .appendFilter(new ExpandedComparatorFilter("value-quantity", comparator2, DECIMAL_2, UNIT));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam("value-quantity", comparator1, DECIMAL_1, UNIT)
                .appendParam("value-quantity", comparator2, DECIMAL_2, UNIT)));

    }

    @Test
    void toQuery_withOneRangeFilter() {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedRangeFilter("value-quantity", DECIMAL_LB, DECIMAL_UB, UNIT));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam("value-quantity", GREATER_EQUAL, DECIMAL_LB, UNIT)
                .appendParam("value-quantity", LESS_EQUAL, DECIMAL_UB, UNIT)));
    }


    @Test
    void toQuery_withTowRangeFilters() {
        var criterion = ExpandedCriterion.of("Observation", "code", CORTISOL)
                .appendFilter(new ExpandedRangeFilter("value-quantity", DECIMAL_LB_1, DECIMAL_UB_1, UNIT))
                .appendFilter(new ExpandedRangeFilter("value-quantity", DECIMAL_LB_2, DECIMAL_UB_2, UNIT));

        var query = criterion.toQuery();

        assertThat(query).isEqualTo(Query.of("Observation", QueryParams.EMPTY
                .appendParam("code", CORTISOL)
                .appendParam("value-quantity", GREATER_EQUAL, DECIMAL_LB_1, UNIT)
                .appendParam("value-quantity", LESS_EQUAL, DECIMAL_UB_1, UNIT)
                .appendParam("value-quantity", GREATER_EQUAL, DECIMAL_LB_2, UNIT)
                .appendParam("value-quantity", LESS_EQUAL, DECIMAL_UB_2, UNIT)));
    }

    static Stream<Arguments> arityTwoComparatorArgumentProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Comparator c1 : Comparator.values()) {
            for (Comparator c2 : Comparator.values()) {
                argumentBuilder.add(Arguments.of(c1, c2));
            }
        }
        return argumentBuilder.build();
    }
}
