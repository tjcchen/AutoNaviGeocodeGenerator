package com.tjcchen.coordinates;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;
import com.google.common.base.Optional;

/**
 * A domestic geocodes generator of AutoNavi map
 * <p>
 * Usage: please refer to src/Applications/com/tjcchen/coordinates/CoordinatesEngine.java
 *
 * <ul class="references">
 *   <li class="documentation">Guidance: http://lbs.amap.com/api/webservice/guide/api/georegeo/</li>
 *   <li class="verification">Geocode Picker: http://lbs.amap.com/console/show/picker</li>
 * </ul>
 *
 * @author yangchen
 * @email tjcchen.engineer@gmail.com
 * @since 09/08/2016
 */
public class AutoNaviGeocodeGenerator extends GeocodeGenerator
{
    private static final String S_GEOCODING_API_ROOT = "http://restapi.amap.com/v3/geocode/geo";
    private static final String S_REST_API_KEY       = "8325164e247e15eea68b59e89200988b";

    /**
     * Process DB location elements to custom geocode locations
     *
     * @return return geocode locations, each one contains lat and lng
     */
    @Nonnull
    @Override
    public Collection<GeocodeLocation> process(@CheckForNull Collection<DBLocationElement> locations)
    {
        if (BaseUtils.isEmptySafe(locations))
        {
            LOGGER.error("Empty location list, failed to write file.");
            return Collections.emptyList();
        }

        return locations.stream()
                        .map(loc -> _processSingle(loc))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
    }

    /**
     * Convert Geocoding API response json result to coordinates pair
     *
     * @return return the coordinates pair
     */
    @CheckForNull
    private String[] _JsonToCoords(@CheckForNull final JsonObject jObj) throws Exception
    {
        if (jObj == null) return null;

        JsonArray jArr = jObj.getAsJsonArray("geocodes");
        if (jArr == null || jArr.size() == 0)
        {
            return null;
        }

        JsonObject subObj = jArr.get(0).getAsJsonObject();

        // usually the api result coordinates format like "118.070260,24.444943"
        String[] coords = subObj.get("location").getAsString().split(",");
        if (coords.length == 2)
        {
            return coords;
        }

        return null;
    }

    /**
     * Build geocoding api request url with city_name, address_name and location_name
     *
     * Example: http://restapi.amap.com/v3/geocode/geo?key=map_key&city=city_name&address=address_name
     * @return return the request url
     */
    private String _buildRequestUrl(String cityName, String addressName, String locationName)
    {
        addressName = (addressName + locationName).replaceAll("\\s+", "").trim();

        return new StringBuilder().append(S_GEOCODING_API_ROOT)
                                  .append("?key=")
                                  .append(S_REST_API_KEY)
                                  .append("&city=")
                                  .append(cityName)
                                  .append("&address=")
                                  .append(addressName).toString();
    }

    /**
     * Process single coordinates geocode conversion
     *
     * @param le db location element
     * @return optional GeocodeLocation object
     */
    private Optional<GeocodeLocation> _processSingle(@CheckForNull final DBLocationElement le)
    {
        if (le == null)
        {
            return Optional.absent();
        }

        GeocodeLocation location = null;
        String sRequestUrl = _buildRequestUrl(le.getCityName(), le.getPlainTextChineseAddress(), le.getName(Locale.CHINA));

        URL url;
        HttpURLConnection conn;
        InputStreamReader streamReader = null;
        BufferedReader bufferedReader = null;
        try
        {
            url = new URL(sRequestUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setReadTimeout(10000);

            streamReader = new InputStreamReader(conn.getInputStream());
            bufferedReader = new BufferedReader(streamReader);

            // parse geocoding api json result
            JsonObject jsonObj = new JsonParser().parse(bufferedReader).getAsJsonObject();
            String[] coords = _JsonToCoords(jsonObj);

            location = new GeocodeLocation.Builder(le.getId())
                                          .withLocName(le.getName(Locale.CHINA))
                                          .withAddressName(le.getPlainTextChineseAddress())
                                          .withCityName(le.getCityName())
                                          .withLongitude(BaseUtils.parseFloat(coords[0]))
                                          .withLatitude(BaseUtils.parseFloat(coords[1]))
                                          .build();
        }
        catch (Exception ex)
        {
            LOGGER.error("An error encountered when retrieving location coordinates pair from AutoNavi Geocoding API. Exception message: " + ex);
        }
        finally
        {
            Utils.safeClose(streamReader);
            Utils.safeClose(bufferedReader);
        }

        return Optional.fromNullable(location);
    }
}
