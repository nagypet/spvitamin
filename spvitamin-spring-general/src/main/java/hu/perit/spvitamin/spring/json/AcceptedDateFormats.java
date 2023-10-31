/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.spring.json;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
 *
 *    Symbol  Meaning                     Presentation      Examples
 *    ------  -------                     ------------      -------
 *    G       era                         text              AD; Anno Domini; A
 *    u       year                        year              2004; 04
 *    y       year-of-era                 year              2004; 04
 *    D       day-of-year                 number            189
 *    M/L     month-of-year               number/text       7; 07; Jul; July; J
 *    d       day-of-month                number            10
 *
 *    Q/q     quarter-of-year             number/text       3; 03; Q3; 3rd quarter
 *    Y       week-based-year             year              1996; 96
 *    w       week-of-week-based-year     number            27
 *    W       week-of-month               number            4
 *    E       day-of-week                 text              Tue; Tuesday; T
 *    e/c     localized day-of-week       number/text       2; 02; Tue; Tuesday; T
 *    F       week-of-month               number            3
 *
 *    a       am-pm-of-day                text              PM
 *    h       clock-hour-of-am-pm (1-12)  number            12
 *    K       hour-of-am-pm (0-11)        number            0
 *    k       clock-hour-of-am-pm (1-24)  number            0
 *
 *    H       hour-of-day (0-23)          number            0
 *    m       minute-of-hour              number            30
 *    s       second-of-minute            number            55
 *    S       fraction-of-second          fraction          978
 *    A       milli-of-day                number            1234
 *    n       nano-of-second              number            987654321
 *    N       nano-of-day                 number            1234000000
 *
 *    V       time-zone ID                zone-id           America/Los_Angeles; Z; -08:30
 *    z       time-zone name              zone-name         Pacific Standard Time; PST
 *    O       localized zone-offset       offset-O          GMT+8; GMT+08:00; UTC-08:00;
 *    X       zone-offset 'Z' for zero    offset-X          Z; -08; -0830; -08:30; -083015; -08:30:15;
 *    x       zone-offset                 offset-x          +0000; -08; -0830; -08:30; -083015; -08:30:15;
 *    Z       zone-offset                 offset-Z          +0000; -0800; -08:00;
 *
 *    p       pad next                    pad modifier      1
 *
 *    '       escape for text             delimiter
 *    ''      single quote                literal           '
 *    [       optional section start
 *    ]       optional section end
 *    #       reserved for future use
 *    {       reserved for future use
 *    }       reserved for future use
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class AcceptedDateFormats
{
    private static final List<String> DATE_FORMATS;
    private static final List<String> LOCAL_DATE_FORMATS;

    static
    {
        List<String> formats = new ArrayList<>();
        // Without T
        formats.add("yyyy-MM-dd HH:mm:ss.SSS");
        formats.add("yyyy-MM-dd HH:mm:ss");
        formats.add("yyyy-MM-dd HH:mm");
        formats.add("yyyy-MM-dd");
        // With T
        formats.add("yyyy-MM-dd'T'HH:mm:ss.SSS");
        formats.add("yyyy-MM-dd'T'HH:mm:ss");
        formats.add("yyyy-MM-dd'T'HH:mm");
        DATE_FORMATS = formats;

        // With timezone
        DATE_FORMATS.addAll(formats.stream().filter(i -> i.length() > 10).map(i -> i + "Z").toList());
        DATE_FORMATS.addAll(formats.stream().filter(i -> i.length() > 10).map(i -> i + "XXX").toList());

        List<String> localDateFormats = new ArrayList<>();
        localDateFormats.add("yyyy-MM-dd HH:mm:ss.nnnnnnnnn");
        localDateFormats.add("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn");
        LOCAL_DATE_FORMATS = new ArrayList<>();
        LOCAL_DATE_FORMATS.addAll(localDateFormats);

        // With timezone
        LOCAL_DATE_FORMATS.addAll(localDateFormats.stream().filter(i -> i.length() > 10).map(i -> i + "Z").toList());
        LOCAL_DATE_FORMATS.addAll(localDateFormats.stream().filter(i -> i.length() > 10).map(i -> i + "XXX").toList());

        LOCAL_DATE_FORMATS.addAll(formats);
    }

    public static List<String> getAcceptedDateFormats()
    {
        return Collections.unmodifiableList(DATE_FORMATS);
    }


    public static List<String> getAcceptedLocalDateFormats()
    {
        return Collections.unmodifiableList(LOCAL_DATE_FORMATS);
    }
}
