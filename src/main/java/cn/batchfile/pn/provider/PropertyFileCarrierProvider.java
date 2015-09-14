package cn.batchfile.pn.provider;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import cn.batchfile.pn.CarrierProvider;

public class PropertyFileCarrierProvider
implements CarrierProvider
{

public PropertyFileCarrierProvider()
{
}

public String getCarrier(String number)
{
    init();
    String s = StringUtils.substring(number, 0, 4);
    try
    {
        String m = rb.getString(s);
        if(!StringUtils.isEmpty(m))
            return m;
    }
    catch(MissingResourceException e) { }
    s = StringUtils.substring(number, 0, 3);
    try
    {
        String m = rb.getString(s);
        if(!StringUtils.isEmpty(m))
            return m;
    }
    catch(MissingResourceException e) { }
    return "";
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

private static final Logger LOG = Logger.getLogger(PropertyFileCarrierProvider.class);
private static final String PROPERTIES_FILE = "cn/batchfile/pn/carriers";
private static ResourceBundle rb = null;

}
