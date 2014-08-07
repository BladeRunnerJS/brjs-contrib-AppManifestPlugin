/**
 * Created by robm on 05/08/2014.
 */

import org.bladerunnerjs.model.*;
import org.bladerunnerjs.model.exception.ConfigException;
import org.bladerunnerjs.model.exception.request.ContentProcessingException;
import org.bladerunnerjs.model.exception.request.MalformedTokenException;
import org.bladerunnerjs.plugin.CharResponseContent;
import org.bladerunnerjs.plugin.Locale;
import org.bladerunnerjs.plugin.ResponseContent;
import org.bladerunnerjs.plugin.base.AbstractContentPlugin;
import org.bladerunnerjs.utility.AppMetadataUtility;
import org.bladerunnerjs.utility.ContentPathParser;
import org.bladerunnerjs.utility.ContentPathParserBuilder;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AppManifestPlugin extends AbstractContentPlugin {

    private static final String APP_MANIFEST_REQUEST = "app-manifest-request";
    private ContentPathParser contentPathParser;
    private BRJS brjs;
    public File configFile;

    {
        ContentPathParserBuilder contentPathParserBuilder = new ContentPathParserBuilder();
        contentPathParserBuilder.accepts("manifest.webapp").as(APP_MANIFEST_REQUEST);
        contentPathParser = contentPathParserBuilder.build();
        configFile = new File("C:\\Users\\robm\\AppManifestPlugin\\src\\test\\resources\\testApp\\manifest.json");
    }

    @Override
    public String getRequestPrefix() {
        return "manifest.webapp";
    }

    @Override
    public String getCompositeGroupName() {
        return null;
    }

    @Override
    public ContentPathParser getContentPathParser() {
        return contentPathParser;
    }

    @Override
    public ResponseContent handleRequest(ParsedContentPath contentPath, BundleSet bundleSet, UrlContentAccessor contentAccessor, String version) throws ContentProcessingException {
        App app = bundleSet.getBundlableNode().app();
        if (contentPath.formName.equals(APP_MANIFEST_REQUEST)) {
            return new CharResponseContent(brjs, getManifestContents(configFile, app, version));
        } else {
            throw new ContentProcessingException("unknown request form '" + contentPath.formName + "'.");
        }
    }

    @Override
    public List<String> getValidDevContentPaths(BundleSet bundleSet, Locale... locales) throws ContentProcessingException {
        return getValidProdContentPaths(bundleSet, locales);
    }

    @Override
    public List<String> getValidProdContentPaths(BundleSet bundleSet, Locale... locales) throws ContentProcessingException {
        try {
            return Arrays.asList(contentPathParser.createRequest(APP_MANIFEST_REQUEST));
        } catch (MalformedTokenException e) {
            throw new ContentProcessingException(e);
        }
    }

    @Override
    public void setBRJS(BRJS brjs) {
        this.brjs = brjs;
    }

    public String getManifestContents(File configFile, App app, String version) {
        byte[] data = getData(configFile);
        String fileText = getTextFromConfigFile(data);
        String JSONText = translateYAMLTextToJSON(fileText);
        return addAutomaticFieldsToManifest(JSONText, app, version);
    }

    private String addAutomaticFieldsToManifest(String jsonText, App app, String version) {
        String reformedJSONText = jsonText.substring(0, jsonText.length() - 1);
        try {
            reformedJSONText += (",\"default_locale\":" + "\"" + app.appConf().getDefaultLocale() + "\",\"version\":" + "\"" + AppMetadataUtility.getRelativeVersionedBundlePath(version, "") + "\"}");
        } catch (ConfigException e) {
            e.printStackTrace();
        }
        return reformedJSONText;
    }

    private String translateYAMLTextToJSON(String fileText) {
        Yaml yaml = new Yaml();
        Map map = (Map) yaml.load(fileText);
        JSONObject jsonObject = new JSONObject(map);
        return jsonObject.toString();
    }

    private String getTextFromConfigFile(byte[] data) {
        String fileText = null;
        try {
            fileText = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return fileText;
    }

    private byte[] getData(File configFile) {
        FileInputStream fileInputStream;
        byte[] data = null;
        try {
            fileInputStream = new FileInputStream(configFile);

            data = new byte[(int) configFile.length()];
            fileInputStream.read(data);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
