package masdar.jh;

import org.json.JSONObject;

public class Response {
    int         statusCode;
    JSONObject      data;

    public Response(int statusCode, JSONObject data) {
        this.statusCode = statusCode;
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Response{" +
                "statusCode=" + statusCode +
                ", data=" + data.toString() +
                "} ";
    }
}
