package kidozen.client;

public enum KZHttpMethod {
	GET,
	POST,
	PUT,
	DELETE,
    HEAD,
	TRACE,
	CONNECT;

    @Override
    public String toString() {
        switch(this) {
            case GET: return "GET";
            case POST: return "POST";
            case PUT: return "PUT";
            case DELETE: return "DELETE";
            default: throw new IllegalArgumentException();
        }
    }
}
