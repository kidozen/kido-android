package kidozen.client;
import java.util.Date;

public class Metadata {
	public String createdBy;
	public String createdOn;
	public Boolean isPrivate;
	public Integer sync;
	public String updatedBy;
    private Date _updatedOn;
    
    public Date updatedOn()
    {
    	return _updatedOn;
    }
    
    public void updatedOn(Date date)
    {
    	_updatedOn = date;
    }
    
    
}
