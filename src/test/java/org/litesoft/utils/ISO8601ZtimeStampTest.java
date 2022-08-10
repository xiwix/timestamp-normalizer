package org.litesoft.utils;

import java.time.Instant;
import java.util.List;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.litesoft.utils.ISO8601ZtimeStamp.*;

class ISO8601ZtimeStampTest {
    private static final String TO_HOUR = "2011-01-16T13";
    private static final String TO_MIN = TO_HOUR + ":25";
    private static final String TO_SEC = TO_MIN + ":30";
    private static final String TO_MILLIS = TO_SEC + ".123";
    private static final String TO_MICROS = TO_MILLIS + "456";
    private static final String TO_NANOS = TO_MICROS + "789";

    private static final long MILLIS = Instant.parse( TO_MILLIS + "Z" ).toEpochMilli();
    private static final String NANOSTR = Instant.parse( TO_NANOS + "Z" ).toString();

    @Test
    void _fromEpochMillis() {
        check( fromEpochMillis( MILLIS ), TO_MILLIS + "Z" );
    }

    void check( ISO8601ZtimeStamp ts, String strFormZ ) {
        assertFalse( ts.hasError() );
        assertNull( ts.getError() );

        assertEquals( strFormZ, ts.getValue() );
    }

    @Test
    void happyCases_fromString() {
        check( TO_NANOS, ISO8601ZtimeStamp::toNanos );
        // Stepped expansion
        check( TO_MICROS, ISO8601ZtimeStamp::toMicros, "000" );
        check( TO_MILLIS, ISO8601ZtimeStamp::toMillis, "000", "000" );
        check( TO_SEC, ISO8601ZtimeStamp::toSecond, ".000", "000", "000" );
        check( TO_MIN, ISO8601ZtimeStamp::toMinute, ":00", ".000", "000", "000" );
        check( TO_HOUR, ISO8601ZtimeStamp::toHour, ":00", ":00", ".000", "000", "000" );

        // Bulk expansion
        check( fromString( TO_HOUR + "Z" ).toNanos(), TO_HOUR + ":00:00.000000000Z" ); //

        // Round Trip via Instant
        check( fromString( TO_NANOS + "Z" ), NANOSTR );

        // w/ Offsets
        check( fromString( "2011-12-31T23:35+0:30" ), "2012-01-01T00:05Z" );
        check( fromString( "2011-01-01T00:25-0:30" ), "2010-12-31T23:55Z" );

        // varius slopy forms
        check( fromString( "11-1-2T0:5:6.7Z" ), "0011-01-02T00:05:06.700Z" );
        check( fromString( "1970-01-01T00:05:06.78Z" ), "1970-01-01T00:05:06.780Z" );
        check( fromString( "1970-01-01T00:05:06.7809Z" ), "1970-01-01T00:05:06.780900Z" );
        check( fromString( "1970-01-01T00:05:06.78091Z" ), "1970-01-01T00:05:06.780910Z" );
        check( fromString( "1970-01-01T00:05:06.7809123Z" ), "1970-01-01T00:05:06.780912300Z" );
        check( fromString( "1970-01-01T00:05:06.78091234Z" ), "1970-01-01T00:05:06.780912340Z" );
    }

