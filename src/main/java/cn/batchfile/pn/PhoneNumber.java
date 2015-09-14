package cn.batchfile.pn;

import java.text.ParseException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import cn.batchfile.pn.provider.PropertyFileAreaProvider;
import cn.batchfile.pn.provider.PropertyFileCarrierProvider;
import cn.batchfile.pn.provider.PropertyFileCityProvider;
import cn.batchfile.pn.provider.PropertyFilePrefixProvider;
import cn.batchfile.pn.provider.PropertyFileSegmentProvider;
import cn.batchfile.pn.provider.PropertyFileSpecialProvider;

public class PhoneNumber
{

    public static PhoneNumber parse(String number)
        throws ParseException
    {
        if(StringUtils.isBlank(number))
        {
            return null;
        } else
        {
            PhoneNumber pn = new PhoneNumber();
            pn.fromString(number);
            return pn;
        }
    }

    private PhoneNumber()
    {
        number = "";
        prefix = "";
        area = "";
        city = "";
        local = "";
        fine = "";
        extension = "";
        carrier = "";
        mobile = false;
        special = false;
    }

    public AreaProvider getAreaProvider()
    {
        return _areaProvider;
    }

    public void setAreaProvider(AreaProvider areaProvider)
    {
        _areaProvider = areaProvider;
    }

    public CarrierProvider getCarrierProvider()
    {
        return _carrierProvider;
    }

    public void setCarrierProvider(CarrierProvider carrierProvider)
    {
        _carrierProvider = carrierProvider;
    }

    public CityProvider getCityProvider()
    {
        return _cityProvider;
    }

    public void setCityProvider(CityProvider cityProvider)
    {
        _cityProvider = cityProvider;
    }

    public SegmentProvider getSegmentProvider()
    {
        return _segmentProvider;
    }

    public void setSegmentProvider(SegmentProvider segmentProvider)
    {
        _segmentProvider = segmentProvider;
    }

    public SpecialProvider getSpecialProvider()
    {
        return _specialProvider;
    }

    public void setSpecialProvider(SpecialProvider specialProvider)
    {
        _specialProvider = specialProvider;
    }

    public PrefixProvider getPrefixProvider()
    {
        return _prefixProvider;
    }

    public void setPrefixProvider(PrefixProvider prefixProvider)
    {
        _prefixProvider = prefixProvider;
    }

