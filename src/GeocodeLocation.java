package com.tjcchen.coordinates;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Model class for coordinates output file column fields
 *
 * @author yangchen
 * @email tjcchen.engineer@gmail.com
 * @since 09/08/2016
 */
public class GeocodeLocation
{
    private final int    m_locId;
    private final String m_locName;
    private final String m_cityName;
    private final String m_addressName;
    private final float  m_latitude;
    private final float  m_longitude;

    private GeocodeLocation(@Nonnull final Builder builder)
    {
        this.m_locId       = builder.m_locId;
        this.m_locName     = builder.m_locName;
        this.m_cityName    = builder.m_cityName;
        this.m_latitude    = builder.m_latitude;
        this.m_longitude   = builder.m_longitude;
        this.m_addressName = builder.m_addressName;
    }

    public int getId()
    {
        return m_locId;
    }

    public String getLocationName()
    {
        return m_locName;
    }

    public String getAddressName()
    {
        return m_addressName;
    }

    public String getCityName()
    {
        return m_cityName;
    }

    public float getLatitude()
    {
        return m_latitude;
    }

    public float getLongitude()
    {
        return m_longitude;
    }

    @Override
    public String toString()
    {
        return "{ locId : " + m_locId + ", locName : " + m_locName + ", addressName : " + m_addressName + ", " +
                "cityName : " + m_cityName + ", latitude : " + m_latitude + ", longitude : " + m_longitude + " }";
    }

    @Override
    public int hashCode()
    {
        return m_locId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;

        if (null == o || getClass() != o.getClass())
        {
            return false;
        }

        GeocodeLocation that = (GeocodeLocation) o;
        return that.getId() == this.getId();
    }

    public static class Builder
    {
        private int    m_locId;
        private String m_locName;
        private String m_cityName;
        private String m_addressName;
        private float  m_latitude;
        private float  m_longitude;

        public Builder(@Nonnegative int nLocId)
        {
            if (nLocId <= 0)
            {
                throw new IllegalArgumentException("[Error - GeocodeLocation]: locationId can't be zero or negative");
            }
            this.m_locId = nLocId;
        }

        public Builder withLocName(@Nonnull String sLocName)
        {
            this.m_locName = sLocName;
            return this;
        }

        public Builder withAddressName(@Nonnull String sAddressName)
        {
            this.m_addressName = sAddressName;
            return this;
        }

        public Builder withCityName(@Nonnull String sCityName)
        {
            this.m_cityName = sCityName;
            return this;
        }

        public Builder withLatitude(float fLatitude)
        {
            this.m_latitude = fLatitude;
            return this;
        }

        public Builder withLongitude(float fLongitude)
        {
            this.m_longitude = fLongitude;
            return this;
        }

        public GeocodeLocation build()
        {
            return new GeocodeLocation(this);
        }
    }
}
