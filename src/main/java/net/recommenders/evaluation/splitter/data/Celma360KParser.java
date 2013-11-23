/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.recommenders.evaluation.splitter.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.recommenders.evaluation.core.DataModel;

/**
 *
 * @author alejandr
 */
public class Celma360KParser extends AbstractCelmaParser {

    public static final int USER_TOK = 0;
    public static final int ARTIST_TOK = 1;
    public static final int TRACK_TOK = 2;
    public static final int PREF_TOK = 3;

    public Celma360KParser(boolean useArtists) {
        super(useArtists);
    }

    public DataModel<Long, Long> parseData(File f, String mapIdsPrefix) throws IOException {
        DataModel<Long, Long> dataset = new DataModel<Long, Long>();

        Map<String, Long> mapUserIds = new HashMap<String, Long>();
        Map<String, Long> mapItemIds = new HashMap<String, Long>();

        long curUser = getIndexMap(new File(mapIdsPrefix + "_userId.txt"), mapUserIds);
        long curItem = getIndexMap(new File(mapIdsPrefix + "_itemId.txt"), mapItemIds);

        BufferedReader br = new BufferedReader(new FileReader(f));
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] toks = line.split("\t");
            // user
            String user = toks[USER_TOK];
            if (!mapUserIds.containsKey(user)) {
                mapUserIds.put(user, curUser);
                curUser++;
            }
            long userId = mapUserIds.get(user);
            // item
            String artist = toks[ARTIST_TOK];
            String track = toks[TRACK_TOK];
            String item = null;
            if (useArtists) {
                item = artist;
            } else {
                item = artist + "_" + track;
            }
            if (!mapItemIds.containsKey(item)) {
                mapItemIds.put(item, curItem);
                curItem++;
            }
            long itemId = mapItemIds.get(item);
            // preference
            double preference = 1.0;
            if (PREF_TOK != -1) {
                preference = Double.parseDouble(toks[PREF_TOK]);
//                preference = TypeFormat.parseDouble(toks[prefTok], new Cursor());
            }
            //////
            // update information
            //////
            dataset.addPreference(userId, itemId, preference);
        }
        br.close();

        // save map ids?
        if (mapIdsPrefix != null) {
            // save user map
            PrintStream outUser = new PrintStream(mapIdsPrefix + "_userId.txt");
            for (Entry<String, Long> e : mapUserIds.entrySet()) {
                outUser.println(e.getKey() + "\t" + e.getValue());
            }
            outUser.close();
            // save item map
            PrintStream outItem = new PrintStream(mapIdsPrefix + "_itemId.txt");
            for (Entry<String, Long> e : mapItemIds.entrySet()) {
                outItem.println(e.getKey() + "\t" + e.getValue());
            }
            outItem.close();
        }

        return dataset;
    }
}
