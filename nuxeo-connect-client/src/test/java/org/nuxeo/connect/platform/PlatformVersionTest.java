package org.nuxeo.connect.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.Test;
import org.nuxeo.connect.platform.PlatformVersion;

public class PlatformVersionTest {

    private static final PlatformVersion BASE_VERSION_1 = new PlatformVersion("1.0.0");

    @Test
    public void testValidVersionsComparison() {
        assertThat(BASE_VERSION_1).isEqualTo(new PlatformVersion("1.0.0"));
        assertThat(BASE_VERSION_1).isEqualByComparingTo(new PlatformVersion("1.0.0"));
        assertThat(BASE_VERSION_1).isEqualTo(new PlatformVersion("1"));
        assertThat(BASE_VERSION_1).isEqualTo(new PlatformVersion("1."));
        assertThat(BASE_VERSION_1).isEqualTo(new PlatformVersion("1.."));
        assertThat(BASE_VERSION_1).isEqualTo(new PlatformVersion("1..."));
        assertThat(BASE_VERSION_1).isEqualTo(new PlatformVersion("1-"));
        assertThat(BASE_VERSION_1).isEqualTo(new PlatformVersion("1.-"));
        assertThat(BASE_VERSION_1).isEqualTo(new PlatformVersion("0001"));
        assertThat(BASE_VERSION_1).isEqualTo(new PlatformVersion("01.00"));
        assertThat(BASE_VERSION_1).isEqualTo(new PlatformVersion("1.0.000"));
        assertThat(BASE_VERSION_1).isEqualTo(new PlatformVersion("001.000.0-"));
        assertThat(BASE_VERSION_1).isGreaterThan(new PlatformVersion("0"));
        assertThat(BASE_VERSION_1).isGreaterThan(new PlatformVersion("0.0.1"));
        assertThat(BASE_VERSION_1).isGreaterThan(new PlatformVersion("0.1.0"));
        assertThat(BASE_VERSION_1).isGreaterThan(new PlatformVersion("0.9999.9999"));
        assertThat(BASE_VERSION_1).isGreaterThan(new PlatformVersion("0.9999.9999-ZZZZZ"));
        assertThat(BASE_VERSION_1).isGreaterThan(new PlatformVersion("0.-9999.9999-ZZZZZ"));
        assertThat(BASE_VERSION_1).isGreaterThan(new PlatformVersion("0.9999.-9999"));
        assertThat(BASE_VERSION_1).isLessThan(new PlatformVersion("1.0.0-0"));
        assertThat(BASE_VERSION_1).isLessThan(new PlatformVersion("1.0.0-a"));
        assertThat(BASE_VERSION_1).isLessThan(new PlatformVersion("1.0.0--"));
        assertThat(BASE_VERSION_1).isLessThan(new PlatformVersion("1.0.0-ALPHA"));
        assertThat(BASE_VERSION_1).isLessThan(new PlatformVersion("1.0.0-BETA1"));
        assertThat(BASE_VERSION_1).isLessThan(new PlatformVersion("1.0.0-m2"));
        assertThat(BASE_VERSION_1).isLessThan(new PlatformVersion("1.0.0-RC.3"));
        assertThat(BASE_VERSION_1).isLessThan(new PlatformVersion("1.0.0-SNAPSHOT"));
        assertThat(BASE_VERSION_1).isLessThan(new PlatformVersion("1.0.000000001"));
        assertThat(BASE_VERSION_1).isLessThan(new PlatformVersion("1.0.1"));
        assertThat(BASE_VERSION_1).isLessThan(new PlatformVersion("1.1.0"));
        assertThat(BASE_VERSION_1).isLessThan(new PlatformVersion("2.0.0"));

        assertThat(new PlatformVersion("1-qualifierCase")).isEqualTo(new PlatformVersion("1-QualifierCASE"));
        assertThat(new PlatformVersion("  1  -  unTrimmed  ")).isEqualTo(new PlatformVersion("1.0.0-unTrimmed"));
        assertThat(new PlatformVersion("1  . 2.  3  -  unTrimmed  ")).isEqualTo(new PlatformVersion("1.2.3-unTrimmed"));
        assertThat(new PlatformVersion("1-specials²&é~\"#'{-è`_\\ç^à@=}^¨$£¤%ùµ*?.;/:§!<>")).isEqualTo(
                new PlatformVersion("1.0.0-SPECIALS²&É~\"#'{-È`_\\Ç^À@=}^¨$£¤%Ùμ*?.;/:§!<>"));

    }

    @Test
    public void testInvalidVersionsExceptions() {
        assertThatIllegalArgumentException().isThrownBy(() -> new PlatformVersion(null))
                                            .withMessage("Version cannot be blank");
        assertThatIllegalArgumentException().isThrownBy(() -> new PlatformVersion(""))
                                            .withMessage("Version cannot be blank");
        assertThatIllegalArgumentException().isThrownBy(() -> new PlatformVersion("   "))
                                            .withMessage("Version cannot be blank");
        assertThatIllegalArgumentException().isThrownBy(() -> new PlatformVersion("1,")) //
                                            .withMessage(
                                                    "Version cannot contain commas (','), brackets ('[]') or parenthesis ('()'): 1,");
        assertThatIllegalArgumentException().isThrownBy(() -> new PlatformVersion("[1]")) //
                                            .withMessage(
                                                    "Version cannot contain commas (','), brackets ('[]') or parenthesis ('()'): [1]");
        assertThatIllegalArgumentException().isThrownBy(() -> new PlatformVersion("(1)")) //
                                            .withMessage(
                                                    "Version cannot contain commas (','), brackets ('[]') or parenthesis ('()'): (1)");
        assertThatIllegalArgumentException().isThrownBy(() -> new PlatformVersion("qualifierOnly"))
                                            .withMessage(
                                                    "Version should at least explicit a major number: qualifierOnly");
        assertThatIllegalArgumentException().isThrownBy(() -> new PlatformVersion("-qualifierOnly"))
                                            .withMessage(
                                                    "Version should at least explicit a major number: -qualifierOnly");
        assertThatIllegalArgumentException().isThrownBy(() -> new PlatformVersion("1-qualifier with space"))
                                            .withMessage(
                                                    "Version cannot contain whitespaces in qualifier: '1-qualifier with space'");
    }
}