    public String getNumber()
    {
        return number;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public String getAreaCode()
    {
        return area;
    }

    public String getAreaName()
    {
        return _areaProvider.getArea(area);
    }

    public String getCityCode()
    {
        return city;
    }

    public String getCityName()
    {
        return _cityProvider.getCity(city);
    }

    public String getLocal()
    {
        return local;
    }

    public String getExtension()
    {
        return extension;
    }

    public String getFineNumber()
    {
        return fine;
    }

    public String getFormattedNumber()
    {
        if(mobile)
            return formatMobileNumber();
        if(special)
            return formatSpecialNumber();
        else
            return formatPstnNumber();
    }

    public String getCarrier()
    {
        return carrier;
    }

    public boolean isMobile()
    {
        return mobile;
    }

    public boolean isSpecial()
    {
        return special;
    }

    public String toString()
    {
        return getFormattedNumber();
    }

    private String parseAreaCode(String number)
    {
        if(number.startsWith("+") || number.startsWith("00"))
        {
            String s = StringUtils.stripStart(StringUtils.stripStart(number, "+"), "0");
            for(int pos = s.length(); pos > 0; pos--)
            {
                String p = s.substring(0, pos);
                String area = _areaProvider.getArea(p);
                if(!StringUtils.isEmpty(area))
                    return p;
            }

            return "";
        } else
        {
            return "";
        }
    }

    private String parseCityCode(String area, String number)
    {
        if(!StringUtils.isEmpty(area) || number.startsWith("0"))
        {
            number = StringUtils.stripStart(number, "0");
            for(int pos = number.length(); pos > 0; pos--)
            {
                String p = number.substring(0, pos);
                if(!StringUtils.isEmpty(_cityProvider.getCity(p)))
                    return p;
            }

            return "";
        } else
        {
            return "";
        }
    }

    private void parseMobileNumber(String number)
    {
        if(number.length() > 11)
        {
            local = number.substring(0, 11);
            extension = number.substring(11);
        } else
        {
            local = number;
        }
        if(local.length() == 11)
            city = _segmentProvider.getMobileNumberCity(local);
    }

    private void parsePstnNumber(String area, String number)
    {
        city = parseCityCode(area, number);
        number = StringUtils.stripStart(number, "0");
        String localAndExt = StringUtils.substring(number, city.length());
        String speCode = _specialProvider.getSpecialNumber(localAndExt);
        if(!StringUtils.isEmpty(speCode))
        {
            special = true;
            local = localAndExt;
            return;
        }
        if(city.length() == 2 || localAndExt.length() == 8)
        {
            local = StringUtils.substring(localAndExt, 0, 8);
            extension = StringUtils.substring(localAndExt, 8);
        } else
        if(city.length() == 3 || localAndExt.length() == 7)
        {
            local = StringUtils.substring(localAndExt, 0, 7);
            extension = StringUtils.substring(localAndExt, 7);
        } else
        {
            local = localAndExt;
        }
    }

    private void fromString(String number)
        throws ParseException
    {
        LOG.debug((new StringBuilder()).append("parse telephone number: ").append(number).toString());
        this.number = number;
        String n = removeLossyChar(number);
        prefix = _prefixProvider.getPrefix(n);
        if(!StringUtils.isEmpty(prefix))
            fine = n.substring(prefix.length());
        else
            fine = n;
        area = parseAreaCode(fine);
        LOG.debug((new StringBuilder()).append("area: ").append(area).toString());
        String civil = fine;
        if(!StringUtils.isEmpty(area))
        {
            civil = StringUtils.stripStart(civil, "0");
            civil = StringUtils.stripStart(civil, "+");
            civil = StringUtils.substring(civil, area.length());
        }
        if(!StringUtils.isEmpty(area) && !StringUtils.equals("86", area))
        {
            local = civil;
            return;
        }
        carrier = _carrierProvider.getCarrier(StringUtils.stripStart(civil, "0"));
        if(!StringUtils.isEmpty(carrier))
        {
            mobile = true;
            parseMobileNumber(StringUtils.stripStart(civil, "0"));
        } else
        {
            parsePstnNumber(area, civil);
        }
    }

    private String removeLossyChar(String number)
        throws ParseException
    {
        String s = "";
        boolean ok = false;
        for(int i = 0; i < number.length(); i++)
        {
            char c = number.charAt(i);
            if(c >= '0' && c <= '9')
            {
                s = (new StringBuilder()).append(s).append(c).toString();
                ok = true;
                continue;
            }
            if(c == '+' || c == '#' || c == '*')
                s = (new StringBuilder()).append(s).append(c).toString();
        }

        if(!ok)
            throw new ParseException("Invalid phone number format", 0);
        else
            return s;
    }

    private String formatMobileNumber()
    {
        String s = formatAreaAndCity();
        if(local.length() == 11)
        {
            String c = local.substring(0, 3);
            String h = local.substring(3, 7);
            String n = local.substring(7, 11);
            s = (new StringBuilder()).append(s).append(c).append("-").append(h).append("-").append(n).toString();
        } else
        {
            s = local;
        }
        s = (new StringBuilder()).append(s).append(extension.length() <= 0 ? "" : (new StringBuilder()).append(" ").append(extension).toString()).toString();
        return s;
    }

    private String formatSpecialNumber()
    {
        String s = formatAreaAndCity();
        return (new StringBuilder()).append(s).append(local).toString();
    }

    private String formatPstnNumber()
    {
        String s = formatAreaAndCity();
        if(local.length() == 8)
        {
            String b = local.substring(0, 4);
            String n = local.substring(4, 8);
            s = (new StringBuilder()).append(s).append(b).append("-").append(n).toString();
        } else
        if(local.length() == 7)
        {
            String b = local.substring(0, 3);
            String n = local.substring(3, 7);
            s = (new StringBuilder()).append(s).append(b).append("-").append(n).toString();
        } else
        {
            s = (new StringBuilder()).append(s).append(local).toString();
        }
        s = (new StringBuilder()).append(s).append(extension.length() <= 0 ? "" : (new StringBuilder()).append(" ").append(extension).toString()).toString();
        return s;
    }

    private String formatAreaAndCity()
    {
        String s = "";
        s = (new StringBuilder()).append(s).append(StringUtils.isEmpty(area) ? "" : (new StringBuilder()).append("+").append(area).toString()).toString();
        if(!mobile)
            if(StringUtils.isEmpty(s))
                s = (new StringBuilder()).append(s).append(StringUtils.isEmpty(city) ? "" : (new StringBuilder()).append("0").append(city).toString()).toString();
            else
            if(!StringUtils.isEmpty(city))
                s = (new StringBuilder()).append(s).append("-").append(city).toString();
        if(!StringUtils.isEmpty(s))
            s = (new StringBuilder()).append("(").append(s).append(")").toString();
        return s;
    }

    private static final Logger LOG = Logger.getLogger(PhoneNumber.class);
    private static final int MOBILE_NUMBER_LENGTH = 11;
    private static final int SHORT_CITY_CODE_LENGTH = 2;
    private static final int LONG_CITY_CODE_LENGTH = 3;
    private static final int SHORT_PSTN_NUMBER_LENGTH = 7;
    private static final int LONG_PSTN_NUMBER_LENGTH = 8;
    private static final String CHINA_AREA_CODE = "86";
    private static AreaProvider _areaProvider = new PropertyFileAreaProvider();
    private static CarrierProvider _carrierProvider = new PropertyFileCarrierProvider();
    private static CityProvider _cityProvider = new PropertyFileCityProvider();
    private static SegmentProvider _segmentProvider = new PropertyFileSegmentProvider();
    private static SpecialProvider _specialProvider = new PropertyFileSpecialProvider();
    private static PrefixProvider _prefixProvider = new PropertyFilePrefixProvider();
    private String number;
    private String prefix;
    private String area;
    private String city;
    private String local;
    private String fine;
    private String extension;
    private String carrier;
    private boolean mobile;
    private boolean special;

}
