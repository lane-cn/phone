package cn.batchfile.pn.provider;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import cn.batchfile.pn.CityProvider;

public class PropertyFileCityProvider
implements CityProvider
{

public PropertyFileCityProvider()
{
}

public String getCity(String code)
{
    init();
    String s = StringUtils.stripStart(code, "0");
    try
    {
        String m = rb.getString(s);
        return StringUtils.isEmpty(m) ? "" : m;
    }
    catch(MissingResourceException e)
    {
        return "";
    }
}

private void init()
{
    if(rb == null)
        initProps();
}

private synchronized void initProps()
{
    rb = ResourceBundle.getBundle(PROPERTIES_FILE);
    LOG.debug("properties file loaded: " + PROPERTIES_FILE);
}

private static final Logger LOG = Logger.getLogger(PropertyFileCityProvider.class);
private static final String PROPERTIES_FILE = "cn/batchfile/pn/cities";
private static ResourceBundle rb = null;

}
