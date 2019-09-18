public class Message {

    private String message;
    private String Date;
    private String Sender;
    private String ServerName;


    public Message(String data, String Date, String Sender, String serverName){


        this.message = data;
        this.Date = Date;
        this.Sender = Sender;
        this.ServerName = serverName;

    }

    public String getMessage(){
        return message;
    }

    public String getDate(){
        return Date;
    }

    public String getSender(){
        return Sender;
    }

    public String getServerName() {
        return ServerName;
    }
}
