package org.litesoft.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.litesoft.utils.GregorianMonth.*;

class GregorianMonthTest {

    @Test
    void _isLeapYear() {
        assertFalse( isLeapYear( 0 ) ); // Doesn't exist...

        // BCE years
        assertFalse( isLeapYear( -1 ) );
        assertFalse( isLeapYear( Integer.MIN_VALUE ) );

        assertFalse( isLeapYear( 1900 ) );
        assertTrue( isLeapYear( 2000 ) );

        assertEquals( 28, FEB.getDaysInMonth( 1900 ) );
        assertEquals( 29, FEB.getDaysInMonth( 2000 ) );

        Set<Integer> leapYears = new HashSet<>();
        checkAndAddLeapYear( leapYears, false, 0, 100, 200, 300, 500, 600, 700, 900 ); // - 999
        checkAndAddLeapYear( leapYears, false, 1000, 1100, 1300, 1400, 1500, 1700, 1800, 1900 ); // - 1999
        checkAndAddLeapYear( leapYears, true, 2000, 2100, 2200, 2300, 2500, 2600, 2700, 2900 ); // - 2999
        checkAndAddLeapYear( leapYears, false, 3000, 3100, 3300, 3400, 3500, 3700, 3800, 3900 ); // - 3999

        for ( int year = 1; year < 4000; year++ ) {
            if ( !leapYears.contains( year ) ) {
                checkNotLeapYear( year );
            }
        }
    }

    private void checkNotLeapYear( int year ) {
        assertFalse( isLeapYear( year ), () -> "expect " + year + " to NOT be a leap year" );
    }

    private void checkLeapYear( Set<Integer> leapYears, int year ) {
        assertTrue( isLeapYear( year ), () -> "expect " + year + " to BE a leap year" );
        leapYears.add( year );
    }

    private void checkEveryFourYearsIsLeapYears( Set<Integer> leapYears, int fromYear, int toYear ) {
        for ( int year = fromYear + 4; year < toYear; year += 4 ) {
            checkLeapYear( leapYears, year );
        }
    }

    private void checkAndAddLeapYear( Set<Integer> leapYears, boolean kYearsFromIsLeapYear, int kYearsFrom, int... nonLeapYearCenturies ) {
        if ( kYearsFromIsLeapYear ) {
            checkLeapYear( leapYears, kYearsFrom );
        }
        int from = kYearsFrom;
        for ( int toDontCheckYear : nonLeapYearCenturies ) {
            checkEveryFourYearsIsLeapYears( leapYears, from, toDontCheckYear );
            from = toDontCheckYear;
        }
        checkEveryFourYearsIsLeapYears( leapYears, from, kYearsFrom + 1000 );
    }

    @Test
    void _getMonthNumber() {
        assertEquals( 1, JAN.getMonthNumber() );
        assertEquals( 12, DEC.getMonthNumber() );
        GregorianMonth[] months = values(); // defined Order
        assertEquals( 12, months.length );
        for ( int i = 0; i < months.length; ) {
            GregorianMonth month = months[i];
            i++;
            assertEquals( i, month.getMonthNumber() );
        }
    }

    @Test
    void _checkOtherGetters() {
        checkOtherGetters( JAN, "JAN", "Jan", "JA", 31, "January", 1 );
        checkOtherGetters( FEB, "FEB", "Feb", "FE", 28, "February", 2 );
        checkOtherGetters( MAR, "MAR", "Mar", "MR", 31, "March", 3 );
        checkOtherGetters( APR, "APR", "Apr", "AP", 30, "April", 4 );
        checkOtherGetters( MAY, "MAY", "May", "MY", 31, "May", 5 );
        checkOtherGetters( JUN, "JUN", "Jun", "JN", 30, "June", 6 );
        checkOtherGetters( JUL, "JUL", "Jul", "JL", 31, "July", 7 );
        checkOtherGetters( AUG, "AUG", "Aug", "AU", 31, "August", 8 );
        checkOtherGetters( SEP, "SEP", "Sep", "SE", 30, "September", 9 );
        checkOtherGetters( OCT, "OCT", "Oct", "OC", 31, "October", 10 );
        checkOtherGetters( NOV, "NOV", "Nov", "NV", 30, "November", 11 );
        checkOtherGetters( DEC, "DEC", "Dec", "DE", 31, "December", 12 );
    }

    private void checkOtherGetters( GregorianMonth month, String upper3Abbrev, String camel3Abbrev, String twoLetterAbbrev, int nominalDays, String nameEnglish, int monthNumber ) {
        assertEquals( month, GregorianMonth.from( monthNumber ) );

        Supplier<String> msg = month::name;
        assertEquals( upper3Abbrev, month.getAbbreviationUppercase(), msg );
        assertEquals( camel3Abbrev, month.getAbbreviationCamelcase(), msg );
        assertEquals( twoLetterAbbrev, month.getTwoLetterAbbreviation(), msg );
        assertEquals( nominalDays, month.getNominalDaysInMonth(), msg );
        assertEquals( nameEnglish, month.getNameEnglish(), msg );
        assertEquals( monthNumber, month.getMonthNumber(), msg );

        if (month != FEB) {
            assertEquals( month.getNominalDaysInMonth(), month.getDaysInMonth( 1900 ), msg );
            assertEquals( month.getNominalDaysInMonth(), month.getDaysInMonth( 2000 ), msg );
        }

        assertEquals( month, GregorianMonth.from( monthNumber ) );
        assertEquals( month, GregorianMonth.fromThreeLetterCode( upper3Abbrev ) );
        assertEquals( month, GregorianMonth.fromThreeLetterCode( camel3Abbrev ) );
        assertEquals( month, GregorianMonth.fromThreeLetterCode( upper3Abbrev.toLowerCase() ) );
        assertEquals( month, GregorianMonth.fromTwoLetterCode( twoLetterAbbrev ) );
        assertEquals( month, GregorianMonth.fromTwoLetterCode( twoLetterAbbrev .toLowerCase()) );
        assertEquals( month, GregorianMonth.fromNameEnglish( nameEnglish ) );
        assertEquals( month, GregorianMonth.fromNameEnglish( nameEnglish.toUpperCase() ) );
        assertEquals( month, GregorianMonth.fromNameEnglish( nameEnglish.toLowerCase() ) );
    }
}