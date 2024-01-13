package org.jhotdraw8.fxcontrols.fontchooser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.application.resources.ModulepathResources;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.base.text.NaturalSortCollator;
import org.jhotdraw8.os.macos.PListParsers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MacOSFontCollectionsFactory extends DefaultFontCollectionsFactory {
    private final @NonNull Logger LOGGER = Logger.getLogger(MacOSFontCollectionsFactory.class.getName());

    @Override
    protected @NonNull ObservableList<FontCollection> generateCollections(@NonNull List<FontFamily> families) {
        List<FontCollection> collections = new ArrayList<>();

        final Resources labels = ModulepathResources.getResources(FontDialog.class.getModule(), "org.jhotdraw8.fxcontrols.spi.labels");

        // All fonts
        FontCollection allFonts = new FontCollection(labels.getString("FontCollection.allFonts"), true, families);
        collections.add(allFonts);

        // Read font collections
        Path dir = Paths.get(System.getProperty("user.home"), "Library", "FontCollections");
        Map<String, FontFamily> familiesMap = families.stream().collect(Collectors.toMap(FontFamily::getName, Function.identity()));
        families.stream().forEach(ff -> {
            String name = ff.getName();
            familiesMap.put(name.replaceAll(" ", ""), ff);
            int pblank = name.indexOf(' ');
            if (pblank != -1) {
                String firstPartOfName = name.substring(0, pblank);
                familiesMap.putIfAbsent(firstPartOfName, ff);
            }
        });
        try {
            Files.list(dir).filter(path ->
                    {
                        String fileName = path.getFileName().toString();
                        return !"com.apple.Recents.collection".equals(fileName)
                                && fileName.endsWith(".collection");
                    })
                    .forEach(path -> {
                        try {
                            collections.add(readFontCollection(familiesMap, path));
                        } catch (IOException e) {
                            LOGGER.log(Level.FINE, "Not a font collection. path=" + path);
                        }
                    });
            collections.sort(Comparator.comparing(FontCollection::getName, new NaturalSortCollator()));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not read font collections directory. dir=" + dir);
        }

        return FXCollections.observableArrayList(collections);
    }

    /**
     * Reads a font collection from the specified font collection file.
     *
     * @param families the list of font families from which the font collection can choose from
     * @param path     the path to the font collection file
     * @return
     * @throws IOException
     */
    private @NonNull FontCollection readFontCollection(@NonNull Map<String, FontFamily> families, @NonNull Path path) throws IOException {
        try {
            var map = PListParsers.toMap(PListParsers.readPList(path.toFile()));
            Set<FontFamily> fontFamilies = new LinkedHashSet<>();

            List<LinkedHashMap<String, Object>> plist = (List<LinkedHashMap<String, Object>>) map.getOrDefault("plist", List.of());
            if (plist.isEmpty()) throw new IOException("Could not find a plist. path=" + path);
            LinkedHashMap<String, Object> plistMap = plist.get(0);
            List<Object> objectsList = (List<Object>) plistMap.getOrDefault("$objects", List.of());

            for (Object o : objectsList) {
                if (o instanceof String potentialFontFamilyName) {
                    int pminus = potentialFontFamilyName.indexOf('-');
                    String fontFamilyName = potentialFontFamilyName.substring(0, pminus == -1 ? potentialFontFamilyName.length() : pminus);
                    FontFamily fontFamily = families.get(fontFamilyName);
                    if (fontFamily == null && fontFamilyName.endsWith("ITCTT")) {
                        fontFamily = families.get(fontFamilyName.substring(0, fontFamilyName.length() - 5));
                    }
                    if (fontFamily != null) {
                        fontFamilies.add(fontFamily);
                    }
                }
            }

            String collectionName = path.getFileName().toString();
            if (collectionName.endsWith(".collection")) {
                collectionName = collectionName.substring(0, collectionName.length() - 11);
            }

            if (fontFamilies.isEmpty()) {
                throw new IOException("Font collection is empty. path=" + path);
            }

            ArrayList<FontFamily> familiesList = new ArrayList<>(fontFamilies);
            familiesList.sort(Comparator.comparing(FontFamily::getName, new NaturalSortCollator()));
            return new FontCollection(collectionName, familiesList);
        } catch (ClassCastException e) {
            throw new IOException("Could not cast element in plist. path=" + path);
        }
    }
}
