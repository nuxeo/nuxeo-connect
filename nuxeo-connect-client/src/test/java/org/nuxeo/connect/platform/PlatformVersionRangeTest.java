package org.nuxeo.connect.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.Test;

public class PlatformVersionRangeTest {

    private static final PlatformVersion BASE_VERSION_1 = new PlatformVersion("1.0.0");

    @Test
    public void testValidRangesBoundaries() {
        assertThat(PlatformVersionRange.fromRangeSpec("1").containsVersion(BASE_VERSION_1)).isTrue();
        assertThat(PlatformVersionRange.fromRangeSpec("1.0").containsVersion(BASE_VERSION_1)).isTrue();
        assertThat(PlatformVersionRange.fromRangeSpec("1.0.0").containsVersion(BASE_VERSION_1)).isTrue();
        assertThat(PlatformVersionRange.fromRangeSpec("[1]").containsVersion(BASE_VERSION_1)).isTrue();
        assertThat(PlatformVersionRange.fromRangeSpec("[1.0]").containsVersion(BASE_VERSION_1)).isTrue();
        assertThat(PlatformVersionRange.fromRangeSpec("[1.0.0]").containsVersion(BASE_VERSION_1)).isTrue();

        PlatformVersionRange range = PlatformVersionRange.fromRangeSpec("[1,1.1]");
        assertThat(range.containsVersion(new PlatformVersion("0.9999.9999"))).isFalse();
        assertThat(range.containsVersion(BASE_VERSION_1)).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.0-A"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1-A"))).isFalse();
        assertThat(range.containsVersion(new PlatformVersion("1.1.1"))).isFalse();
        assertThat(range.containsVersion(new PlatformVersion("1.1.1-A"))).isFalse();

        range = PlatformVersionRange.fromRangeSpec("(,)");
        assertThat(range.containsVersion(new PlatformVersion("0.9999.9999"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("0.9999.9999-ZZ"))).isTrue();
        assertThat(range.containsVersion(BASE_VERSION_1)).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.0-A"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1-A"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1.1-A"))).isTrue();

        range = PlatformVersionRange.fromRangeSpec("[,]");
        assertThat(range.containsVersion(new PlatformVersion("0.9999.9999"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("0.9999.9999-ZZ"))).isTrue();
        assertThat(range.containsVersion(BASE_VERSION_1)).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.0-A"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1-A"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1.1-A"))).isTrue();

        range = PlatformVersionRange.fromRangeSpec("[,)");
        assertThat(range.containsVersion(new PlatformVersion("0.9999.9999"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("0.9999.9999-ZZ"))).isTrue();
        assertThat(range.containsVersion(BASE_VERSION_1)).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.0-A"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1-A"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1.1-A"))).isTrue();

        range = PlatformVersionRange.fromRangeSpec("(,]");
        assertThat(range.containsVersion(new PlatformVersion("0.9999.9999"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("0.9999.9999-ZZ"))).isTrue();
        assertThat(range.containsVersion(BASE_VERSION_1)).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.0-A"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1-A"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1.1-A"))).isTrue();

        range = PlatformVersionRange.fromRangeSpec("[1,)");
        assertThat(range.containsVersion(new PlatformVersion("0.9999.9999"))).isFalse();
        assertThat(range.containsVersion(new PlatformVersion("0.9999.9999-ZZ"))).isFalse();
        assertThat(range.containsVersion(BASE_VERSION_1)).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.0-A"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1-A"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1.1-A"))).isTrue();

        range = PlatformVersionRange.fromRangeSpec("[,1.1)");
        assertThat(range.containsVersion(new PlatformVersion("0.9999.9999"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("0.9999.9999-ZZ"))).isTrue();
        assertThat(range.containsVersion(BASE_VERSION_1)).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.0-A"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1"))).isFalse();
        assertThat(range.containsVersion(new PlatformVersion("1.1-A"))).isFalse();
        assertThat(range.containsVersion(new PlatformVersion("1.1.1"))).isFalse();
        assertThat(range.containsVersion(new PlatformVersion("1.1.1-A"))).isFalse();

        range = PlatformVersionRange.fromRangeSpec("(1,1.1)");
        assertThat(range.containsVersion(new PlatformVersion("0.9999.9999"))).isFalse();
        assertThat(range.containsVersion(BASE_VERSION_1)).isFalse();
        assertThat(range.containsVersion(new PlatformVersion("1.0.0-A"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.1"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.1"))).isFalse();
        assertThat(range.containsVersion(new PlatformVersion("1.1-A"))).isFalse();
        assertThat(range.containsVersion(new PlatformVersion("1.1.1"))).isFalse();
        assertThat(range.containsVersion(new PlatformVersion("1.1.1-A"))).isFalse();

        range = PlatformVersionRange.fromRangeSpec("[1-A,1-C)");
        assertThat(range.containsVersion(BASE_VERSION_1)).isFalse();
        assertThat(range.containsVersion(new PlatformVersion("1.0.0-."))).isFalse();
        assertThat(range.containsVersion(new PlatformVersion("1.0.0-A"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.0-AA"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.0-B"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.0-B9"))).isTrue();
        assertThat(range.containsVersion(new PlatformVersion("1.0.0-C"))).isFalse();
        assertThat(range.containsVersion(new PlatformVersion("1.0.1"))).isFalse();

    }

    @Test
    public void testInvalidRangeExceptions() {
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec(null))
                                            .withMessage("Range cannot be blank");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec(""))
                                            .withMessage("Range cannot be blank");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("   "))
                                            .withMessage("Range cannot be blank");

        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec(",1"))
                                            .withMessage("Range should start with '[' or '(': ,1");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("1,"))
                                            .withMessage("Range should start with '[' or '(': 1,");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("*1,"))
                                            .withMessage("Range should start with '[' or '(': *1,");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("[1,"))
                                            .withMessage("Range should end with ']' or ')': [1,");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("(1,"))
                                            .withMessage("Range should end with ']' or ')': (1,");

        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("(1,1)"))
                                            .withMessage(
                                                    "Range cannot have identical boundaries with exclusions: (1,1)");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("[1,1.0)"))
                                            .withMessage(
                                                    "Range cannot have identical boundaries with exclusions: [1,1.0)");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("(1-A,1.0-a]")) //
                                            .withMessage(
                                                    "Range cannot have identical boundaries with exclusions: (1-A,1.0-a]");

        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("[1.1,1.0]"))
                                            .withMessage("Range defies version ordering: [1.1,1.0]");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("(1.0-A,1.0)"))
                                            .withMessage("Range defies version ordering: (1.0-A,1.0)");

        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("(1)")) //
                                            .withMessage(
                                                    "Single version can only have inclusive boundaries ('[x.y.z]'): (1)");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("[1)")) //
                                            .withMessage(
                                                    "Single version can only have inclusive boundaries ('[x.y.z]'): [1)");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("(1]")) //
                                            .withMessage(
                                                    "Single version can only have inclusive boundaries ('[x.y.z]'): (1]");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("(1")) //
                                            .withMessage(
                                                    "Single version can only have inclusive boundaries ('[x.y.z]'): (1");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("1)")) //
                                            .withMessage(
                                                    "Single version can only have inclusive boundaries ('[x.y.z]'): 1)");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("[1")) //
                                            .withMessage(
                                                    "Single version can only have inclusive boundaries ('[x.y.z]'): [1");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("1]")) //
                                            .withMessage(
                                                    "Single version can only have inclusive boundaries ('[x.y.z]'): 1]");

        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("1/")) //
                                            .withMessage("Version should at least explicit a major number: 1/");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("*1")) //
                                            .withMessage("Version should at least explicit a major number: *1");
        assertThatIllegalArgumentException().isThrownBy(() -> PlatformVersionRange.fromRangeSpec("qualifierOnly"))
                                            .withMessage(
                                                    "Version should at least explicit a major number: qualifierOnly");
        assertThatIllegalArgumentException().isThrownBy(() -> new PlatformVersion("-qualifierOnly"))
                                            .withMessage(
                                                    "Version should at least explicit a major number: -qualifierOnly");

        assertThatIllegalArgumentException().isThrownBy(() -> new PlatformVersion("1-qualifier with space"))
                                            .withMessage(
                                                    "Version cannot contain whitespaces in qualifier: 1-qualifier with space");
    }
}
