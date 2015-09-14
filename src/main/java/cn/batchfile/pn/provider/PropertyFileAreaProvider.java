package cn.batchfile.pn.provider;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import cn.batchfile.pn.AreaProvider;

public class PropertyFileAreaProvider
implements AreaProvider
{

public PropertyFileAreaProvider()
{
}

public String getArea(String code)
{
    init();
    String n = StringUtils.stripStart(StringUtils.stripStart(code, "+"), "0");
    try
    {
        String s = rb.getString(n);
        return StringUtils.isEmpty(s) ? "" : s;
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

private static final Logger LOG = Logger.getLogger(PropertyFileAreaProvider.class);
private static final String PROPERTIES_FILE = "cn/batchfile/pn/areas";
private static ResourceBundle rb = null;

}