    private void check( String strForm, UnaryOperator<ISO8601ZtimeStamp> shrinker, String... stretcherStrings ) {
        ISO8601ZtimeStamp tsNS = fromString( TO_NANOS + "Z" );
        assertNull( tsNS.getError(), strForm );
        ISO8601ZtimeStamp tsShrunk = shrinker.apply( tsNS );
        assertNull( tsShrunk.getError(), strForm );
        ISO8601ZtimeStamp ts = fromString( strForm + "Z" );
        assertNull( ts.getError(), strForm );
        assertEquals( ts, tsShrunk, strForm );

        assertEquals( ts, fromString( strForm + "+00:00" ) );
        assertEquals( ts, fromString( strForm + "-00:00" ) );
        assertEquals( ts, fromString( strForm + "+00:0" ) );
        assertEquals( ts, fromString( strForm + "-00:0" ) );
        assertEquals( ts, fromString( strForm + "+0:00" ) );
        assertEquals( ts, fromString( strForm + "-0:00" ) );
        assertEquals( ts, fromString( strForm + "+00" ) );
        assertEquals( ts, fromString( strForm + "-00" ) );
        assertEquals( ts, fromString( strForm + "+0" ) );
        assertEquals( ts, fromString( strForm + "-0" ) );
        assertEquals( ts, fromString( strForm + "Z+11:59" ) );
        assertEquals( ts, fromString( strForm + "Z-11:59" ) );
        assertEquals( ts, fromString( strForm + "Z+1:59" ) );
        assertEquals( ts, fromString( strForm + "Z-1:59" ) );
        assertEquals( ts, fromString( strForm + "Z+11:9" ) );
        assertEquals( ts, fromString( strForm + "Z-11:9" ) );
        assertEquals( ts, fromString( strForm + "+0:00Z" ) );
        assertEquals( ts, fromString( strForm + "-00:0Z" ) );
        assertEquals( ts, fromString( strForm + "-0:0Z" ) );
        assertEquals( ts, fromString( strForm + "-0Z" ) );

        ISO8601ZtimeStamp shouldBeSame = shrinker.apply( ts );
        assertSame( ts, shouldBeSame, strForm );

        int listIndex = STRETCHERS.size() - stretcherStrings.length;
        for ( String str : stretcherStrings ) {
            ts = STRETCHERS.get( listIndex++ ).apply( ts );
            strForm += str;
            assertNull( ts.getError(), strForm );
            assertEquals( strForm + "Z", ts.getValue(), strForm );
        }
    }

    private static final List<UnaryOperator<ISO8601ZtimeStamp>> STRETCHERS = List.of(
            ISO8601ZtimeStamp::toMinute,
            ISO8601ZtimeStamp::toSecond,
            ISO8601ZtimeStamp::toMillis,
            ISO8601ZtimeStamp::toMicros,
            ISO8601ZtimeStamp::toNanos );

    @Test
    void unhappyCases_fromString() {
        checkExpectError( null, TO_PARSE_WAS_NULL );
        checkExpectError( "  ", TO_PARSE_WAS_EMPTY );
        checkExpectError( "2011-01-16 13Z", TO_PARSE_NO_T );

        checkExpectError( "2011-0x-16T13Z", INT_PARSE_ERROR );
        checkExpectError( "12011-01-16T13Z", INT_GT_MAX );
        checkExpectError( "2011-00-16T13Z", INT_LT_MIN );

        checkExpectError( "2011-01T13Z", DATE_NOT_3_FIELDS );
        checkExpectError( "2011-01-16-4T13Z", DATE_NOT_3_FIELDS );
        checkExpectError( "-2011-01-16T13Z", DATE_NOT_3_FIELDS );

        checkExpectError( "2011-01-16T13", TIME_NO_Z_OR_OFFSET );
        checkExpectError( "2011-01-16T13Z.", TIME_STUFF_AFTER_Z );
        checkExpectError( "2011-01-16T13+1:2:3", TIME_TOO_MANY_OFFSET_COLONS );
        checkExpectError( "2011-01-16T13+1+2", TIME_MULTIPLE_OFFSETS );
        checkExpectError( "2011-01-16T13+1-2", TIME_MULTIPLE_OFFSETS );
        checkExpectError( "2011-01-16T13-1-2", TIME_MULTIPLE_OFFSETS );
        checkExpectError( "2011-01-16T13-1+2", TIME_MULTIPLE_OFFSETS );
        checkExpectError( "2011-01-16T13-1:2", TIME_MINUTE_OFFSET_NOT_QUARTER_HOUR );
        checkExpectError( "2011-01-16T13:0:0:0Z", TIME_GT_3_FIELDS );
        checkExpectError( "2011-01-16T13:00:00.0123456789Z", TIME_FRACTIONAL_SECONDS_TOO_LONG );

        checkExpectError( "2011-01-16T13+5:45", TIME_NO_MINUTES_TO_ADJUST );

        checkExpectError( "9999-12-31T23+2", DATE_YEAR_ROLLED_GT_9999 ); // Role Over
        checkExpectError( "0001-01-01T00-2", DATE_YEAR_ROLLED_LT_1 ); // Role Under
    }

    private void checkExpectError( String input, String expectedErrorText ) {
        ISO8601ZtimeStamp ts = fromString( input );
        assertTrue( ts.hasError(), input );
        String error = ts.getError();
        if ( !error.contains( expectedErrorText ) ) {
            fail( "expected '" +
                  input +
                  "' input to generate error containing '" +
                  expectedErrorText +
                  "', but got:\n" + error );
        }
    }
}