package cn.batchfile.pn.provider;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import cn.batchfile.pn.SpecialProvider;

public class PropertyFileSpecialProvider
implements SpecialProvider
{

public PropertyFileSpecialProvider()
{
}

public String getSpecialNumber(String number)
{
    init();
    int pos = number.length();
    for(int i = pos; i > 0; i--)
    {
        String p = number.substring(0, i);
        try
        {
            String m = rb.getString(p);
            if(!StringUtils.isEmpty(m))
                return p;
        }
        catch(MissingResourceException e) { }
    }

    return "";
}

private void init()
{
    if(rb == null)
        initMap();
}

private synchronized void initMap()
{
    rb = ResourceBundle.getBundle(PROPERTIES_FILE);
    LOG.debug("properties file loaded: " + PROPERTIES_FILE);
}

private static final Logger LOG = Logger.getLogger(PropertyFileSpecialProvider.class);
private static final String PROPERTIES_FILE = "cn/batchfile/pn/specials";
private static ResourceBundle rb = null;

}
