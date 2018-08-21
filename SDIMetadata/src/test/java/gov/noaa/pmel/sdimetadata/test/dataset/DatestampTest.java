package gov.noaa.pmel.sdimetadata.test.dataset;

import gov.noaa.pmel.sdimetadata.dataset.Datestamp;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DatestampTest {

    private static final Integer YEAR = 2010;
    private static final Integer MONTH = 6;
    private static final Integer DAY = 25;
    private static final String DATESTAMP = "2010-06-25";
    private static final double TIME_OF_DATESTAMP = 1277424000.000;


    @Test
    public void testGetSetYear() {
        Datestamp datestamp = new Datestamp();
        assertEquals(Datestamp.INVALID, datestamp.getYear());
        datestamp.setYear(YEAR);
        assertEquals(YEAR, datestamp.getYear());
        datestamp.setYear(null);
        assertEquals(Datestamp.INVALID, datestamp.getYear());
    }

    @Test
    public void testGetSetMonth() {
        Datestamp datestamp = new Datestamp();
        assertEquals(Datestamp.INVALID, datestamp.getMonth());
        datestamp.setMonth(MONTH);
        assertEquals(MONTH, datestamp.getMonth());
        assertEquals(Datestamp.INVALID, datestamp.getYear());
        datestamp.setMonth(null);
        assertEquals(Datestamp.INVALID, datestamp.getMonth());
    }

    @Test
    public void testGetSetDay() {
        Datestamp datestamp = new Datestamp();
        assertEquals(Datestamp.INVALID, datestamp.getDay());
        datestamp.setDay(DAY);
        assertEquals(DAY, datestamp.getDay());
        assertEquals(Datestamp.INVALID, datestamp.getMonth());
        assertEquals(Datestamp.INVALID, datestamp.getYear());
        datestamp.setDay(null);
        assertEquals(Datestamp.INVALID, datestamp.getDay());
    }

    @Test
    public void testHashCodeEquals() {
        Datestamp first = new Datestamp();
        assertFalse(first.equals(null));
        assertFalse(first.equals(YEAR));

        Datestamp second = new Datestamp();
        assertEquals(first.hashCode(), second.hashCode());
        assertTrue(first.equals(second));

        first.setYear(YEAR);
        assertNotEquals(first.hashCode(), second.hashCode());
        assertFalse(first.equals(second));
        second.setYear(YEAR);
        assertEquals(first.hashCode(), second.hashCode());
        assertTrue(first.equals(second));

        first.setMonth(MONTH);
        assertNotEquals(first.hashCode(), second.hashCode());
        assertFalse(first.equals(second));
        second.setMonth(MONTH);
        assertEquals(first.hashCode(), second.hashCode());
        assertTrue(first.equals(second));

        first.setDay(DAY);
        assertNotEquals(first.hashCode(), second.hashCode());
        assertFalse(first.equals(second));
        second.setDay(DAY);
        assertEquals(first.hashCode(), second.hashCode());
        assertTrue(first.equals(second));
    }

    @Test
    public void testGetEarliestTime() {
        Datestamp datestamp = new Datestamp();
        try {
            datestamp.getEarliestTime();
            fail("getEarliestTime called on an empty datestamp did not throw an exception");
        } catch ( IllegalStateException ex ) {
            // expected result
        }
        datestamp.setYear(YEAR);
        datestamp.setMonth(MONTH);
        datestamp.setDay(DAY);
        assertEquals(TIME_OF_DATESTAMP, datestamp.getEarliestTime(), 0.1);
        datestamp.setYear(2011);
        datestamp.setMonth(2);
        datestamp.setDay(29);
        try {
            datestamp.getEarliestTime();
            fail("getEarliestTime called on an invalid datestamp did not throw an exception");
        } catch ( IllegalStateException ex ) {
            // expected result
        }
    }

    @Test
    public void testStampString() {
        Datestamp datestamp = new Datestamp();
        try {
            datestamp.stampString();
            fail("stampString called on an empty datestamp did not throw an exception");
        } catch ( IllegalStateException ex ) {
            // expected result
        }
        datestamp.setYear(YEAR);
        datestamp.setMonth(MONTH);
        datestamp.setDay(DAY);
        assertEquals(DATESTAMP, datestamp.stampString());
        datestamp.setYear(2011);
        datestamp.setMonth(2);
        datestamp.setDay(29);
        try {
            datestamp.stampString();
            fail("stampString called on an invalid datestamp did not throw an exception");
        } catch ( IllegalStateException ex ) {
            // expected result
        }
    }

    @Test
    public void testIsValid() {
        Datestamp datestamp = new Datestamp();
        assertFalse(datestamp.isValid());

        datestamp.setYear(YEAR);
        datestamp.setMonth(MONTH);
        datestamp.setDay(DAY);
        assertTrue(datestamp.isValid());

        datestamp.setYear(2011);
        datestamp.setMonth(2);
        datestamp.setDay(29);
        assertFalse(datestamp.isValid());
    }

    @Test
    public void setDatestamp() {
        Datestamp datestamp = new Datestamp(null, null, null);
        assertEquals(Datestamp.INVALID, datestamp.getYear());
        assertEquals(Datestamp.INVALID, datestamp.getMonth());
        assertEquals(Datestamp.INVALID, datestamp.getDay());

        datestamp = new Datestamp(YEAR, MONTH, DAY);
        assertEquals(YEAR, datestamp.getYear());
        assertEquals(MONTH, datestamp.getMonth());
        assertEquals(DAY, datestamp.getDay());
    }

    @Test
    public void testClone() {
        Datestamp datestamp = new Datestamp();
        Datestamp clone = datestamp.clone();
        assertEquals(datestamp, clone);
        assertNotSame(datestamp, clone);

        datestamp.setYear(YEAR);
        datestamp.setMonth(MONTH);
        datestamp.setDay(DAY);
        assertNotEquals(datestamp, clone);

        clone = datestamp.clone();
        assertEquals(datestamp, clone);
        assertNotSame(datestamp, clone);
    }

}

