/*
 * Copyright 2015-2018 the original author or authors
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package net.javaru.iip.frc.wpilib.version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import net.javaru.iip.frc.wpilib.version.WpiLibVersion.PreReleaseModifier;

import static org.junit.jupiter.api.Assertions.*;



class WpiLibVersionTest
{


    @ParameterizedTest
    @MethodSource("parseProvider")
    void parse(WpiLibVersion expected, WpiLibVersion actual)
    {
        assertAll(
            () -> assertEquals(expected.getGeneration(), actual.getGeneration(), "Generation"),
            () -> assertEquals(expected.getMajor(), actual.getMajor(), "Major"),
            () -> assertEquals(expected.getMinor(), actual.getMinor(), "Minor"),
            () -> assertEquals(expected.getPatch(), actual.getPatch(), "Path"),
            () -> assertEquals(expected.getPreReleaseModifier(), actual.getPreReleaseModifier(), "PreReleaseModifier"),
            () -> assertEquals(expected.getPreReleaseModifierVersion(), actual.getPreReleaseModifierVersion(), "PreReleaseModifierVersion")
        );
    }



    @RepeatedTest(5)
    void compareTo()
    {
        List<WpiLibVersion> list = new ArrayList<>(versionList);
        Collections.shuffle(list);
        Collections.sort(list);
        assertEquals(versionList, list, "Sorted list is not equal to expected");
    }


    @ParameterizedTest
    @MethodSource("isNewerThanProvider")
    void isNewerThan(WpiLibVersion x, WpiLibVersion y, boolean expectedIsXNewerThanY)
    {
        final boolean actual = x.isNewerThan(y);
        assertEquals(expectedIsXNewerThanY,
                     actual,
                     "" + x.getVersionString() + ".isNewerThan(" + y.getVersionString() + ") returned " + actual + " but should have been "
                     + expectedIsXNewerThanY);
    }


    @ParameterizedTest
    @MethodSource("isOlderThanProvider")
    void isOlderThan(WpiLibVersion x, WpiLibVersion y, boolean expectedIsXOlderThanY)
    {
        final boolean actual = x.isOlderThan(y);
        assertEquals(expectedIsXOlderThanY,
                     actual,
                     "" + x.getVersionString() + ".isOlderThan(" + y.getVersionString() + ") returned " + actual + " but should have been "
                     + expectedIsXOlderThanY);
    }


    static Iterable<Arguments> isNewerThanProvider()
    {
        return createArgsList(false, true);
    }

    static Iterable<Arguments> isOlderThanProvider()
    {
        return createArgsList(true, false);
    }


    @NotNull
    private static List<Arguments> createArgsList(boolean oldToNew, boolean newToOld)
    {
        List<Arguments> args = new ArrayList<>();

        for (int i = 0; i < versionList.size() - 1; i++)
        {
            final WpiLibVersion olderVer = versionList.get(i);
            final WpiLibVersion newerVer = versionList.get(i + 1);
            args.add(Arguments.of(olderVer, newerVer, oldToNew));
            args.add(Arguments.of(newerVer, olderVer, newToOld));
        }


        for (int i = 0; i < versionList.size() - 2; i+=2)
        {
            final WpiLibVersion olderVer = versionList.get(i);
            final WpiLibVersion newerVer = versionList.get(i + 2);
            args.add(Arguments.of(olderVer, newerVer, oldToNew));
            args.add(Arguments.of(newerVer, olderVer, newToOld));
        }

        for (int i = 0; i < versionList.size() - 3; i+=3)
        {
            final WpiLibVersion olderVer = versionList.get(i);
            final WpiLibVersion newerVer = versionList.get(i + 3);
            args.add(Arguments.of(olderVer, newerVer, oldToNew));
            args.add(Arguments.of(newerVer, olderVer, newToOld));
        }

        for (int i = versionList.size() - 1; i > 1; i--)
        {
            final WpiLibVersion olderVer = versionList.get(i - 1);
            final WpiLibVersion newerVer = versionList.get(i);
            args.add(Arguments.of(olderVer, newerVer, oldToNew));
            args.add(Arguments.of(newerVer, olderVer, newToOld));
        }
        return args;
    }


    static Iterable<Arguments> parseProvider()
    {
        List<Arguments> args = new ArrayList<>();
        WpiLibVersion expected;
        WpiLibVersion actual;

        expected = new ExpectedWpiLibVersion(2017, 2018, 1, 2, PreReleaseModifier.alpha, 5);
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-alpha-5")));
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.alpha-5")));
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-alpha.5")));
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.alpha.5")));

        expected = new ExpectedWpiLibVersion(2017, 2018, 1, 2, PreReleaseModifier.beta, 5);
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta-5")));
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta-5")));
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta.5")));
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta.5")));

        expected = new ExpectedWpiLibVersion(2017, 2018, 1, 2, PreReleaseModifier.rc, 5);
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-rc-5")));
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.rc-5")));
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-rc.5")));
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.rc.5")));

        expected = new ExpectedWpiLibVersion(2017, 2018, 1, 2, PreReleaseModifier.beta, null);
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta")));
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta")));
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta")));
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta")));

        expected = new ExpectedWpiLibVersion(2017, 2018, 1, 2, null, null);
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2")));

        expected = new ExpectedWpiLibVersion(2017, 2018, 1, 0, null, null);
        args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1")));


        return args;
    }


    static class ExpectedWpiLibVersion implements WpiLibVersion
    {
        private final int generation;
        private final int major;
        private final int minor;
        private final int patch;
        @Nullable
        private final PreReleaseModifier preReleaseModifier;

        @Nullable
        private final Integer preReleaseModifierVersion;


        public ExpectedWpiLibVersion(int generation,
                                     int major,
                                     int minor,
                                     int patch,
                                     @Nullable PreReleaseModifier preReleaseModifier,
                                     @Nullable Integer preReleaseModifierVersion)
        {
            this.generation = generation;
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.preReleaseModifier = preReleaseModifier;
            this.preReleaseModifierVersion = preReleaseModifierVersion;
        }


        @Override
        public int getGeneration() { return generation; }


        @Override
        public String getVersionString() { throw new UnsupportedOperationException("Not supported fo test impl");}


        @Override
        public int getMajor() { return major; }


        @Override
        public int getMinor() { return minor; }


        @Override
        public int getPatch() { return patch; }


        @Nullable
        @Override
        public PreReleaseModifier getPreReleaseModifier() { return preReleaseModifier; }


        @Nullable
        @Override
        public Integer getPreReleaseModifierVersion() { return preReleaseModifierVersion; }
    }

    private static final WpiLibVersion v15 = WpiLibVersionImpl.parse("0.1.0.201502241928");
    private static final WpiLibVersion v16_02 = WpiLibVersionImpl.parse("0.1.0.201602112135");
    private static final WpiLibVersion v16_03 = WpiLibVersionImpl.parse("0.1.0.201603020231");
    private static final WpiLibVersion v2017_1_1_alpha_1 = WpiLibVersionImpl.parse("2017.1.1.alpha-1");
    private static final WpiLibVersion v2017_1_1_alpha_2 = WpiLibVersionImpl.parse("2017.1.1.alpha-2");
    private static final WpiLibVersion v2017_1_1_beta_1 = WpiLibVersionImpl.parse("2017.1.1.beta-1");
    private static final WpiLibVersion v2017_1_1_beta_2 = WpiLibVersionImpl.parse("2017.1.1.beta-2");
    private static final WpiLibVersion v2017_1_1_beta_3 = WpiLibVersionImpl.parse("2017.1.1.beta-3");
    private static final WpiLibVersion v2017_1_1_rc_1 = WpiLibVersionImpl.parse("2017.1.1.rc-1");
    private static final WpiLibVersion v2017_1_1_rc_2 = WpiLibVersionImpl.parse("2017.1.1.rc-2");
    private static final WpiLibVersion v2017_1_1 = WpiLibVersionImpl.parse("2017.1.1");
    private static final WpiLibVersion v2017_1_2_rc_1 = WpiLibVersionImpl.parse("2017.1.2.rc-1");
    private static final WpiLibVersion v2017_1_2 = WpiLibVersionImpl.parse("2017.1.2");
    private static final WpiLibVersion v2017_2_1_beta_1 = WpiLibVersionImpl.parse("2017.2.1.beta-1");
    private static final WpiLibVersion v2017_2_1_rc_1 = WpiLibVersionImpl.parse("2017.2.1.rc-1");
    private static final WpiLibVersion v2017_2_1_rc_2 = WpiLibVersionImpl.parse("2017.2.1.rc-2");
    private static final WpiLibVersion v2017_2_1 = WpiLibVersionImpl.parse("2017.2.1");
    private static final WpiLibVersion v2018_1_1_alpha_1 = WpiLibVersionImpl.parse("2018.1.1.alpha-1");
    private static final WpiLibVersion v2018_1_1_alpha_2 = WpiLibVersionImpl.parse("2018.1.1.alpha-2");
    private static final WpiLibVersion v2018_1_1_alpha_3 = WpiLibVersionImpl.parse("2018.1.1.alpha-3");
    private static final WpiLibVersion v2018_1_1_alpha_4 = WpiLibVersionImpl.parse("2018.1.1.alpha-4");
    private static final WpiLibVersion v2018_1_1_alpha_5 = WpiLibVersionImpl.parse("2018.1.1.alpha-5");
    private static final WpiLibVersion v2018_1_1_alpha_6 = WpiLibVersionImpl.parse("2018.1.1.alpha-6");
    private static final WpiLibVersion v2018_1_1_beta_1 = WpiLibVersionImpl.parse("2018.1.1.beta-1");
    private static final WpiLibVersion v2018_1_1_beta_2 = WpiLibVersionImpl.parse("2018.1.1.beta-2");
    private static final WpiLibVersion v2018_1_1_beta_3 = WpiLibVersionImpl.parse("2018.1.1.beta-3");
    private static final WpiLibVersion v2018_1_1_beta_4 = WpiLibVersionImpl.parse("2018.1.1.beta-4");
    private static final WpiLibVersion v2018_1_1_beta_5 = WpiLibVersionImpl.parse("2018.1.1.beta-5");
    private static final WpiLibVersion v2018_1_1_beta_6 = WpiLibVersionImpl.parse("2018.1.1.beta-6");
    private static final WpiLibVersion v2018_1_1_rc_1 = WpiLibVersionImpl.parse("2018.1.1.rc-1");
    private static final WpiLibVersion v2018_1_1_rc_2 = WpiLibVersionImpl.parse("2018.1.1.rc-2");
    private static final WpiLibVersion v2018_1_1_rc_3 = WpiLibVersionImpl.parse("2018.1.1.rc-3");
    private static final WpiLibVersion v2018_1_1_rc_4 = WpiLibVersionImpl.parse("2018.1.1.rc-4");
    private static final WpiLibVersion v2018_1_1_rc_5 = WpiLibVersionImpl.parse("2018.1.1.rc-5");
    private static final WpiLibVersion v2018_1_1_rc_6 = WpiLibVersionImpl.parse("2018.1.1.rc-6");
    private static final WpiLibVersion v2018_1_1 = WpiLibVersionImpl.parse("2018.1.1");
    private static final WpiLibVersion v2018_1_2 = WpiLibVersionImpl.parse("2018.1.2");
    private static final WpiLibVersion v2018_1_3 = WpiLibVersionImpl.parse("2018.1.3");
    private static final WpiLibVersion v2018_1_4 = WpiLibVersionImpl.parse("2018.1.4");
    private static final WpiLibVersion v2018_1_5 = WpiLibVersionImpl.parse("2018.1.5");
    private static final WpiLibVersion v2018_2_0 = WpiLibVersionImpl.parse("2018.2.0");
    private static final WpiLibVersion v2018_2_1 = WpiLibVersionImpl.parse("2018.2.1");
    private static final WpiLibVersion v2018_2_2 = WpiLibVersionImpl.parse("2018.2.2");
    private static final WpiLibVersion v2018_2_3 = WpiLibVersionImpl.parse("2018.2.3");
    private static final WpiLibVersion v2018_2_4 = WpiLibVersionImpl.parse("2018.2.4");
    private static final WpiLibVersion v2018_2_5 = WpiLibVersionImpl.parse("2018.2.5");

    private static final ImmutableList<WpiLibVersion> versionList;

    static
    {
        final Builder<WpiLibVersion> list = ImmutableList.builder();

        list.add(v15);
        list.add(v16_02);
        list.add(v16_03);
        list.add(v2017_1_1_alpha_1);
        list.add(v2017_1_1_alpha_2);
        list.add(v2017_1_1_beta_1);
        list.add(v2017_1_1_beta_2);
        list.add(v2017_1_1_beta_3);
        list.add(v2017_1_1_rc_1);
        list.add(v2017_1_1_rc_2);
        list.add(v2017_1_1);
        list.add(v2017_1_2_rc_1);
        list.add(v2017_1_2);
        list.add(v2017_2_1_beta_1);
        list.add(v2017_2_1_rc_1);
        list.add(v2017_2_1_rc_2);
        list.add(v2017_2_1);
        list.add(v2018_1_1_alpha_1);
        list.add(v2018_1_1_alpha_2);
        list.add(v2018_1_1_alpha_3);
        list.add(v2018_1_1_alpha_4);
        list.add(v2018_1_1_alpha_5);
        list.add(v2018_1_1_alpha_6);
        list.add(v2018_1_1_beta_1);
        list.add(v2018_1_1_beta_2);
        list.add(v2018_1_1_beta_3);
        list.add(v2018_1_1_beta_4);
        list.add(v2018_1_1_beta_5);
        list.add(v2018_1_1_beta_6);
        list.add(v2018_1_1_rc_1);
        list.add(v2018_1_1_rc_2);
        list.add(v2018_1_1_rc_3);
        list.add(v2018_1_1_rc_4);
        list.add(v2018_1_1_rc_5);
        list.add(v2018_1_1_rc_6);
        list.add(v2018_1_1);
        list.add(v2018_1_2);
        list.add(v2018_1_3);
        list.add(v2018_1_4);
        list.add(v2018_1_5);
        list.add(v2018_2_0);
        list.add(v2018_2_1);
        list.add(v2018_2_2);
        list.add(v2018_2_3);
        list.add(v2018_2_4);
        list.add(v2018_2_5);

        versionList = list.build();
    }
}