package org.litesoft.utils;

import java.util.function.Function;

/**
 * Gregorian Month(s) using enhanced enum to handle Februaries' Leap Day.
 * <p>
 * Also contains the lesser known <a href="https://eventguide.com/topics/two_digit_month_abbreviations.html">two-letter codes (or abbreviations)</a>.
 * <p>
 * Note: The two-letter codes always start with the first letter of the three-letter codes, which then leads to
 * conflicts if the 2nd or 3rd letter was always selected
 * <p>
 * Note2: Because of the varying times of the adoption of the Gregorian calendar (some countries have still not adopted,
 * as of mid 2022: Afghanistan, Iran, Ethiopia, and Nepal), Saudi Arabia only adopted it in 2016; if we limit ourselves to:
 * Africa, the Americas, Western Europe, and former British colonies in Asia and the Pacific (e.g. India & Australia); any
 * dates after 1767 (thanks Alaska) are contemporarily correct!
 */
@SuppressWarnings("unused")
public enum GregorianMonth {
    JAN( "JA", 31, "January" ), // re "JA": because "JN" is used for June
    FEB( "FE", 28, "February" ) { // I would have picked "FB" as the 'e' is soft

        @Override
        public int getDaysInMonth( int year ) {
            return getNominalDaysInMonth() + (isLeapYear( year ) ? 1 : 0);
        }
    }, // February has either 29 or 28 days based on if it is a Leap year or not
    MAR( "MR", 31, "March" ), // re "MR": because "MA" could be confused with May
    APR( "AP", 30, "April" ),
    MAY( "MY", 31, "May" ), // re "MY": because "MA" could have been March
    JUN( "JN", 30, "June" ), // re "JN": because "JU" could be July
    JUL( "JL", 31, "July" ), // re "JN": because "JU" could be June & "JY" could be January
    AUG( "AU", 31, "August" ), // I would have picked "AG" as the 'u' is soft
    SEP( "SE", 30, "September" ), // I would have picked "SP" as the 'e' is soft
    OCT( "OC", 31, "October" ),
    NOV( "NV", 30, "November" ), // "NO" was rejected to prevent any confusion with the word "no"
    DEC( "DE", 31, "December" );

    private final String twoLetterAbbreviation;
    private final int nominalDaysInMonth;
    private final String nameEnglish;

    GregorianMonth( String twoLetterAbbreviation, int nominalDaysInMonth, String nameEnglish ) {
        this.twoLetterAbbreviation = twoLetterAbbreviation;
        this.nominalDaysInMonth = nominalDaysInMonth;
        this.nameEnglish = nameEnglish;
    }

    /**
     * One based month numbers
     *
     * @return 1-12
     */
    public int getMonthNumber() {
        return ordinal() + 1;
    }

    public String getAbbreviationCamelcase() {
        String abbreviation = getAbbreviationUppercase();
        return abbreviation.charAt( 0 ) + abbreviation.substring( 1 ).toLowerCase();
    }

    public String getAbbreviationUppercase() {
        return name();
    }

    public String getTwoLetterAbbreviation() {
        return twoLetterAbbreviation;
    }

    public int getNominalDaysInMonth() {
        return nominalDaysInMonth;
    }

    public String getNameEnglish() {
        return nameEnglish;
    }

    public int getDaysInMonth( int year ) {
        return getNominalDaysInMonth();
    }

    /**
     * Get an instance based on a 1 based month number or an exception.
     *
     * @param month1_12 month number expected to be from 1 thru 12
     * @throws IllegalArgumentException if <code>month1_12</code> is not 1 - 12!
     */
    public static GregorianMonth from( int month1_12 ) {
        if ( (month1_12 < 1) || (12 < month1_12) ) {
            throw new IllegalArgumentException( "expected month value of 1 thru 12, but got: " + month1_12 );
        }
        return values()[month1_12 - 1];
    }

    /**
     * Get an instance if Three Letter <code>code</code> is matched (ignoring case) OR a null if not matched.
     *
     * @param code to match
     */
    public static GregorianMonth fromThreeLetterCode( String code ) {
        return find( code, GregorianMonth::getAbbreviationUppercase );
    }

    /**
     * Get an instance if Two Letter <code>code</code> is matched (ignoring case) OR a null if not matched.
     *
     * @param code to match
     */
    public static GregorianMonth fromTwoLetterCode( String code ) {
        return find( code, GregorianMonth::getTwoLetterAbbreviation );
    }

    /**
     * Get an instance if name (english) is matched (ignoring case) OR a null if not matched.
     *
     * @param nameEnglish to match
     */
    public static GregorianMonth fromNameEnglish( String nameEnglish ) {
        return find( nameEnglish, GregorianMonth::getNameEnglish );
    }

    /**
     * Checks if <code>year</code> is a leap year -- assumes a Gregorian Calendar back to 1 CE (AD), years before 1 CE (AD) return false!
     * <p>
     * Note: There is no year 0, the Common Era (CE) starts with year 1, BCE (Before Common Era) ends at -1!
     * The beauty of deciding when an Era starts being sufficiently in the past, is that you don't have to worry about
     * messing up peoples lives by making it more difficult to determine someone's age when they were born BEFORE the Era started!
     * After all, everyone knows that (10 - -10) == 21! :)
     *
     * @param year to check
     * @return true if <code>year</code> is a non-negative leap year
     */
    public static boolean isLeapYear( int year ) {
        if ( year < 1 ) {
            return false;
        }
        // Rule 1: Is year dividable by 4 -> probably IS a leap year
        if ( (year & 3) != 0 ) { // year is NOT dividable by 4
            return false; // NOT a leap year
        }
        // Rule 2: (Exception to Rule 1) every 100 years (dividable by 100) -> probably NOT a leap year
        int century = year / 100;
        if ( year != (century * 100) ) { // there was a remainder -> not dividable by 100 (note: historically division was slower than multiplication)
            return true; // IS a leap year
        }
        // Rule 3: (Exception to Rule 2) every 400 years (dividable by 400) -> IS a leap year
        return ((century & 3) == 0);
    }

    private static GregorianMonth find( String toMatch, Function<GregorianMonth, String> valueExtractor ) {
        if ( toMatch != null ) {
            toMatch = toMatch.trim();
            if ( !toMatch.isEmpty() ) {
                for ( GregorianMonth month : values() ) {
                    String value = valueExtractor.apply( month );
                    if ( toMatch.equalsIgnoreCase( value ) ) {
                        return month;
                    }
                }
            }
        }
        return null; // not found!
    }
}
