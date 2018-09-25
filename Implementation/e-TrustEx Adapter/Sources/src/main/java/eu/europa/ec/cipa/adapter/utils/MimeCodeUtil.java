package eu.europa.ec.cipa.adapter.utils;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

public final class MimeCodeUtil{

	private BidiMap epriorCodes = null;
	
	public MimeCodeUtil() {
		epriorCodes = new DualHashBidiMap();
		
		epriorCodes.put("application/pdf",                                                   "pdf");
		epriorCodes.put("text/plain",                                                        "txt");
		epriorCodes.put("image/gif",                                                         "gif");
		epriorCodes.put("image/tiff",                                                        "tiff");
		epriorCodes.put("image/png",                                                         "png");
		epriorCodes.put("image/jpeg",                                                        "jpeg");
		epriorCodes.put("text/csv",                                                          "csv");
	    epriorCodes.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
	    epriorCodes.put("application/vnd.oasis.opendocument.spreadsheet",                    "ods");
	    epriorCodes.put("text/xml",                                                          "xml");
	}
    
	
    public String getDomainFromCode(String code) {
    	Object object = epriorCodes.get(code);
    	if(object != null){
        return (String) object;
    	}else{
    		return null;
    	}
    }
	
}
