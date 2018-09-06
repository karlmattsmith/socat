package gov.noaa.pmel.sdimetadata.variable;

import gov.noaa.pmel.sdimetadata.util.NumericString;

/**
 * Information about an air pressure measurement.
 * The unit for accuracy and precision are set to hectopascals and cannot be modified,
 * and the default unit for the variable is hectopascals (but can be modified).
 */
public class AirPressure extends DataVar implements Cloneable {

    public static final String HECTOPASCALS_UNIT = "hPa";

    protected String pressureCorrection;

    public AirPressure() {
        super();
        varUnit = HECTOPASCALS_UNIT;
        accuracy.setUnitString(HECTOPASCALS_UNIT);
        precision.setUnitString(HECTOPASCALS_UNIT);
        pressureCorrection = "";
    }

    /**
     * @return the pressure correction information; never null but may be empty
     */
    public String getPressureCorrection() {
        return pressureCorrection;
    }

    /**
     * @param pressureCorrection
     *         assign as the pressure correction string; if null, and empty string is assigned
     */
    public void setPressureCorrection(String pressureCorrection) {
        this.pressureCorrection = (pressureCorrection != null) ? pressureCorrection.trim() : "";
    }

    /**
     * @param varUnit
     *         assign as the unit for values of this variable; if null or blank, hectopascals is assigned
     */
    @Override
    public void setVarUnit(String varUnit) {
        this.varUnit = (varUnit != null) ? varUnit.trim() : HECTOPASCALS_UNIT;
        if ( this.varUnit.isEmpty() )
            this.varUnit = HECTOPASCALS_UNIT;
    }

    /**
     * @apiNote also throws IllegalArgumentException if the unit string is not {@link AirPressure#HECTOPASCALS_UNIT}
     */
    @Override
    public void setAccuracy(NumericString accuracy) throws IllegalArgumentException {
        if ( accuracy != null ) {
            if ( !HECTOPASCALS_UNIT.equals(accuracy.getUnitString()) )
                throw new IllegalArgumentException("unit of accuracy is not " + HECTOPASCALS_UNIT);
            super.setAccuracy(accuracy);
        }
        else
            this.accuracy = new NumericString(null, HECTOPASCALS_UNIT);
    }

    /**
     * @apiNote also throws IllegalArgumentException if the unit string is not {@link AirPressure#HECTOPASCALS_UNIT}
     */
    @Override
    public void setPrecision(NumericString precision) throws IllegalArgumentException {
        if ( precision != null ) {
            if ( !HECTOPASCALS_UNIT.equals(precision.getUnitString()) )
                throw new IllegalArgumentException("unit of precision is not " + HECTOPASCALS_UNIT);
            super.setPrecision(precision);
        }
        else
            this.precision = new NumericString(null, HECTOPASCALS_UNIT);
    }

    @Override
    public AirPressure clone() {
        AirPressure dup = (AirPressure) super.clone();
        dup.pressureCorrection = pressureCorrection;
        return dup;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( null == obj )
            return false;
        if ( !(obj instanceof AirPressure) )
            return false;
        if ( !super.equals(obj) )
            return false;

        AirPressure other = (AirPressure) obj;

        if ( !pressureCorrection.equals(other.pressureCorrection) )
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 37;
        int result = super.hashCode();
        result = result * prime + pressureCorrection.hashCode();
        return result;
    }

    @Override
    public String toString() {
        String repr = super.toString().replaceFirst("DataVar", "AirPressure");
        return repr.substring(0, repr.length() - 1) +
                ", pressureCorrection='" + pressureCorrection + "'}";
    }

}