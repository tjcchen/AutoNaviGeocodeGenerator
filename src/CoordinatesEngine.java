package com.tjcchen.coordinates;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A coordinates generator engine for POIs
 * <p>
 * Usage: javatr.sh com.tjcchen.coordinates.CoordinatesEngine -i xxx1.csv -o xxx2.csv
 *
 * <ul class="description">
 *   <li class="input-file" data-alias="i">One column of location ids list from input file</li>
 *   <li class="output-file" data-alias="o">Six columns of associated fields: "locationId", "locationName", "addressName", "cityName", "longitude", "latitude"</li>
 * </ul>
 *
 * @author yangchen
 * @email tjcchen.engineer@gmail.com
 * @since 09/08/2016
 */
public class CoordinatesEngine
{
    private static final int S_ERROR_CODE     = 1;
    private static final int S_INVALID_LOC_ID = -1;

    /**
     * Private class for command line arguments collecting
     */
    private static class CmdLineArgs
    {
        @Option(name = "--input", aliases = { "-i" }, usage = "input locationIds file path", required = true)
        private String m_inputFile = null;

        @Option(name = "--output", aliases = { "-o" }, usage = "output coordinates file path", required = true)
        private String m_outputFile = null;

        public String getInputFile()
        {
            return m_inputFile;
        }

        public String getOutputFile()
        {
            return m_outputFile;
        }
    }

    /**
     * Coordinates engine application main entry function
     *
     * @param args command line arguments
     * @throws Exception application exception
     */
    public static void main(String... args) throws Exception
    {
        final CmdLineArgs cmdLineArgs = new CmdLineArgs();
        final CmdLineParser parser = new CmdLineParser(cmdLineArgs);

        // parse command line input arguments
        try
        {
            parser.parseArgument(args);
        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
            System.exit(S_ERROR_CODE);
        }

        // instantiate GeocodeGenerator with AutoNavi
        GeocodeGenerator generator = new AutoNaviGeocodeGenerator();

        // retrieve locationId collections from input file
        Set<Integer> locationIds = Sets.newHashSet();
        try
        {
            locationIds = generator.readFile(cmdLineArgs.getInputFile());
        }
        catch (Exception ex)
        {
            System.err.println("An error occurred when reading input file: " + ex.getMessage());
            System.exit(S_ERROR_CODE);
        }

        // convert corresponding locationIds to DB location elements
        List<DBLocationElement> locations = locationIds.stream()
                                                       .filter(locId -> locId != S_INVALID_LOC_ID)
                                                       .map(DBLocationStore::get)
                                                       .filter(Objects::nonNull)
                                                       .collect(Collectors.toList());

        // exit application when failed getting location elements with input locationIds
        if (BaseUtils.isEmptySafe(locations))
        {
            System.err.println(String.format("Empty or invalid locations retrieved from %s, please check.", cmdLineArgs.getInputFile()));
            System.exit(S_ERROR_CODE);
        }

        // cache location elements
        LocationCache.getInstance().cacheLocations(locations);

        // processing...
        int statusCode = S_ERROR_CODE;
        try
        {
            statusCode = generator.writeFile(cmdLineArgs.getOutputFile(), generator.process(locations));
        }
        catch (Exception ex)
        {
            System.err.println("An error occurred when writing output file: " + ex.getMessage());
            System.exit(statusCode);
        }

        System.exit(statusCode);
    }
}
