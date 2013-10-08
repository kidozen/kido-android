package kidozen.client;

import java.util.HashMap;
import java.util.List;

public class Mail {
	HashMap<String, String> email =new HashMap<String, String >();
    public List<String> Attachments;

    public void to (String value)
	{
		email.put("to", value);
	};
    public void from (String value)
	{
    	email.put("from", value);
	}
    public void subject (String value)
	{
    	email.put("subject", value);
	}
    public void htmlBody (String value)
	{
    	email.put("bodyHtml", value);
	}
    public void textBody (String value)
	{
    	email.put("bodyText", value);
	}
    public void attachments (List<String> attachments)
    {
        Attachments = attachments;
    }
    public HashMap<String, String> GetHashMap ()
    {
    	return email;
    }
    
    @Override
    public String toString() {
    	return email.toString();
    }
	

}
