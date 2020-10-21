package com.tjcchen.coordinates;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract class geocode generator for coordinates generation
 *
 * <p>
 * Note: Super class GeocodeGenerator will handle read and write file functions,
 * and its sub classes are compulsory to handle the process method respectively,
 * which transforms the DB location elements to custom geocode locations
 *
 * @author yangchen
 * @email tjcchen.engineer@gmail.com
 * @since 09/28/2016
 */
public abstract class GeocodeGenerator
{
    private static final Object[]  S_CSV_FILE_HEADER     =  { "locationId", "locationName", "addressName", "cityName", "longitude", "latitude" };
    private static final String    S_BOM_CODE            =  "\uFEFF";
    private static final String    S_COMMENT_LINE_START  =  "#";
    private static final String    S_CSV_FILE_DELIMITER  =  "\r\n";
    private static final int       S_ERROR_STATUS        =  1;
    private static final int       S_SUCCESS_STATUS      =  0;

    protected static final LoggerHelper LOGGER = Logging.APPLICATION;

    /**
     * Read locationId collections from input csv file
     *
     * @return return locationId collections
     */
    @Nonnull
    public Set<Integer> readFile(@CheckForNull final String inPath) throws Exception
    {
        Preconditions.checkNotNull(inPath);

        Set<Integer> locationIds= Files.lines(Paths.get(inPath))
                                       .filter(BaseStringUtils::isNotBlank)
                                       .map(line -> line.startsWith(S_BOM_CODE) ? line.replace(S_BOM_CODE, "") : line)
                                       .filter(line -> !line.startsWith(S_COMMENT_LINE_START))
                                       .map(String::trim)
                                       .map(BaseUtils::parseInt)
                                       .collect(Collectors.toSet());
        return locationIds;
    }

    /**
     * Write processed coordinates result to output file
     *
     * @return return the application exit status code
     */
    public int writeFile(@CheckForNull final String outPath, @CheckForNull Collection<GeocodeLocation> locations) throws Exception
    {
        if (StringUtils.isBlank(outPath) || BaseUtils.isEmptySafe(locations))
        {
            LOGGER.error("Empty output file path or location list, failed to write file.");
            return S_ERROR_STATUS;
        }

        CSVFormat format = CSVFormat.DEFAULT.withRecordSeparator(S_CSV_FILE_DELIMITER);
        int statusCode = S_ERROR_STATUS;

        try (FileWriter writer = new FileWriter(outPath);
             CSVPrinter out = new CSVPrinter(writer, format))
        {
            out.printRecord(S_CSV_FILE_HEADER);

            for(GeocodeLocation loc : locations)
            {
                // column fields - locationId, locationName, addressName, cityName, longitude, latitude
                out.printRecord(loc.getId(), loc.getLocationName(), loc.getAddressName(), loc.getCityName(), loc.getLongitude(), loc.getLatitude());
            }

            out.flush();
            statusCode = S_SUCCESS_STATUS;
        }
        catch (IOException ioe)
        {
            LOGGER.error(String.format("An error encountered when generating %s. Exception message: %s", outPath, ioe));
            throw ioe;
        }

        return statusCode;
    }

    /**
     * Geocode generator process method must be implemented by its sub class( AutoNavi, Bing or probable other map providers in the future ),
     * this method serves as the function of transforming DB location elements to custom geocode locations which is used to write output file column fields
     *
     * @param locations collection of DB location elements
     * @return return the collection of custom geocode locations
     */
    public abstract Collection<GeocodeLocation> process(Collection<DBLocationElement> locations);
}
